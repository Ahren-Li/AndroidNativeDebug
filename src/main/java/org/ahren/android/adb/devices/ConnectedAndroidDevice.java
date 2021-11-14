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

package org.ahren.android.adb.devices;

import com.android.ddmlib.*;
import com.intellij.openapi.diagnostic.Logger;
import org.ahren.android.adb.tools.DevicePropertyUtil;
import org.ahren.android.ui.icons.AndroidIcons;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.devices.Abi;
import com.google.common.collect.ImmutableList;
import com.intellij.ide.ui.search.SearchUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import org.ahren.android.utils.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConnectedAndroidDevice implements AndroidDevice {

    private final static Logger LOG = Log.factory("ConnectedAndroidDevice");
    private final static boolean DEBUG = true;

    @NotNull
    private final IDevice mDevice;
    private volatile String mDeviceManufacturer;
    private volatile String mDeviceBuild;
    private volatile String mDeviceModel;
    private volatile boolean mIsRoot = false;

    public ConnectedAndroidDevice(@NotNull IDevice device) {
        this.mDevice = device;
        try {
            mIsRoot = device.isRoot();
        } catch (Exception e) {
            e.printStackTrace();
            mIsRoot = false;
        }
    }

    public IDevice getDevice() {
        return mDevice;
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public boolean isVirtual() {
        return mDevice.isEmulator();
    }

    @Override
    public @NotNull AndroidVersion getVersion() {
        return mDevice.getVersion();
    }

    @Override
    public int getDensity() {
        return mDevice.getDensity();
    }

    public boolean isRoot() {
        return mIsRoot;
    }

    public boolean doRoot(){
        try {
            return mDevice.root();
        }catch (AdbCommandRejectedException ignore) {
        }catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean pushFile(String src, String dts){
        try {
            mDevice.pushFile(src, dts);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    public boolean execCmd(String cmd, IShellOutputReceiver receiver){
        try {
            mDevice.executeShellCommand(cmd, receiver);
            return true;
        }catch (ShellCommandUnresponsiveException ignore){
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public @NotNull List<Abi> getAbis() {
        List<String> abis = mDevice.getAbis();
        ImmutableList.Builder<Abi> builder = ImmutableList.builder();

        for(String abi : abis){
            Abi a = Abi.getEnum(abi);
            builder.add(a);
        }

        return builder.build();
    }

    @Override
    public @NotNull String getSerial() {
        if(mDevice.isEmulator()){
            String avdName = mDevice.getAvdName();
            if(avdName != null) return avdName;
        }
        return mDevice.getSerialNumber();
    }

    @Override
    public boolean supportsFeature(IDevice.@NotNull HardwareFeature feature) {
        return mDevice.supportsFeature(feature);
    }

    @Override
    public @NotNull String getName() {
        return getDeviceName();
    }

    @Override
    public boolean renderLabel(@NotNull SimpleColoredComponent renderer, boolean isCompatible, @Nullable String searchPrefix) {

        renderer.setIcon(mDevice.isEmulator() ? AndroidIcons.Ddms.Emulator : AndroidIcons.Ddms.RealDevice);
        IDevice.DeviceState state = mDevice.getState();
        if(state != IDevice.DeviceState.ONLINE){
            StringBuilder buf = new StringBuilder();
            buf.append(String.format("%1$s [%2$s", mDevice.getSerialNumber(), state));
            if (state == IDevice.DeviceState.UNAUTHORIZED) {
                buf.append(" - Press 'OK' in the 'Allow USB Debugging' dialog on your device");
            }

            buf.append("] ");
            renderer.append(buf.toString(), SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES);
            return true;
        }else if( mDeviceManufacturer != null && mDeviceModel != null && mDeviceBuild != null){
            SimpleTextAttributes attr = isCompatible ? SimpleTextAttributes.REGULAR_ATTRIBUTES : SimpleTextAttributes.GRAY_ATTRIBUTES;
            String name = this.getName();
            if (name.isEmpty()) {
                name = "Unknown";
            }

            SearchUtil.appendFragments(searchPrefix, name, attr.getStyle(), attr.getFgColor(), attr.getBgColor(), renderer);
            renderer.append(": " + getSerial(), SimpleTextAttributes.GRAY_ATTRIBUTES);
            String build = this.getDeviceBuild();
            if (!build.isEmpty()) {
                renderer.append(" (" + build + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
            }

            return true;
        }else{
            renderer.append("...");
            return false;
        }
    }

    @Override
    public void prepareToRenderLabel() {
        assert !isDispatchThread();

        getDeviceManufacturer();
        getDeviceModel();
        getDeviceBuild();
    }

    public boolean isRenderLabelOk(){
        return mDeviceManufacturer != null && mDeviceModel != null && mDeviceBuild != null;
    }

    @NotNull
    private String getDeviceName() {
        StringBuilder name = new StringBuilder(20);
        name.append(this.getDeviceManufacturer());
        if (name.length() > 0) {
            name.append(' ');
        }

        name.append(this.getDeviceModel());
        return name.toString();
    }

    @NotNull
    private String getDeviceManufacturer() {
        if (mDeviceManufacturer == null) {
            assert !isDispatchThread();

            mDeviceManufacturer = DevicePropertyUtil.getManufacturer(mDevice, "");
        }

        return mDeviceManufacturer;
    }

    @NotNull
    private String getDeviceModel() {
        if (this.mDeviceModel == null) {
            assert !isDispatchThread();

            mDeviceModel = DevicePropertyUtil.getModel(mDevice, "");
        }

        return mDeviceModel;
    }

    @NotNull
    private String getDeviceBuild() {
        if (this.mDeviceBuild == null) {
            assert !isDispatchThread();

            mDeviceBuild = DevicePropertyUtil.getBuild(mDevice);
        }

        return mDeviceBuild;
    }

    private boolean isDispatchThread() {
        Application application = ApplicationManager.getApplication();
        return application != null && application.isDispatchThread();
    }
}
