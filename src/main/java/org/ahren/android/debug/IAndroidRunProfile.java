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

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.jetbrains.cidr.execution.debugger.CidrDebugProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IAndroidRunProfile extends RunProfile, CidrDebugProfile {

    @Nullable
    AndroidDebugState getState(@NotNull Executor paramExecutor, @NotNull ExecutionEnvironment paramExecutionEnvironment)
            throws ExecutionException;

}
