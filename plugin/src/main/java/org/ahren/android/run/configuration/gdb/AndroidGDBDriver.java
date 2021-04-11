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

import com.jetbrains.cidr.ArchitectureType;
import org.ahren.android.debug.AndroidDebugParameters;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerCommandException;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.jetbrains.cidr.execution.debugger.backend.gdb.GDBDriver;
import org.ahren.android.utils.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class AndroidGDBDriver extends GDBDriver {

    private final Logger LOG   = Log.factory("AndroidGDBDriver");

    private final AndroidDebugParameters mParameters;

    AndroidGDBDriver(@NotNull AndroidDebugParameters parameters, @NotNull Handler handler, @NotNull ArchitectureType architectureType, @NotNull DebuggerDriverConfiguration debuggerDriverConfiguration) throws ExecutionException {
        super(handler, debuggerDriverConfiguration, architectureType);
        mParameters = parameters;
        LOG.info("architectureType=" + architectureType);
//        executeCommandNoUserException(new AttachConnectCommand() {
//            @Override
//            protected int attach() throws ExecutionException, DebuggerCommandException {
//                gdbSet("target-charset", "UTF-8");
//                return 0;
//            }
//
//            @Override
//            protected void whenAttached() throws ExecutionException, DebuggerCommandException {
//
//            }
//        })
//        try {
//            gdbSet("target-charset", "UTF-8");
//        } catch (DebuggerCommandException e) {
//            e.printStackTrace();
//        }
//        getProcessHandler().addProcessListener(new ProcessListener() {
//            @Override
//            public void startNotified(@NotNull ProcessEvent processEvent) {
//
//            }
//
//            @Override
//            public void processTerminated(@NotNull ProcessEvent processEvent) {
//
//            }
//
//            @Override
//            public void processWillTerminate(@NotNull ProcessEvent processEvent, boolean b) {
//
//            }
//
//            @Override
//            public void onTextAvailable(@NotNull ProcessEvent processEvent, @NotNull Key key) {
//                String text = processEvent.getText();
//                if(key == ProcessOutputTypes.STDOUT){
//                    Log.i("text1=" + text);
//                    try {
//                        Log.i("text1=" + new String(text.getBytes("CP1252"), Charset.forName("CP1252")));
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
    }

    @NotNull
    @Override
    public Inferior loadForRemote(@NotNull String s, @Nullable File file, @Nullable File file1, @NotNull List<PathMapping> list) throws ExecutionException {
        Inferior inferior = super.loadForRemote(s, file, file1, list);
        initParameters();
        return inferior;
    }

    private void gdbMiDir(@NotNull String path) throws ExecutionException, DebuggerCommandException {

        String cmd = String.format("-environment-directory %s", path);
        this.sendRequestAndWaitForDone("%s", cmd);
    }

    private void initParameters(){
        String sourcePath = mParameters.getAndroidPath();
        String launch = mParameters.getAndroidLaunch();

        LOG.info("initParameters,sourcePath=" + sourcePath);
        LOG.info("initParameters,launch=" + launch);

        if(sourcePath != null){
            String sym = sourcePath;
            if(launch != null){
                sym += "/out/target/product/" + launch + "/symbols";
            }

            try {
                gdbMiDir(sourcePath);
                gdbSet("sysroot", sym);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
