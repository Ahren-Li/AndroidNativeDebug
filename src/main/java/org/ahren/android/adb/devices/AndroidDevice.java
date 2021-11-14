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

import com.android.ddmlib.IDevice;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.devices.Abi;
import com.google.common.util.concurrent.ListenableFuture;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleColoredComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public interface AndroidDevice {

    boolean isRunning();

    boolean isVirtual();

    @NotNull
    AndroidVersion getVersion();

    int getDensity();

    @NotNull
    List<Abi> getAbis();

    @NotNull
    String getSerial();

    boolean supportsFeature(@NotNull IDevice.HardwareFeature feature);

    @NotNull
    String getName();

    boolean renderLabel(@NotNull SimpleColoredComponent component, boolean isCompatible, @Nullable String searchPrefix);

    void prepareToRenderLabel();

//    @NotNull
//    ListenableFuture<IDevice> launch(@NotNull Project var1);

//    @NotNull
//    ListenableFuture<IDevice> getLaunchedDevice();

//    @NotNull
//    LaunchCompatibility canRun(@NotNull AndroidVersion var1, @NotNull IAndroidTarget var2, @NotNull EnumSet<IDevice.HardwareFeature> var3, @Nullable Set<String> var4);

}
