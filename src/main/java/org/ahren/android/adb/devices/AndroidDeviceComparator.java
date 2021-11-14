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

import com.android.sdklib.AndroidVersion;

import java.util.Comparator;

public class AndroidDeviceComparator implements Comparator<AndroidDevice> {

    @Override
    public int compare(AndroidDevice device1, AndroidDevice device2) {
        if (device1.isRunning() != device2.isRunning()) {
            return device1.isRunning() ? -1 : 1;
        } else {
            AndroidVersion version1 = device1.getVersion();
            AndroidVersion version2 = device2.getVersion();
            if (!version1.equals(version2)) {
                return version2.compareTo(version1);
            } else if (device1.isVirtual() != device2.isVirtual()) {
                return device1.isVirtual() ? 1 : -1;
            } else {
                return device1.getSerial().compareTo(device2.getSerial());
            }
        }
    }
}
