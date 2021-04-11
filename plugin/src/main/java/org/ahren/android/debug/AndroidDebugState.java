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

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.jetbrains.cidr.execution.CidrLauncher;
import org.jetbrains.annotations.NotNull;

public class AndroidDebugState extends CommandLineState {

    private CidrLauncher mLauncher;

    public AndroidDebugState(CidrLauncher launcher, ExecutionEnvironment environment) {
        super(environment);
        mLauncher = launcher;
    }

    @NotNull
    XDebugProcess startDebugProcess(@NotNull XDebugSession session) throws ExecutionException {
        return mLauncher.startDebugProcess(this, session);
    }

    @NotNull
    @Override
    public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner programRunner) throws ExecutionException {
        DefaultExecutionResult result = (DefaultExecutionResult) super.execute(executor, programRunner);
        mLauncher.configureExecutionResult(this, result);
        return result;
    }

    @Override
    protected @NotNull ProcessHandler startProcess() throws ExecutionException {
        return mLauncher.startProcess(this);
    }

    @NotNull
    @SuppressWarnings("unused")
    public CidrLauncher getLauncher(){
        return mLauncher;
    }
}
