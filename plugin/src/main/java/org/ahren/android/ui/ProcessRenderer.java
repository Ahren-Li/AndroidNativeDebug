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

import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.ahren.android.adb.AdbProcess;
import org.ahren.android.ui.icons.AndroidIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ProcessRenderer extends ColoredListCellRenderer<AdbProcess> {

    @Override
    protected void customizeCellRenderer(@NotNull JList<? extends AdbProcess> jList, AdbProcess adbProcess, int i, boolean b, boolean b1) {
        setIcon(AndroidIcons.Ddms.Threads);
        append(adbProcess.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        append(" :" + adbProcess.getPid(), SimpleTextAttributes.GRAY_ATTRIBUTES);
    }

}
