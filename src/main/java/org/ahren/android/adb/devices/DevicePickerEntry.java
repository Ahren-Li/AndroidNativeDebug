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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DevicePickerEntry {

    public static final DevicePickerEntry CONNECTED_DEVICES_MARKER = new DevicePickerEntry(null, "Connected Devices");
    public static final DevicePickerEntry LAUNCHABLE_DEVICES_MARKER = new DevicePickerEntry(null, "Available Virtual Devices");
    public static final DevicePickerEntry NONE = new DevicePickerEntry(null, "   <none>");
    private final AndroidDevice mAndroidDevice;
    private final String mMarker;

    private DevicePickerEntry(@Nullable AndroidDevice androidDevice, @Nullable String marker) {
        this.mAndroidDevice = androidDevice;
        this.mMarker = marker;
    }

    public boolean isMarker() {
        return mMarker != null;
    }

    @Nullable
    public String getMarker() {
        return mMarker;
    }

    @Nullable
    public AndroidDevice getAndroidDevice() {
        return mAndroidDevice;
    }

    public static DevicePickerEntry create(@NotNull AndroidDevice device) {
        return new DevicePickerEntry(device, null);
    }

}
