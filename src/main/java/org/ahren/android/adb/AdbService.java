/*
 * Copyright 2017 The Android Open Source Project
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

package org.ahren.android.adb;

import com.android.ddmlib.AndroidDebugBridge;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import org.ahren.android.utils.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class AdbService implements Disposable {
    private static final Logger LOG = Log.factory("AdbService");
    private static final Object ADB_INIT_LOCK = new Object();

    private ListenableFuture<AndroidDebugBridge> mFuture;
    private final AtomicReference<File> mAdb = new AtomicReference<>();

    public static AdbService getInstance() {
        return ServiceManager.getService(AdbService.class);
    }

    @Override
    public void dispose() {
        terminateDdmlib();
    }

    public synchronized ListenableFuture<AndroidDebugBridge> getDebugBridge(@NotNull File adb) {
        mAdb.set(adb);
        if (this.mFuture != null && this.mFuture.isDone() && !wasSuccessful(mFuture)) {
            terminateDdmlib();
        }

        if (this.mFuture == null) {
            Future<BridgeConnectionResult> future = ApplicationManager.getApplication().executeOnPooledThread(new CreateBridgeTask(adb));
            mFuture = makeTimedFuture(future, 20L, TimeUnit.SECONDS);
        }

        return mFuture;
    }

    @SuppressWarnings("WeakerAccess")
    public synchronized void terminateDdmlib() {
        if (this.mFuture != null) {
            this.mFuture.cancel(true);
            this.mFuture = null;
        }

        synchronized(ADB_INIT_LOCK) {
            AndroidDebugBridge.disconnectBridge();
            AndroidDebugBridge.terminate();
        }
    }

    private static boolean wasSuccessful(Future<AndroidDebugBridge> future) {
        if (!future.isDone()) {
            return false;
        } else {
            try {
                AndroidDebugBridge bridge = future.get();
                return bridge != null && bridge.isConnected();
            } catch (Exception var2) {
                return false;
            }
        }
    }

    private static ListenableFuture<AndroidDebugBridge> makeTimedFuture(@NotNull Future<BridgeConnectionResult> delegate, long timeout, @NotNull TimeUnit unit) {
        SettableFuture<AndroidDebugBridge> future = SettableFuture.create();
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                BridgeConnectionResult value = delegate.get(timeout, unit);
                if (value.error != null) {
                    future.setException(new RuntimeException("Unable to create Debug Bridge: " + value.error));
                } else {
                    future.set(value.bridge);
                }
            } catch (ExecutionException var6) {
                future.setException(var6.getCause());
            } catch (TimeoutException | InterruptedException var7) {
                delegate.cancel(true);
                future.setException(var7);
            }

        });
        return future;
    }

    private static class BridgeConnectionResult {
        @Nullable
        public final AndroidDebugBridge bridge;
        @Nullable
        public final String error;

        private BridgeConnectionResult(@Nullable AndroidDebugBridge bridge, @Nullable String error) {
            this.bridge = bridge;
            this.error = error;
        }

        public static BridgeConnectionResult make(@NotNull AndroidDebugBridge bridge) {
            return new BridgeConnectionResult(bridge, null);
        }

        public static BridgeConnectionResult make(@NotNull String error) {
            return new BridgeConnectionResult(null, error);
        }
    }

    private static class CreateBridgeTask implements Callable<BridgeConnectionResult> {
        private final File mAdb;

        public CreateBridgeTask(@NotNull File adb) {
            mAdb = adb;
        }

        public BridgeConnectionResult call() throws Exception {
            AdbService.LOG.info("Initializing adb using: " + mAdb.getAbsolutePath());
            ImmutableMap<String, String> env;
            if (ApplicationManager.getApplication() != null && !ApplicationManager.getApplication().isUnitTestMode()) {
                env = ImmutableMap.of();
            } else {
                env = ImmutableMap.of("HOME", Files.createTempDir().getAbsolutePath());
            }

            BridgeConnectionResult var4;

                AndroidDebugBridge bridge;
                synchronized(AdbService.ADB_INIT_LOCK) {
                    AndroidDebugBridge.init(true, false, env);
                    bridge = AndroidDebugBridge.createBridge(mAdb.getPath(), false);
                }

                if (bridge != null) {
                    while(!bridge.isConnected()) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(200L);
                        } catch (InterruptedException var11) {
                            BridgeConnectionResult var5 = BridgeConnectionResult.make("Timed out attempting to connect to adb: ");
                        }
                    }

                    AdbService.LOG.info("Successfully connected to adb");
                    var4 = BridgeConnectionResult.make(bridge);
                    return var4;
                }

                var4 = BridgeConnectionResult.make("Unable to start adb server: ");


            return var4;
        }
    }
}
