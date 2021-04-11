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

package org.ahren.android.debug;

import com.android.sdklib.devices.Abi;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AndroidDebugParameters {

    private Abi    mArmAbi;
    private String mRemote;
    private String mSdkPath;
    private String mNdkPath;
    private int    mApiLevel;
    private String mSerialNum;
    private int    mProcessPid;
    private boolean mAdbCanRoot;
    private String mProcessName;
    private String mAndroidPath;
    private String mDebugExePath;
    private String mAndroidLaunch;
    private List<String> mAndroidSymbolList;

    public AndroidDebugParameters(AndroidDebugParameters parameters){
        mArmAbi        = parameters.mArmAbi;
        mRemote        = parameters.mRemote;
        mSdkPath       = parameters.mSdkPath;
        mNdkPath       = parameters.mNdkPath;
        mApiLevel      = parameters.mApiLevel;
        mSerialNum     = parameters.mSerialNum;
        mAdbCanRoot    = parameters.mAdbCanRoot;
        mProcessPid    = parameters.mProcessPid;
        mProcessName   = parameters.mProcessName;
        mAndroidPath   = parameters.mAndroidPath;
        mDebugExePath  = parameters.mDebugExePath;
        mAndroidLaunch = parameters.mAndroidLaunch;
        mAndroidSymbolList = new ArrayList<>(parameters.mAndroidSymbolList);
    }

    public String getProcessName() {
        return mProcessName;
    }

    public void setProcessName(String mProcessName) {
        this.mProcessName = mProcessName;
    }

    public int getProcessPid() {
        return mProcessPid;
    }

    public void setProcessPid(int processPid) {
        this.mProcessPid = processPid;
    }

    public Abi getArmAbi() {
        return mArmAbi;
    }

    public void setArmAbi(Abi mArmAbi) {
        this.mArmAbi = mArmAbi;
    }

    public String getSdkPath() {
        return mSdkPath;
    }

    public void setSdkPath(String sdkPath) {
        this.mSdkPath = sdkPath;
    }

    public String getNdkPath() {
        return mNdkPath;
    }

    public void setNdkPath(String mNdkPath) {
        this.mNdkPath = mNdkPath;
    }

    public int getApiLevel() {
        return mApiLevel;
    }

    public void setApiLevel(int mApiLevel) {
        this.mApiLevel = mApiLevel;
    }

    public AndroidDebugParameters(){
    }

    public String getRemote() {
        return mRemote;
    }

    public void setRemote(String remote) {
        this.mRemote = remote;
    }

    public String getAndroidPath() {
        return mAndroidPath;
    }

    public void setAndroidPath(String androidPath) {
        this.mAndroidPath = androidPath;
    }

    public String getAndroidLaunch() {
        return mAndroidLaunch;
    }

    public void setAndroidLaunch(String launch) {
        this.mAndroidLaunch = launch;
    }

    public void setDebugExePath(String exePath) {
        this.mDebugExePath = exePath;
    }

    public String getDebugExePath() {
        return mDebugExePath;
    }

    public String getSerialNum() {
        return mSerialNum;
    }

    public void setSerialNum(String serialNum) {
        this.mSerialNum = serialNum;
    }

    public void setAdbCanRoot(boolean adbCanRoot) {
        this.mAdbCanRoot = adbCanRoot;
    }

    public boolean isAdbCanRoot() {
        return mAdbCanRoot;
    }

    public void setAndroidSymbolList(@NotNull List<String> androidSymbolList) {
        this.mAndroidSymbolList = new ArrayList<>(androidSymbolList);
    }

    public ArrayList<String> getAndroidSymbolList() {
        return new ArrayList<>(mAndroidSymbolList);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("parameter:\n");
        builder.append("Android Remote:").append(mRemote).append("\n");
        builder.append("Android Source Path:").append(mAndroidPath).append("\n");
        builder.append("Android SDK Path:").append(mSdkPath).append("\n");
        builder.append("Android NDK Path:").append(mNdkPath).append("\n");
        builder.append("Android launcher:").append(mAndroidLaunch).append("\n");
        builder.append("Android ABI:").append(mArmAbi).append("\n");
        builder.append("Android Version:").append(mApiLevel).append("\n");
        builder.append("Android Process:").append(mProcessName).append("\n");
        builder.append("Android PID:").append(mProcessPid).append("\n");
        builder.append("Android Debug Exec:").append(mDebugExePath).append("\n");
        builder.append("Android Symbol Path:\n");
        for(String sym : mAndroidSymbolList){
            builder.append(sym).append("\n");
        }

        return builder.toString();
    }
}
