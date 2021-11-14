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

import com.intellij.openapi.ui.TextComponentAccessor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PatchAccessor implements TextComponentAccessor<JTextField> {

    private final PatchAccessorListen mListen;

    public interface PatchAccessorListen{
        void onText(String s);
    }

    public PatchAccessor(){
        mListen = null;
    }

    public PatchAccessor(PatchAccessorListen listen){
        mListen = listen;
    }

    @Override
    public String getText(JTextField jTextField) {
        return TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT.getText(jTextField);
    }

    @Override
    public void setText(JTextField jTextField, @NotNull String s) {
//        s = s.replace("\\", "/");
        jTextField.setText(s);
        if(mListen != null){
            mListen.onText(s);
        }
    }
}