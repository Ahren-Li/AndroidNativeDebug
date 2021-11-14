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

import com.intellij.util.system.CpuArch;
import com.jetbrains.cidr.ArchitectureType;
import com.jetbrains.cidr.execution.debugger.backend.lldb.LLDBDriverConfiguration;
import org.ahren.android.debug.AndroidDebugParameters;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.BaseProcessHandler;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import com.jetbrains.cidr.execution.debugger.CidrDebuggerPathManager;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import com.jetbrains.cidr.execution.debugger.backend.lldb.LLDBDriver;
import org.ahren.android.utils.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

public class AndroidLLDBDriverConfiguration extends LLDBDriverConfiguration {

    private static final Logger LOG = Log.factory("AndroidLLDBDriverConfiguration");

    private final File myFrameworkFile;
    private final String mLLDBPath;
    private final String mNDKPath;

    private final AndroidDebugParameters mParameters;
    AndroidLLDBDriverConfiguration(@NotNull AndroidDebugParameters parameters) {
        super();
        mParameters = parameters;
        mLLDBPath = parameters.getDebugExePath();
        mNDKPath = parameters.getNdkPath();
        myFrameworkFile = findFrameworkFile();
        LOG.info("LLDB framework file: " + myFrameworkFile.getAbsolutePath());
        LOG.info("mLLDBPath: " + mLLDBPath);
    }

    @NotNull
    @Override
    public BaseProcessHandler createDebugProcessHandler(@NotNull GeneralCommandLine generalCommandLine) throws ExecutionException {
        BaseProcessHandler handler = super.createDebugProcessHandler(generalCommandLine);
        LOG.info("createDebugProcessHandler:Charset=" + handler.getCharset());
        return handler;
    }

    @NotNull
    @Override
    public LLDBDriver createDriver(@NotNull DebuggerDriver.Handler handler, @NotNull ArchitectureType architectureType) throws ExecutionException {
        return new AndroidLLDBDriver(mParameters, handler, architectureType, this);
    }

    @Override
    public boolean isStaticVarsLoadingEnabled() {
        return true;
    }

    @Override
    protected void configureDriverCommandLine(@NotNull GeneralCommandLine result) {

        Map<String, String> environment = result.getEnvironment();

        File galaPath = ModulePathManager.getRepoLLDBStlPrintersFolder();
        if (galaPath.exists()) {
            environment.put("AS_GALA_PATH", galaPath.getAbsolutePath());
        }

        if (mNDKPath != null) {
            File libStdCxxPrinterPath = getLibStdCxxPrintersPath(new File(mNDKPath), "4.9");
            if (libStdCxxPrinterPath.exists()) {
                environment.put("AS_LIBSTDCXX_PRINTER_PATH", libStdCxxPrinterPath.getAbsolutePath());
            }
        }


    }

    @NotNull
    @Override
    public GeneralCommandLine createDriverCommandLine(@NotNull DebuggerDriver driver, @NotNull ArchitectureType architectureType) throws ExecutionException {

        File lldbFrameworkFile = new File(mLLDBPath);
        if (!lldbFrameworkFile.exists()) {
            throw new ExecutionException(lldbFrameworkFile + " not found");
        } else {
            File frontendExecutable = this.getLLDBBinFile(SystemInfo.isWindows ? "LLDBFrontend.exe" : "LLDBFrontend");
            if (!frontendExecutable.exists()) {
                throw new ExecutionException(frontendExecutable.getAbsolutePath() + " not found");
            } else {
                GeneralCommandLine result = new GeneralCommandLine();
                result.setExePath(frontendExecutable.getAbsolutePath());
                result.addParameter(String.valueOf(((LLDBDriver)driver).getPort()));
                setupCommonParameters(result);
                Map<String, String> env = result.getEnvironment();
                env.put("ANDROID_SERIAL", mParameters.getSerialNum());
                if (SystemInfo.isLinux) {
                    env.put("LD_LIBRARY_PATH", getLLDBLibDir().getAbsolutePath());
                } else if (SystemInfo.isMac) {
                    env.put("DYLD_FRAMEWORK_PATH", lldbFrameworkFile.getParent());
                    File pythonBinDir = CidrDebuggerPathManager.getOSXSystemPythonBinDir();
                    if (pythonBinDir != null) {
                        env.put("PATH", pythonBinDir.getAbsolutePath());
                    }
                }

                if (!SystemInfo.isMac) {
                    env.put("PYTHONHOME", getLLDBLibDir().getParent());
                    env.put("PYTHONDONTWRITEBYTECODE", "1");
                }

                File dir = getMinidumpDir();
                if (dir != null) {
                    env.put("LLDBFRONTEND_DUMPDIR", dir.getAbsolutePath());
                }
                this.configureDriverCommandLine(result);
                LOG.info(",createDriverCommandLine:Charset=" + result.getCharset());
                return result;
            }
        }
    }

    @NotNull
    private File getLLDBPlatformBinFile(@NotNull String relativePath) {
        return new File(mLLDBPath, relativePath);
    }

    @NotNull
    protected File getLLDBBinFile(@NotNull String relativePath) {
        if(relativePath.contains("bin")){
            return getLLDBPlatformBinFile(relativePath);
        }
        return getLLDBPlatformBinFile((new File("bin", relativePath)).getPath());
    }

    @NotNull
    private File getLLDBLibDir() {
        return getLLDBPlatformBinFile((new File("lib")).getPath());
    }

    @NotNull
    private File findFrameworkFile() {
        if (SystemInfo.isWindows) {
            return this.getLLDBBinFile("liblldb.dll");
        } else if (SystemInfo.isLinux) {
            return this.findLinuxFrameworkFile();
        } else if (SystemInfo.isMac) {
            return this.getLLDBPlatformBinFile("LLDB.framework");
        } else {
            throw new RuntimeException("Unsupported platform");
        }
    }

    @NotNull
    private File findLinuxFrameworkFile() {
        File libDir = getLLDBLibDir();
        File[] libFiles = libDir.listFiles(pathname -> pathname.getName().startsWith("liblldb.so"));
        if (libFiles != null && libFiles.length > 0) {
            return libFiles[0];
        } else {
            throw new RuntimeException("Found broken Linux LLDB FrameworkFile");
        }
    }

    @Nullable
    private File getMinidumpDir() {
        File folder = new File(PathManager.getTempPath(), "lldb");
        if (folder.exists()) {
            if (!folder.isDirectory()) {
                return null;
            }
        } else if (!folder.mkdir()) {
            return null;
        }

        return folder;
    }

    @NotNull
    private File getLibStdCxxPrintersPath(@NotNull File ndkRoot, @NotNull String gccVersion) {
        File prebuilt = new File(ndkRoot, "prebuilt");
        File hostPlatform = new File(prebuilt, getHostPlatformString());
        File share = new File(hostPlatform, "share");
        File prettyPrinters = new File(share, "pretty-printers");
        File libStdCxx = new File(prettyPrinters, "libstdcxx");
        return new File(libStdCxx, "gcc-" + gccVersion);
    }

    @NotNull
    private static String getHostPlatformString() {
        String platformString;
        if (SystemInfo.isLinux) {
            platformString = "linux";
        } else if (SystemInfo.isWindows) {
            platformString = "windows";
        } else {
            if (!SystemInfo.isMac) {
                return "UNKNOWN";
            }

            platformString = "darwin";
        }

        platformString = platformString + "-";
        if (CpuArch.CURRENT.width == 64) {
            platformString = platformString + "x86_64";
        } else {
            if (!(CpuArch.CURRENT.width == 32)) {
                return "UNKNOWN";
            }

            platformString = platformString + "x86";
        }

        return platformString;
    }
}
