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

package org.ahren.android.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ProgressReporter {
    private static final Logger LOG = Log.factory("ProgressReporter");
    private final BlockingQueue<StepContext> myStepQueue = new LinkedBlockingDeque<>();
    private boolean myFinished = false;

    private synchronized void putStep(@NotNull ProgressReporter.StepContext stepContext) {
        if (!this.myFinished) {
            try {
                this.myStepQueue.put(stepContext);
            } catch (InterruptedException var3) {
                LOG.error(var3);
            }

            this.myFinished = stepContext.isFinished();
        }
    }

    private void runProgressTask(@NotNull Project project) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Attaching native debugger", false) {
            public void run(@NotNull ProgressIndicator indicator) {
                while(true) {
                    try {
                        StepContext stepContext = myStepQueue.take();
                        if (!stepContext.isFinished()) {
                            indicator.setText(stepContext.getName());
                            continue;
                        }
                    } catch (InterruptedException var3) {
                        ProgressReporter.LOG.error(var3);
                    }

                    return;
                }
            }
        });
    }

    public ProgressReporter(@NotNull final Project project) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                ProgressReporter.this.runProgressTask(project);
            }
        });
    }

    public void step(String stepName) {
        this.putStep(new StepContext(stepName, false));
    }

    public void finish() {
        this.putStep(new StepContext(null, true));
    }

    private static class StepContext {
        private final String myName;
        private final boolean myFinished;

        public StepContext(@Nullable String name, boolean finished) {
            this.myName = name;
            this.myFinished = finished;
        }

        @Nullable
        public String getName() {
            return this.myName;
        }

        public boolean isFinished() {
            return this.myFinished;
        }
    }
}
