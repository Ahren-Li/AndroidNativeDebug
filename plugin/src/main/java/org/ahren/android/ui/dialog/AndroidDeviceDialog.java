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

package org.ahren.android.ui.dialog;

import org.ahren.android.adb.AdbService;
import org.ahren.android.ui.ConnectDeviceRenderer;
import org.ahren.android.ui.model.AndroidListModel;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBLoadingPanel;
import com.intellij.util.Alarm;
import com.intellij.util.ui.EdtInvocationManager;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import org.ahren.android.adb.devices.ConnectedAndroidDevice;
import org.ahren.android.adb.tools.EdtExecutor;
import org.ahren.android.utils.Log;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;

public class AndroidDeviceDialog extends DialogWrapper implements Disposable,
        AndroidDebugBridge.IDebugBridgeChangeListener,
        AndroidDebugBridge.IDeviceChangeListener {

    private final Logger LOG = Log.factory("AndroidDeviceDialog");
    private final boolean DEBUG = false;

    private final ListenableFuture<AndroidDebugBridge> mAdbFuture;
    private final MergingUpdateQueue mUpdateQueue;

    private JPanel mPanel;
    private ConnectedAndroidDevice mSelectDevice;
    private JBList<ConnectedAndroidDevice> mDeviceList;

    public AndroidDeviceDialog(@Nullable Project project, String adbPath) {
        super(project);
        mUpdateQueue = new MergingUpdateQueue("android.device.chooser", 250, true, null, this, null, Alarm.ThreadToUse.POOLED_THREAD);
        File adbFile = new File(adbPath);
        if (!adbFile.exists()) {
            throw new IllegalArgumentException("Unable to locate adb");
        } else {
            mAdbFuture = AdbService.getInstance().getDebugBridge(adbFile);
            setTitle("Select Deployment Target");
            setModal(true);
            setOKActionEnabled(true);
            init();
        }

        AndroidDebugBridge.addDebugBridgeChangeListener(this);
        AndroidDebugBridge.addDeviceChangeListener(this);
        postUpdate();
        Disposer.register(getDisposable(), this);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        mDeviceList = new JBList<>();
        mDeviceList.setEmptyText("not found adb devices");
        mDeviceList.setCellRenderer(new ConnectDeviceRenderer());
        new MyDoubleClickListener(new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(MouseEvent mouseEvent) {
                Action okAction = getOKAction();
                if (okAction.isEnabled()) {
                    okAction.actionPerformed(null);
                    return true;
                } else {
                    return false;
                }
            }
        }, this).installOn(mDeviceList);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        final JBLoadingPanel loadingPanel = new JBLoadingPanel(new BorderLayout(), this.getDisposable());
        loadingPanel.add(mPanel);
        loadingPanel.setLoadingText("Initializing ADB");
        if (!mAdbFuture.isDone()) {
            loadingPanel.startLoading();
            Futures.addCallback(mAdbFuture, new FutureCallback<AndroidDebugBridge>() {
                public void onSuccess(AndroidDebugBridge result) {
                    loadingPanel.stopLoading();
                    LOG.info("Successfully obtained debug bridge");
                }

                public void onFailure(@Nullable Throwable t) {
                    loadingPanel.stopLoading();
                    LOG.info("Unable to obtain debug bridge", t);
                }
            }, EdtExecutor.INSTANCE);
        }

        return loadingPanel;
    }

    @Override
    public void dispose() {
        super.dispose();
        mDeviceList = null;
        AndroidDebugBridge.removeDebugBridgeChangeListener(this);
        AndroidDebugBridge.removeDeviceChangeListener(this);
    }

    @Override
    public void bridgeChanged(AndroidDebugBridge androidDebugBridge) {
        postUpdate();
    }

    @Override
    public void deviceConnected(IDevice iDevice) {
        postUpdate();
    }

    @Override
    public void deviceDisconnected(IDevice iDevice) {
        postUpdate();
    }

    @Override
    public void deviceChanged(IDevice iDevice, int i) {
        postUpdate();
    }

    @Override
    protected void doOKAction() {
        mSelectDevice = mDeviceList.getSelectedValue();
        if(DEBUG) LOG.info("doOKAction:" + mSelectDevice.getName());
        super.doOKAction();
    }

    public ConnectedAndroidDevice getSelectDevice(){
        return mSelectDevice;
    }

    private void postUpdate() {
        mUpdateQueue.queue(new Update("updateDevicePickerModel") {
            public void run() {
                updateModel();
            }

            public boolean canEat(Update update) {
                return true;
            }
        });
    }

    private void updateModel(){
        AndroidDebugBridge bridge = AndroidDebugBridge.getBridge();
        if (bridge != null && bridge.isConnected()){
            if(!ApplicationManager.getApplication().isDispatchThread()) {
                if(DEBUG) LOG.info("reUpdateModel!!");
                EdtInvocationManager.getInstance().invokeLater(this::updateModel);
            }else if(mDeviceList != null){
                mDeviceList.setPaintBusy(true);
                EdtInvocationManager.getInstance().invokeLater(() -> {
                    AndroidListModel<ConnectedAndroidDevice> model = new AndroidListModel<>(bridge.getDevices(), o -> {
                        if(o instanceof IDevice){
                            return new ConnectedAndroidDevice((IDevice) o);
                        }
                        return null;
                    });
                    mDeviceList.setModel(model);
                });
            }
        }
    }

    private static class MyDoubleClickListener extends DoubleClickListener implements Disposable {

        private DoubleClickListener myDelegate;

        MyDoubleClickListener(DoubleClickListener delegate, Disposable parent) {
            this.myDelegate = delegate;
            Disposer.register(parent, this);
        }

        protected boolean onDoubleClick(MouseEvent event) {
            return this.myDelegate != null && this.myDelegate.onClick(event, 2);
        }

        public void dispose() {
            this.myDelegate = null;
        }
    }
}
