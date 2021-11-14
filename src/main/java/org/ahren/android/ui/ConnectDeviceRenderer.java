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

package org.ahren.android.ui;

import org.ahren.android.ui.model.AndroidListModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.EdtInvocationManager;
import org.ahren.android.adb.devices.ConnectedAndroidDevice;
import org.ahren.android.utils.Log;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ConnectDeviceRenderer extends ColoredListCellRenderer<ConnectedAndroidDevice> {

    private final Logger LOG = Log.factory("ConnectDeviceRenderer");
    private final boolean DEBUG = false;

    @Override
    @SuppressWarnings("unchecked")
    protected void customizeCellRenderer(@NotNull JList<? extends ConnectedAndroidDevice> jList, ConnectedAndroidDevice device, int i, boolean b, boolean b1) {
        if(!device.isRenderLabelOk()){
            setPaintBusy(jList, true);
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                if(DEBUG) LOG.info("customizeCellRenderer:executeOnPooledThread");
                device.prepareToRenderLabel();
                EdtInvocationManager.getInstance().invokeLater(() -> {
                    if (jList.isDisplayable()) {
                        ListModel<? extends ConnectedAndroidDevice> model = jList.getModel();
                        if (model instanceof AndroidListModel) {
                            ((AndroidListModel<ConnectedAndroidDevice>)model).entryContentChanged(device);
                        }
                    }
                    setPaintBusy(jList, false);
                });
            });
        }else{
            device.renderLabel(this, true, null);
        }
    }

    private static void setPaintBusy(JList list, boolean busy) {
        if (list instanceof JBList) {
            ((JBList)list).setPaintBusy(busy);
        }

    }
}
