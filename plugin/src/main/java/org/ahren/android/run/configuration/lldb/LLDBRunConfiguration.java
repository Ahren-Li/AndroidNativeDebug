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

import com.android.ddmlib.IShellOutputReceiver;
import com.android.sdklib.SdkVersionInfo;
import com.android.sdklib.devices.Abi;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import org.ahren.android.adb.AdbProcess;
import org.ahren.android.adb.devices.ConnectedAndroidDevice;
import org.ahren.android.debug.*;
import org.ahren.android.ui.dialog.AndroidDeviceDialog;
import org.ahren.android.ui.dialog.AndroidProcessDialog;
import org.ahren.android.ui.editor.LLDBEditor;
import org.ahren.android.ui.editor.SymbolEditor;
import org.ahren.android.ui.model.AndroidListModel;
import org.ahren.android.utils.Android;
import org.ahren.android.utils.Configuration;
import org.ahren.android.utils.Log;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class LLDBRunConfiguration extends BaseRunConfiguration
        implements RunConfigurationWithSuppressedDefaultRunAction, IAndroidRunProfile {
    private final static Logger LOG = Log.factory("LLDBRunConfiguration");
    private final static boolean DEBUG = false;

    private List<String> mLLDBPaths;
    private String mCurrentLLDBPath;

    LLDBRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name, SymbolCreator.LLDB_TYPE);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        SettingsEditorGroup<LLDBRunConfiguration> group = new SettingsEditorGroup<>();
        group.addEditor("Config", new LLDBEditor(getProject()));
        group.addEditor("Symbol", new SymbolEditor<>(getProject(),this));
        return group;
    }

    @Nullable
    @Override
    public AndroidDebugState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) {
        boolean canRun = false;
        AndroidDebugParameters parameters = new AndroidDebugParameters(getParameters());
        String adbPath = parameters.getSdkPath() + Android.ADB_PATH;
        AndroidDeviceDialog dialog = new AndroidDeviceDialog(getProject(), adbPath);
        if(dialog.showAndGet()){
            canRun = true;
            ConnectedAndroidDevice device = dialog.getSelectDevice();
            if(DEBUG) {
                LOG.info("device:name=" + device.getName());
                LOG.info("device:getSerial=" + device.getSerial());
                LOG.info("device:isVirtual=" + device.isVirtual());
                List<Abi> abis = device.getAbis();
                for(Abi abi : abis){
                    LOG.info("device:abi=" + abi);
                }
                LOG.info("device:getDensity=" + device.getDensity());
                LOG.info("device:getVersion=" + SdkVersionInfo.getVersionString(device.getVersion().getApiLevel()));
            }

            if(device.getAbis().size() > 0){
                parameters.setArmAbi(device.getAbis().get(0));
            }
            parameters.setSerialNum(device.getSerial());
            parameters.setApiLevel(device.getVersion().getApiLevel());

            AndroidProcessDialog processDialog = new AndroidProcessDialog(getProject(), device);
            String processName = parameters.getProcessName();
            if(StringUtil.isEmpty(processName) || processDialog.needShow(processName)){
                if(processDialog.showAndGet()){
                    AdbProcess process = processDialog.getSelectProcess();
                    parameters.setProcessName(process.getName());
                    parameters.setProcessPid(process.getPid());
                }else{
                    canRun = false;
                }
            } else {
                try {
                    GetPidProcessListener l = new GetPidProcessListener();
                    device.getDevice().executeShellCommand("pidof " + processName , l);
                    l.flush();
                    int pid = l.getPid();
                    if (pid > 0) {
                        parameters.setProcessPid(pid);
                    } else {
                        canRun = false;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    canRun = false;
                }
            }
        }

        if(canRun){
            if(DEBUG) LOG.info(parameters.toString());
            return new AndroidDebugState(new AndroidLLDBLauncher(getProject(), parameters), executionEnvironment);
        }
        return null;
    }

    static class GetPidProcessListener implements IShellOutputReceiver {

        StringBuffer buffer = new StringBuffer();
        int pid = 0;

        public int getPid() {
            return pid;
        }

        @Override
        public void addOutput(byte[] bytes, int i, int i1) {
            buffer.append(new String(bytes, i, i1));
        }

        @Override
        public void flush() {
            try {
                StringReader reader = new StringReader(buffer.toString());
                BufferedReader buffer = new BufferedReader(reader);
                String line = buffer.readLine(); // read one line
                if (line.contains(" ")) { // If multi process with same name, then choose first one
                    line = line.split(" ")[0];
                }
                pid = Integer.parseInt(line);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        AndroidDebugParameters parameters = getParameters();
        String processName = element.getAttributeValue(Configuration.ANDROID_PROCESS_NAME);
        String sdkPath = element.getAttributeValue(Configuration.ANDROID_SDK_PATH);
        String ndkPath = element.getAttributeValue(Configuration.ANDROID_NDK_PATH);
//        String level = element.getAttributeValue(Configuration.ANDROID_LEVEL);
//        String abi = element.getAttributeValue(Configuration.ANDROID_ABI);

        parameters.setProcessName(processName);
        parameters.setSdkPath(sdkPath);
        parameters.setNdkPath(ndkPath);
//        parameters.setApiLevel(level);
//        parameters.setArmAbi(abi);

        mCurrentLLDBPath = element.getAttributeValue(Configuration.LLDB_PATH_SELECT);
        if(!StringUtil.isEmpty(mCurrentLLDBPath)){
            parameters.setDebugExePath(mCurrentLLDBPath);
        }
        String numS = element.getAttributeValue(Configuration.LLDB_PATH_NUM);
        if(!StringUtil.isEmpty(numS)){
            int num = Integer.parseInt(numS);
            mLLDBPaths = new ArrayList<>();
            for(int i = 0; i < num; i ++){
                String path = element.getAttributeValue(Configuration.LLDB_PATH_START + i);
                if(!StringUtil.isEmpty(path)){
                    File file = new File(path);
                    if(file.exists() && file.isDirectory()){
                        mLLDBPaths.add(path);
                    }
                }
            }
        }


        setParameters(parameters);
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        AndroidDebugParameters parameters = getParameters();

        String processName = parameters.getProcessName();
        String sdkPath = parameters.getSdkPath();
        String ndkPath = parameters.getNdkPath();

        if(DEBUG){
            LOG.info("processName:" + processName);
            LOG.info("sdkPath:" + sdkPath);
            LOG.info("ndkPath:" + ndkPath);
        }

        if(sdkPath != null){
            element.setAttribute(Configuration.ANDROID_SDK_PATH, sdkPath);
        }
        if(ndkPath != null){
            element.setAttribute(Configuration.ANDROID_NDK_PATH, ndkPath);
        }
//        if(!StringUtil.isEmpty(parameters.getApiLevel())){
//            element.setAttribute(Configuration.ANDROID_LEVEL, parameters.getApiLevel());
//        }
//        if(!StringUtil.isEmpty(parameters.getArmAbi())){
//            element.setAttribute(Configuration.ANDROID_ABI, parameters.getArmAbi());
//        }

        if(processName != null){
            element.setAttribute(Configuration.ANDROID_PROCESS_NAME, processName);
        }


        if(!StringUtil.isEmpty(mCurrentLLDBPath)){
            element.setAttribute(Configuration.LLDB_PATH_SELECT, mCurrentLLDBPath);
        }
        if(mLLDBPaths != null){
            element.setAttribute(Configuration.LLDB_PATH_NUM, mLLDBPaths.size() + "");
            for(int i = 0; i < mLLDBPaths.size(); i++){
                element.setAttribute(Configuration.LLDB_PATH_START + i, mLLDBPaths.get(i));
            }
        }
    }

    public void setLLDBPaths(List<String> mLLDBPaths) {
        this.mLLDBPaths = new ArrayList<>(mLLDBPaths);
    }

    public List<String> getLLDBPaths() {
        List<String> paths = new ArrayList<>();
        if(mLLDBPaths != null){
            paths.addAll(mLLDBPaths);
        }
        return paths;
    }

    public void setCurrentLLDBPath(@NotNull String mCurrentLLDBPath) {
        this.mCurrentLLDBPath = mCurrentLLDBPath;
//        getParameters().setDebugExePath(mCurrentLLDBPath);
    }

    public String getCurrentLLDBPath() {
        return mCurrentLLDBPath;
    }
}
