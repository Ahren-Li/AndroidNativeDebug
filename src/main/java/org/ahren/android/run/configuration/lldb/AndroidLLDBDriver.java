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

import com.jetbrains.cidr.ArchitectureType;
import com.jetbrains.cidr.execution.Installer;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerCommandException;
import com.jetbrains.cidr.execution.debugger.backend.LLBreakpoint;
import com.jetbrains.cidr.execution.debugger.backend.LLValue;
import com.jetbrains.cidr.execution.debugger.backend.LLWatchpoint;
import com.jetbrains.cidr.execution.debugger.backend.lldb.LLDBDriverConfiguration;
import com.jetbrains.cidr.execution.debugger.backend.lldb.auto_generated.ProtocolResponses;
import org.ahren.android.debug.AndroidDebugParameters;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ThrowableRunnable;
import com.jetbrains.cidr.execution.debugger.backend.lldb.LLDBDriver;
import com.jetbrains.cidr.execution.debugger.backend.lldb.LLDBDriverException;
import com.jetbrains.cidr.execution.debugger.backend.lldb.ProtobufMessageFactory;
import com.jetbrains.cidr.execution.debugger.backend.lldb.auto_generated.Protocol;
import org.ahren.android.utils.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;


public class AndroidLLDBDriver extends LLDBDriver {

    private static final Logger LOG = Log.factory("AndroidLLDBDriver");
    private static final Set<String> WATCH_POINT_WHITELISTED_MODELS = Sets.newHashSet("Nexus 9");
    private static final Set<String> WATCH_POINT_WHITELISTED_ABIS = ImmutableSet.of("X86", "X86_64");

    private final AndroidDebugParameters mParameters;
    private final boolean mIsAndroidArt;

    private ArrayList<String> mStartUpScripts;
    private ArrayList<String> myStartupCommands;
    private ArrayList<String> myPostAttachCommands;
    private boolean myDeviceSupportsWatchPoints = false;
    private boolean myReportedWatchPointsUsage = false;

    AndroidLLDBDriver(@NotNull AndroidDebugParameters parameters, @NotNull Handler handler, @NotNull ArchitectureType architectureType, @NotNull LLDBDriverConfiguration debuggerDriverConfiguration) throws ExecutionException {
        super(handler, debuggerDriverConfiguration, architectureType);
        LOG.info("architectureType=" + architectureType);
        mParameters = parameters;
        mIsAndroidArt = true;
        initCommands();

        if(WATCH_POINT_WHITELISTED_ABIS.contains(parameters.getArmAbi().toString())) myDeviceSupportsWatchPoints = true;
    }

    @NotNull
    public Inferior loadForLaunch(@NotNull Installer installer, @Nullable String architectureId) throws ExecutionException {
        throw new IllegalStateException("loadForLaunch is not supported on Android");
    }

    @NotNull
    @SuppressWarnings("unused")
    public Inferior loadForAttach(int pid, @NotNull final ThrowableRunnable<ExecutionException> preAttach, @NotNull final ThrowableRunnable<ExecutionException> postAttach) throws ExecutionException {
        commonLoad();
        final Inferior delegate = super.loadForAttach(pid);
        setSourceMaps();
        setSymbols();
        return new Inferior(0) {
            protected long startImpl() throws ExecutionException {
                preAttach.run();
                long ret = delegate.start();
                runPostAttachCommands();
                postAttach.run();
                return ret;
            }

            protected void detachImpl() throws ExecutionException {
                delegate.detach();
            }

            protected boolean destroyImpl() throws ExecutionException {
                return delegate.destroy();
            }
        };
    }

    @NotNull
    @SuppressWarnings("WeakerAccess")
    public Inferior loadForAttach(String name, @NotNull final ThrowableRunnable<ExecutionException> preAttach, @NotNull final ThrowableRunnable<ExecutionException> postAttach) throws ExecutionException {
        commonLoad();
        final Inferior delegate = super.loadForAttach(name, false);
        setSourceMaps();
        setSymbols();
        return new Inferior(0) {
            protected long startImpl() throws ExecutionException {
                preAttach.run();
                long ret = delegate.start();
                runPostAttachCommands();
                postAttach.run();
                return ret;
            }

            protected void detachImpl() throws ExecutionException {
                delegate.detach();
            }

            protected boolean destroyImpl() throws ExecutionException {
                return delegate.destroy();
            }
        };
    }

    @Override
    public boolean supportsWatchpoints() {
        return true;
    }

    @NotNull
    @Override
    public LLWatchpoint addWatchpoint(long threadId, int frameNumber, LLValue value, String expr, LLWatchpoint.Lifetime lifetime, LLWatchpoint.AccessType accessType) throws ExecutionException, DebuggerCommandException {
        if (!this.myDeviceSupportsWatchPoints) {
            throw new DebuggerCommandException("You are debugging on a device that is not known to support watch points - supported devices include " + getWatchPointWhitelist());
        } else {
            if (!this.myReportedWatchPointsUsage) {
                this.myReportedWatchPointsUsage = true;
            }

            return super.addWatchpoint(threadId, frameNumber, value, expr, lifetime, accessType);
        }
    }

    @Override
    protected void sendCreateTargetRequest(@NotNull Protocol.CompositeRequest createTargetRequest) throws ExecutionException {
        if (!createTargetRequest.getCreateTarget().getExePath().isEmpty()) {
            throw new IllegalStateException("Creating a target based on an executable path is not supported on Android");
        } else {
            super.sendCreateTargetRequest(createTargetRequest);
        }

    }

    @Override
    public @NotNull LLBreakpoint addBreakpoint(String path, int line, @Nullable String condition) throws ExecutionException, DebuggerCommandException {
        LOG.info("addBreakpoint,path="  + path);
        LOG.info("addBreakpoint,line="  + line);
        LOG.info("addBreakpoint,condition="  + condition);
        LOG.info("addBreakpoint,raw path="  + path);
        String newPath = path.replace(mParameters.getAndroidPath(),"");
        newPath = newPath.replace("\\", "/");
        if(newPath.startsWith("/")){
            newPath = newPath.substring(1);
        }
        LOG.info("addBreakpoint,new path="  + newPath);
        return super.addBreakpoint(newPath, line, condition);
    }

    @Override
    public void removeCodepoints(@NotNull Collection<Integer> ids) throws ExecutionException {
        LOG.info("removeCodepoints,");
        super.removeCodepoints(ids);
    }

    @Override
    public void setValuesFilteringEnabled(boolean enabled) throws ExecutionException {
        super.setValuesFilteringEnabled(true);
    }

    private void initCommands(){
        mStartUpScripts = new ArrayList<>();
        mStartUpScripts.add(mParameters.getDebugExePath() + "/shared/load_script");
        myPostAttachCommands = new ArrayList<>();
        myPostAttachCommands.add("settings set target.process.thread.step-avoid-regexp ''");
        myPostAttachCommands.add("type format add --format boolean jboolean");
        myStartupCommands = new ArrayList<>();
        myStartupCommands.add("settings set auto-confirm true");
        myStartupCommands.add("settings set plugin.symbol-file.dwarf.comp-dir-symlink-paths /proc/self/cwd");
        myStartupCommands.add("settings set plugin.jit-loader.gdb.enable-jit-breakpoint false");
    }

    private void setSourceMaps() throws ExecutionException{
        String source = mParameters.getAndroidPath();
        if(!StringUtil.isEmpty(source)){
            if (SystemInfo.isWindows) {
                LOG.info("Set settings set target.source-map: " + "\\proc\\self\\cwd," + source);
                executeInterpreterCommand("settings set target.source-map " + "\\proc\\self\\cwd " + source.replace("/", "\\"));
            } else {
                String usrHome = System.getProperty("user.home");
                LOG.info("Set settings set target.source-map: " + usrHome + "," + source);
                executeInterpreterCommand("settings set target.source-map " + usrHome + " " + source);
            }
            executeInterpreterCommand("settings append target.source-map " + "\"\"" + " " + source);
        }
    }

    private void setSymbols() throws ExecutionException {
        List<String> symbol = mParameters.getAndroidSymbolList();
        if (!symbol.isEmpty()) {
            List<String> searchPaths = Lists.newArrayListWithExpectedSize(symbol.size());

            for (String symDir : symbol) {
                searchPaths.add("\"" + symDir + "\"");
            }

            String searchPathsStr = StringUtil.join(searchPaths, " ");
            LOG.info("Set target.exec-search-paths: " + searchPathsStr);
            executeInterpreterCommand("settings set target.exec-search-paths " + searchPathsStr);
        }

        //executeConsoleCommand("log enable -f G:/lldb.txt lldb all");
    }

    private void commonLoad() throws ExecutionException {
        LOG.info("Loading driver");
        LOG.debug("Load startup scripts");
        executeInterpreterCommand("settings set target.process.thread.step-out-avoid-nodebug false");
        loadStartupScripts();
        loadJObjectPrettyPrinterScripts();
        LOG.debug("run console commands from environment");

        runStartupCommands();
        LOG.debug("connectPlatform");
        int attempts = 0;

        while(true) {
            ++attempts;

            try {
                connectPlatform();
                return;
            }catch (LLDBDriverException var4) {
                if (attempts >= 10) {
                    LOG.warn("Giving up making LLDB connection after 10 attempts");
                    throw var4;
                }

                LOG.warn("Failed to connect platform (attempt " + attempts + " of " + 10 + ") - retrying.  Error was: " + var4.getMessage());

                try {
                    Thread.sleep(500L);
                } catch (InterruptedException var3) {
                    Thread.currentThread().interrupt();
                    throw new ExecutionException("Interrupted");
                }
            }
        }
    }

    private void loadStartupScripts() throws ExecutionException {
        for(String script : mStartUpScripts){
            LOG.info("Loading startup script: " + script);
            executeInterpreterCommand("command source \"" + script + "\"");
        }
    }

    private void loadJObjectPrettyPrinterScripts() throws ExecutionException {
        File scriptPath = new File(mParameters.getDebugExePath() + "/shared/jobject_printers", "jstring_reader.py");
        LOG.info("Loading startup script: " + scriptPath);
        executeInterpreterCommand("command script import \"" + scriptPath + "\"");
        int level = mParameters.getApiLevel();
        executeInterpreterCommand(String.format("script jstring_reader.register(%d, %s)", level, mIsAndroidArt ? "False" : "True"));
    }

    private void runStartupCommands() throws ExecutionException {
        runCommands("Startup", myStartupCommands);
    }

    private void runPostAttachCommands() throws ExecutionException {
        runCommands("Post attach", myPostAttachCommands);
    }

    private void runCommands(@NotNull String name, @NotNull List<String> commands) throws ExecutionException {
        for(String cmd : commands){
            cmd = cmd.trim();
            if(!cmd.isEmpty()){
                LOG.info(String.format("%s command: \"%s\"", name, cmd));
                executeInterpreterCommand(cmd);
            }
        }
    }

    private void connectPlatform() throws ExecutionException {

        LOG.info("Connecting to LLDB server: " + mParameters.getRemote());
        ThrowIfNotValid<ProtocolResponses.ConnectPlatform_Res> responseHandler = new ThrowIfNotValid<>("Couldn't connect platform");
        Protocol.CompositeRequest connectPlatformReq = ProtobufMessageFactory.connectPlatform("remote-android", mParameters.getRemote());
        getProtobufClient().sendMessageAndWaitForReply(connectPlatformReq, ProtocolResponses.ConnectPlatform_Res.class, responseHandler);
        responseHandler.throwIfNeeded();
    }

    @NotNull
    private static String getWatchPointWhitelist() {
        return String.format("%s based devices/emulators, %s", WATCH_POINT_WHITELISTED_ABIS, WATCH_POINT_WHITELISTED_MODELS);
    }
}
