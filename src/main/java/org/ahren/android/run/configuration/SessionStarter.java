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

package org.ahren.android.run.configuration;


import com.android.ddmlib.SyncService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ConcurrencyUtil;
import org.ahren.android.adb.devices.AdbDeviceProxy;
import org.ahren.android.debug.AndroidDebugParameters;
import org.ahren.android.run.configuration.lldb.LLDBTools;
import org.ahren.android.utils.ProgressReporter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SessionStarter {
    private static final Logger LOG = Logger.getInstance(SessionStarter.class);

    private AndroidDebugParameters mParameters;
    private AdbDeviceProxy mAdbProxy;
    private ProgressReporter mProgressReporter;
    private File mLocalServerFile;
    private File mLocalStartScriptFile;
    private String mLLDBPath;

    public SessionStarter(@NotNull AndroidDebugParameters parameter, @NotNull AdbDeviceProxy proxy, @NotNull ProgressReporter progressReporter){
        mParameters = parameter;
        mAdbProxy = proxy;
        mProgressReporter = progressReporter;
        mLocalServerFile = new File(LLDBTools.getLLDB_server(mParameters.getDebugExePath(), mAdbProxy.getDevice()));
        mLocalStartScriptFile = new File(mParameters.getDebugExePath() + File.separator + "android" + File.separator + "start_lldb_server.sh");
        mLLDBPath = Constants.DEVICE_TEMP_PATH + "/lldb";
    }

    public void startServer() {
        mProgressReporter.step("Starting LLDB server");

        makeDir(mLLDBPath + "/log");
        makeDir(mLLDBPath + "/tmp");

        String cmd = Constants.DEVICE_TEMP_PATH + "/" + mLocalStartScriptFile.getName();
        cmd += " " + mLLDBPath;
        cmd += " unix-abstract";
        cmd += " /sdcard";
        cmd += " debug.sock";
        cmd += " \"lldb process:gdb-remote packets\"";
        LOG.info("Starting LLDB server : " + cmd);

        final String cmdF = cmd;
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            ConcurrencyUtil.runUnderThreadName("lldb-server executor", () -> {
                try {
                    StringBuffer buffer = new StringBuffer();
                    AdbDeviceProxy.ShellOutputReceiver receiver = new AdbDeviceProxy.ShellOutputReceiver(buffer);
                    mAdbProxy.getDevice().getDevice().executeShellCommand(cmdF, receiver, 0L, TimeUnit.DAYS);
                    LOG.info("LLDB server has exited:" + buffer.toString());
                } catch (Exception var3) {
                    LOG.warn("LLDB server has failed: ", var3);
                }

            });
        });
    }



    public boolean pushFilesToDevice(){
        mProgressReporter.step("Pushing files to device");
        makeDir(mLLDBPath);
        makeDir(mLLDBPath + "/bin");

        return pushDebuggerFile("lldb/bin", mLocalServerFile) && pushDebuggerFile("", mLocalStartScriptFile);
    }

    private boolean pushDebuggerFile(String child, @NotNull File localFile) {

        try {
            String tmpDestFile = Constants.DEVICE_TEMP_PATH + "/";
            if(child != null && !child.isEmpty()){
                tmpDestFile += child + "/";
            }
            tmpDestFile += localFile.getName();
            SyncService syncService = mAdbProxy.getDevice().getDevice().getSyncService();
            Date localFileLmt = new Date(localFile.lastModified() / 1000L * 1000L);
            SyncService.FileStat destFileStat = syncService.statFile(tmpDestFile);
            if (destFileStat != null && destFileStat.getMode() != 0 && localFileLmt.equals(destFileStat.getLastModified()) && localFile.length() == (long)destFileStat.getSize()) {
                LOG.info("Remote file " + tmpDestFile + " is up-to-date.");
            } else {
                LOG.info("Pushing to the device: " + localFile + " => " + tmpDestFile);
                mAdbProxy.pushFile(localFile.getAbsolutePath(), tmpDestFile);
                mAdbProxy.exec("chmod 755 " + tmpDestFile);
            }

            return true;
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    private boolean makeDir(String dir){
        try {
            SyncService syncService = mAdbProxy.getDevice().getDevice().getSyncService();
            SyncService.FileStat destFileStat = syncService.statFile(dir);
            if(destFileStat != null){
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return mAdbProxy.exec("mkdir " + dir);
    }
}
