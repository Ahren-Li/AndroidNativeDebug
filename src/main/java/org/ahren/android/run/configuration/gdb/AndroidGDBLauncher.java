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

import org.ahren.android.debug.AndroidDebugParameters;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import com.jetbrains.cidr.execution.debugger.backend.gdb.GDBDriverConfiguration;
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains;
import com.jetbrains.cidr.execution.debugger.CidrDebugProcess;
import com.jetbrains.cidr.execution.CidrLauncher;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

public class AndroidGDBLauncher extends CidrLauncher {

    private Project mProject;
    private CPPToolchains.Toolchain mToolchain;
    private AndroidDebugParameters mParameters;

    AndroidGDBLauncher(Project project, CPPToolchains.Toolchain toolchain,@NotNull AndroidDebugParameters parameters){
        super();
        mProject = project;
        mToolchain = toolchain;
        mParameters = parameters;

    }

    @Override
    protected ProcessHandler createProcess(@NotNull CommandLineState commandLineState) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected @NotNull CidrDebugProcess createDebugProcess(@NotNull CommandLineState commandLineState, @NotNull XDebugSession xDebugSession) throws ExecutionException {
//        String problem = CLionRemoteRunConfigurationKt.getRemoteRunToolchainProblem(mToolchain, false);

        GDBDriverConfiguration gdbdriverconfiguration = new AndroidGDBDriverConfiguration(mParameters, getProject(), mToolchain);
        TextConsoleBuilder builder = commandLineState.getConsoleBuilder();


        Intrinsics.checkExpressionValueIsNotNull(builder, "state.consoleBuilder");

        AndroidGDBDebugProcess process = new AndroidGDBDebugProcess(gdbdriverconfiguration, mParameters, xDebugSession, builder);
        configProcessHandler(process.getProcessHandler(), process.isDetachDefault(), false, getProject());

        return process;
    }

    @Override
    public @NotNull Project getProject() {
        return mProject;
    }
}
