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

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.PluginPathManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ModulePathManager {

    @NotNull
    public static File getRepoLLDBBinFile(@NotNull String relativePath) {
//        LOG.info("PathManager.getBinPath();=" + PathManager.getBinPath());
        File lldbBin = new File(PathManager.getBinPath() + "/lldb");
        File file = new File(lldbBin, relativePath);
        return file.exists() ? file : new File(new File(PathManager.getHomePath(), "../vendor/google/android-ndk/bin/lldb"), relativePath);
    }

    @NotNull
    public static File getRepoLLDBSharedBinFile(@NotNull String relativePath) {
        return getRepoLLDBBinFile((new File("shared", relativePath)).getPath());
    }

    @NotNull
    public static File getRepoLLDBStlPrintersFolder() {
        return getRepoLLDBSharedBinFile("stl_printers");
    }

    @NotNull
    public static File getRepoLLDBStlPrintersBinFile(@NotNull String relativePath) {
        return new File(getRepoLLDBStlPrintersFolder(), relativePath);
    }

    @NotNull
    public static File getRepoLLDBPrettyPrinterScriptsFolder() {
        return getRepoLLDBSharedBinFile("jobject_printers");
    }

}
