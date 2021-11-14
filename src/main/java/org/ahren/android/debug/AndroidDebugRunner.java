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
import com.intellij.execution.configurations.RunConfigurationWithSuppressedDefaultDebugAction;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.console.ConsoleViewWrapperBase;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebugSessionListener;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.impl.XDebugProcessConfiguratorStarter;
import com.intellij.xdebugger.impl.ui.XDebugSessionData;
import com.jetbrains.cidr.execution.CidrRunner;
import org.ahren.android.utils.AndroidBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AndroidDebugRunner extends CidrRunner {
    @NotNull
    @Override
    public String getRunnerId() {
        return AndroidBundle.message("runner.id");
    }

    @Override
    public boolean canRun(@NotNull String s, @NotNull RunProfile runProfile) {
        if(!DefaultDebugExecutor.EXECUTOR_ID.equals(s) && !DefaultRunExecutor.EXECUTOR_ID.equals(s)){
            return false;
        }

        if(runProfile instanceof RunConfigurationWithSuppressedDefaultDebugAction){
            if(DefaultDebugExecutor.EXECUTOR_ID.equals(s)){
                return false;
            }
        }else if(runProfile instanceof RunConfigurationWithSuppressedDefaultRunAction){
            if(DefaultRunExecutor.EXECUTOR_ID.equals(s)){
                return false;
            }
        }

        return runProfile instanceof IAndroidRunProfile;
    }

    @Nullable
    @Override
    protected RunContentDescriptor doExecute(@NotNull RunProfileState runProfileState, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        if(DefaultDebugExecutor.EXECUTOR_ID.equals(executionEnvironment.getExecutor().getId())){
            return startDebugSession((AndroidDebugState) runProfileState, executionEnvironment, false).getRunContentDescriptor();
        }
        return super.doExecute(runProfileState, executionEnvironment);
    }

    private XDebugSession startDebugSession(AndroidDebugState state, ExecutionEnvironment env,
                                            final boolean muteBreakpoints, final XDebugSessionListener... listeners) throws ExecutionException {
        XDebuggerManager manager = XDebuggerManager.getInstance(env.getProject());
        XDebugSession session = manager.startSession(env, new XDebugProcessConfiguratorStarter() {
            @Override
            public void configure(XDebugSessionData xDebugSessionData) {
                if(muteBreakpoints){
                    xDebugSessionData.setBreakpointsMuted(true);
                }
            }

            @NotNull
            @Override
            public XDebugProcess start(@NotNull XDebugSession xDebugSession) throws ExecutionException {

                for(XDebugSessionListener listener : listeners){
                    xDebugSession.addSessionListener(listener);
                }

                return state.startDebugProcess(xDebugSession);
            }
        });

        initXDebugSession(session);
        return session;
    }

    private void initXDebugSession(XDebugSession session){
        if(session != null){
            ConsoleView view = session.getConsoleView();
            if(view instanceof ConsoleViewWrapperBase){
                RunnerLayoutUi ui = session.getUI();
                if(ui != null){
                    ((ConsoleViewWrapperBase) view).buildUi(ui);
                }
            }
        }
    }
}
