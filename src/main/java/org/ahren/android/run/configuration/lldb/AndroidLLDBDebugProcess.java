/*
 * Copyright 2019 Ahren Li(www.lili.kim) AndroidNativeDebug
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ahren.android.run.configuration.lldb;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.jetbrains.cidr.ArchitectureType;
import com.jetbrains.cidr.execution.TrivialRunParameters;
import org.ahren.android.adb.devices.AdbDeviceProxy;
import org.ahren.android.debug.AndroidDebugParameters;
import com.google.common.collect.Sets;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.jetbrains.cidr.execution.debugger.CidrDebugProcess;
import com.jetbrains.cidr.execution.debugger.backend.*;
import com.jetbrains.cidr.execution.debugger.breakpoints.CidrSymbolicBreakpointType;
import com.jetbrains.cidr.execution.debugger.breakpoints.CidrSymbolicBreakpointType.Properties;
import org.ahren.android.run.configuration.SessionStarter;
import org.ahren.android.utils.Log;
import org.ahren.android.utils.ProgressReporter;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class AndroidLLDBDebugProcess extends CidrDebugProcess {

    private final static boolean DEBUG = true;
    private final static Logger LOG = Log.factory("AndroidLLDBDebugProcess");
    private static final CidrSymbolicBreakpointType SYMBOLIC_BREAKPOINT_TYPE;
    private static final String LIBART_SO = "libart.so";
    private static final String ART_SIGSEGV_FAULT = "art_sigsegv_fault";

    private final Set<String> myLoadedModules = Sets.newHashSet();
    private Project mProject;
    private boolean myArt = false;
    private AndroidDebugParameters mParameters;
    private AdbDeviceProxy mAdbProxy;
    private ProgressReporter myProgressReporter;
    private SessionStarter mSessionStarter;

    private XBreakpoint<Properties> myArtSigSegvFaultBp;

    static {
        SYMBOLIC_BREAKPOINT_TYPE = XBreakpointType.EXTENSION_POINT_NAME.findExtension(CidrSymbolicBreakpointType.class);
    }

    @SuppressWarnings("WeakerAccess")
    public AndroidLLDBDebugProcess(DebuggerDriverConfiguration configuration,
                                   @NotNull AndroidDebugParameters parameters,
                                   @NotNull XDebugSession session,
                                   @NotNull TextConsoleBuilder consoleBuilder,
                                   @NotNull ProgressReporter progressReporter) throws ExecutionException {

        super(new TrivialRunParameters(configuration, new GeneralCommandLine(), ArchitectureType.MIPS), session, consoleBuilder);
        mParameters = parameters;
        mAdbProxy = new AdbDeviceProxy(parameters.getSerialNum());
        mProject= session.getProject();
        myProgressReporter = progressReporter;
        mSessionStarter = new SessionStarter(mParameters, mAdbProxy, myProgressReporter);
    }

    @NotNull
    @Override
    protected DebuggerDriver.Inferior doLoadTarget(@NotNull DebuggerDriver debuggerDriver) throws ExecutionException {

        if(mParameters.isAdbCanRoot()){
            doAdbRoot();
            mSessionStarter.startServer();
        }

        AndroidLLDBDriver driver = (AndroidLLDBDriver) debuggerDriver;
        if(DEBUG) LOG.info("doLoadTarget");
        if(mParameters.getProcessPid() > 0){
            return driver.loadForAttach(mParameters.getProcessPid(), () -> {
            }, () -> {
                this.myArt = this.isArtVM();
                if (this.myArt) {
                    LOG.info("Running in ART VM");
                    this.initArtSigSegvFaultBreakpoint();
                } else {
                    LOG.info("Running in Dalvik VM");
                }
            });
        }

        return driver.loadForAttach(mParameters.getProcessName(), () -> {
        }, () -> {
            this.myArt = this.isArtVM();
            if (this.myArt) {
                LOG.info("Running in ART VM");
                this.initArtSigSegvFaultBreakpoint();
            } else {
                LOG.info("Running in Dalvik VM");
            }

//            this.loadExplicitSymbols();
        });
    }

    @Override
    protected long doStartTarget(DebuggerDriver.@NotNull Inferior inferior) throws ExecutionException {
        long ret = super.doStartTarget(inferior);
        if(DEBUG) LOG.info("doStartTarget");
        myProgressReporter.finish();
        return ret;
    }

    @Override
    public void handleModulesLoaded(@NotNull List<LLModule> modules) {
        List<String> trimmedModules = new ArrayList<>();
        for(LLModule module : modules){
            String moduleName = module.getName();
            if(!moduleName.isEmpty()){
                moduleName = moduleName.trim();
                if(!myLoadedModules.contains(moduleName)){
                    trimmedModules.add(moduleName);
                    myProgressReporter.step("Loaded module: " + module.getName());
                    if(DEBUG) LOG.info("handleModulesLoaded:" + module);
                }
            }
        }

        super.handleModulesLoaded(modules);
        myLoadedModules.addAll(trimmedModules);
    }

    @Override
    public boolean isDetachDefault() {
        return true;
    }

    @Override
    public void stop() {
        super.stop();
        myProgressReporter.finish();
        getApp().invokeLater(() -> getApp().runWriteAction(() -> {
            if (this.myArtSigSegvFaultBp != null && !mProject.isDisposed()) {
                XDebuggerManager.getInstance(mProject).getBreakpointManager().removeBreakpoint(this.myArtSigSegvFaultBp);
                this.myArtSigSegvFaultBp = null;
            }

        }));
        if(mAdbProxy.getDevice().isRoot()){
            mAdbProxy.exec("killall lldb-server");
        }
    }

    private boolean isArtVM() {
        return this.myLoadedModules.contains("libart.so");
    }

    @NotNull
    @SuppressWarnings("WeakerAccess")
    protected static Application getApp() {
        return ApplicationManager.getApplication();
    }

    @NotNull
    @SuppressWarnings("WeakerAccess")
    protected XBreakpointManager getBreakpointManager() {
        return XDebuggerManager.getInstance(mProject).getBreakpointManager();
    }

    private void initArtSigSegvFaultBreakpoint() {
        getApp().runReadAction(() -> {
            XBreakpointManager manager = getBreakpointManager();
            Collection<? extends XBreakpoint<Properties>> list = manager.getBreakpoints(SYMBOLIC_BREAKPOINT_TYPE);
            for(XBreakpoint<Properties> point : list){
                Properties properties = point.getProperties();
                if(LIBART_SO.equals(properties.getModuleName()) && ART_SIGSEGV_FAULT.equals(properties.getSymbolPattern())){
                    myArtSigSegvFaultBp = point;
                }
            }
        });
        if (this.myArtSigSegvFaultBp == null) {
            getApp().invokeLater(() -> getApp().runWriteAction(() -> {
                Properties props = new Properties(ART_SIGSEGV_FAULT, LIBART_SO);
                this.myArtSigSegvFaultBp = this.getBreakpointManager().addBreakpoint(SYMBOLIC_BREAKPOINT_TYPE, props);
            }));
        }
    }

    private void doAdbRoot(){
        myProgressReporter.step("wait root device");
        if(DEBUG) LOG.info("wait root device");
        mAdbProxy.doRootWait();

        mSessionStarter.pushFilesToDevice();

        /*
        if(mAdbProxy.isSupportToybox()){
            String src = LLDBTools.getLLDB_server(mParameters.getDebugExePath(), mAdbProxy.getDevice());
            String cache = "/" + AndroidBundle.message("android.cache.partition");
            String dts = cache + "/" + AndroidBundle.message("android.lldb_server.name");

            try {
                ShellScript shellScript = new ShellScript();
                shellScript.addCommand("toybox killall " + AndroidBundle.message("android.lldb_server.name"));
                shellScript.addCommand("chmod 755 " + dts);
                shellScript.addCommand("toybox setsid " + dts
                        + " platform --server --listen unix-abstract:///sdcard/debug.sock &");

                if(DEBUG) LOG.info("getTempPath:" + PathManager.getTempPath());
                String script_path = PathManager.getTempPath() + File.separator + "lldb-start.sh";
                shellScript.writeToFile(script_path);

                if(DEBUG) LOG.info("push:lldb-start.sh");
                myProgressReporter.step("push:lldb-start.sh");
                mAdbProxy.pushFile(script_path, cache + "/lldb-start.sh");
                Thread.sleep(100);

                if(DEBUG) LOG.info("push:" + src + ",to " + dts);
                myProgressReporter.step("push:" + src);
                mAdbProxy.pushFile(src, dts);
                Thread.sleep(100);

                myProgressReporter.step("run:" + dts);
                mAdbProxy.exec("chmod 755 /cache/lldb-start.sh");
                mAdbProxy.exec("toybox setsid /cache/lldb-start.sh");
            }catch (Exception e){
                e.printStackTrace();
            }
        }*/
    }
}
