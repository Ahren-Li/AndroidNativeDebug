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

package org.ahren.android.run.configuration.gdb;

import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.cidr.ArchitectureType;
import org.ahren.android.debug.AndroidDebugParameters;
import org.ahren.android.utils.Log;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.BaseProcessHandler;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.execution.debugger.backend.gdb.GDBDriverConfiguration;
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AndroidGDBDriverConfiguration extends GDBDriverConfiguration {

    private final boolean DEBUG = true;
    private final Logger LOG = Log.factory("AndroidGDBDriverConfiguration");
    private final AndroidDebugParameters mParameters;

    AndroidGDBDriverConfiguration(@NotNull AndroidDebugParameters parameters, @NotNull Project project, @Nullable CPPToolchains.Toolchain toolchain) {
        super();
        mParameters = parameters;
    }

    @NotNull
    @Override
    public BaseProcessHandler createDebugProcessHandler(@NotNull GeneralCommandLine generalCommandLine) throws ExecutionException {
        BaseProcessHandler handler = super.createDebugProcessHandler(generalCommandLine);
        if(DEBUG) LOG.info("createDebugProcessHandler:Charset=" + handler.getCharset());
        return handler;
    }

    @NotNull
    @Override
    public GeneralCommandLine createDriverCommandLine(@NotNull DebuggerDriver debuggerDriver, @NotNull ArchitectureType var2) throws ExecutionException {
        GeneralCommandLine line = super.createDriverCommandLine(debuggerDriver, var2);
        if(DEBUG) LOG.info("createDriverCommandLine:Charset=" + line.getCharset());
        return line;
    }


    @NotNull
    @Override
    public DebuggerDriver createDriver(@NotNull DebuggerDriver.Handler handler, @NotNull ArchitectureType architectureType) throws ExecutionException {
        return new AndroidGDBDriver(mParameters, handler, architectureType, this);
    }
}
