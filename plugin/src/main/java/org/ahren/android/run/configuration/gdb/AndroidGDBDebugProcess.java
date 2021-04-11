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
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.xdebugger.XDebugSession;
import com.jetbrains.cidr.execution.debugger.CidrDebugProcess;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.jetbrains.cidr.execution.debugger.backend.gdb.GDBDriver;
import com.jetbrains.cidr.execution.debugger.remote.CidrRemoteGDBDebugProcessKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AndroidGDBDebugProcess extends CidrDebugProcess {

    private AndroidDebugParameters mParameters;

    AndroidGDBDebugProcess(DebuggerDriverConfiguration configuration,
                           @NotNull AndroidDebugParameters parameters,
                           @NotNull XDebugSession session,
                           @NotNull TextConsoleBuilder consoleBuilder) throws ExecutionException {

        super(CidrRemoteGDBDebugProcessKt.createParams(configuration), session, consoleBuilder);
        mParameters = parameters;

    }

    @NotNull
    @Override
    protected DebuggerDriver.Inferior doLoadTarget(@NotNull DebuggerDriver debuggerDriver) throws ExecutionException {
        Intrinsics.checkNotNullParameter(debuggerDriver, "driver");
        GDBDriver driver = (GDBDriver) debuggerDriver;
        DebuggerDriver.Inferior inferior = driver.loadForRemote(mParameters.getRemote(), null, null, new ArrayList<>());

        Intrinsics.checkExpressionValueIsNotNull(inferior, "(driver as GDBDriver).lo\u2026ters.driverPathMapping())");
        return inferior;
    }

    @Override
    public boolean isDetachDefault() {
        return true;
    }


}
