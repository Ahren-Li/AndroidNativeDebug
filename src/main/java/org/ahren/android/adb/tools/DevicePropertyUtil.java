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

package org.ahren.android.adb.tools;

import com.android.ddmlib.IDevice;
import com.google.common.collect.ImmutableSet;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;

public class DevicePropertyUtil {
    private static final Set<String> ourManufacturerNameIsAcronym = ImmutableSet.of("ASUS", "HTC", "LG", "LGE", "ZTE");


    static String fixManufacturerName(@NotNull String manufacturer) {
        String allCaps = manufacturer.toUpperCase(Locale.US);
        return ourManufacturerNameIsAcronym.contains(allCaps) ? allCaps : StringUtil.capitalizeWords(manufacturer, true);
    }

    @NotNull
    public static String getManufacturer(@NotNull IDevice d, @NotNull String unknown) {
        return getManufacturer(d.getProperty("ro.product.manufacturer"), d.isEmulator(), unknown);
    }

    @NotNull
    public static String getManufacturer(@Nullable String manufacturer, boolean isEmulator, @NotNull String unknown) {
        if (isEmulator && "unknown".equals(manufacturer)) {
            manufacturer = unknown;
        }

        return manufacturer != null ? fixManufacturerName(manufacturer) : unknown;
    }

    @NotNull
    public static String getModel(@NotNull IDevice d, @NotNull String unknown) {
        return getModel(d.getProperty("ro.product.model"), unknown);
    }

    @NotNull
    public static String getModel(@Nullable String model, @NotNull String unknown) {
        return model != null ? StringUtil.capitalizeWords(model, true) : unknown;
    }

    @NotNull
    public static String getBuild(@NotNull IDevice d) {
        return getBuild(d.getProperty("ro.build.version.release"), d.getProperty("ro.build.version.sdk"));
    }

    @NotNull
    public static String getBuild(@Nullable String buildVersion, @Nullable String apiLevel) {
        StringBuilder sb = new StringBuilder(20);
        if (buildVersion != null) {
            sb.append("Android ");
            sb.append(buildVersion);
        }

        if (apiLevel != null) {
            sb.append(String.format(", API %1$s", apiLevel));
        }

        return sb.toString();
    }
}
