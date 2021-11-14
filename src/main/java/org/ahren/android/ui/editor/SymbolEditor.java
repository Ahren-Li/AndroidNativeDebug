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

package org.ahren.android.ui.editor;


import org.ahren.android.debug.AndroidDebugParameters;
import org.ahren.android.debug.BaseJetbrainsEditor;
import org.ahren.android.debug.BaseRunConfiguration;
import org.ahren.android.ui.ChooseSymbolDirectoryPanel;
import org.ahren.android.ui.PatchAccessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.StatusText;
import org.ahren.android.utils.Log;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class SymbolEditor<E extends BaseRunConfiguration> extends SettingsEditor<E> {
    private static final Logger LOG = Log.factory("BaseJetbrainsEditor");


    private final Project mProject;
    private final BaseRunConfiguration mConfiguration;

    private TextFieldWithBrowseButton mAndroidPath;
    private JTextField mAndroidLaunch;
    private ChooseSymbolDirectoryPanel mSymbolList;
    private JPanel mPanel;

    public SymbolEditor(Project project, BaseRunConfiguration configuration){
        mProject = project;
        mConfiguration = configuration;
    }

    private void createUIComponents() {

        mAndroidPath = new TextFieldWithBrowseButton();
        mAndroidPath.setEditable(false);
        String titleA = "Android Source Code";
        FileChooserDescriptor descriptorA =  FileChooserDescriptorFactory.createSingleFolderDescriptor();
        mAndroidPath.addBrowseFolderListener(titleA, null, mProject, descriptorA, new PatchAccessor(s -> {
            if(mAndroidLaunch != null && mConfiguration.getCreator().createSymbolPaths(s, mAndroidLaunch.getText())){
                LOG.info("onSymbolChange!");
                if(mSymbolList == null) return;
                mSymbolList.setSymbolDirs(mConfiguration.getCreator().getSymbolList());
                mSymbolList.invalidate();
            }
        }));

        CaretListener listener = new DebugCaretListener();
        mAndroidLaunch = new JBTextField();
        mAndroidLaunch.addCaretListener(listener);

        StatusText textA = ((JBTextField)mAndroidLaunch).getEmptyText();
        textA.setText("please input launch id");

        mSymbolList = new ChooseSymbolDirectoryPanel(mProject);
        mSymbolList.setSymbolAddAction(new ChooseSymbolDirectoryPanel.SymbolAddAction() {
            @Override
            public void onManualSymbolAdd(String symbol) {
                mConfiguration.getCreator().addSymbol(symbol);
            }

            @Override
            public void onManualSymbolRemove(String symbol) {
                mConfiguration.getCreator().removeSymbol(symbol);
            }
        });
    }

    @Override
    protected void resetEditorFrom(@NotNull E e) {
        AndroidDebugParameters parameters = e.getParameters();

        String path = parameters.getAndroidPath();
        if(mAndroidPath != null){
            mAndroidPath.setText(path);
        }
        String launch = parameters.getAndroidLaunch();
        if(mAndroidLaunch != null){
            mAndroidLaunch.setText(launch);
        }

        if(mSymbolList != null){
            mSymbolList.setSymbolDirs(e.getCreator().getSymbolList());
            mSymbolList.invalidate();
        }
    }

    @Override
    protected void applyEditorTo(@NotNull E e) {
        String path   = mAndroidPath.getText();
        String launch = mAndroidLaunch.getText();

        AndroidDebugParameters parameters = e.getParameters();

        parameters.setAndroidPath(path);
        parameters.setAndroidLaunch(launch);
//        if(!StringUtil.isEmpty(path)){
//        }
//        if(!StringUtil.isEmpty(launch)){
//        }
        parameters.setAndroidSymbolList(e.getCreator().getSymbolList());
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return mPanel;
    }

    private class DebugCaretListener implements CaretListener {

        @Override
        public void caretUpdate(CaretEvent e) {
            if(mConfiguration.getCreator().createSymbolPaths(mAndroidPath.getText(), mAndroidLaunch.getText())){
                LOG.info("onSymbolChange!");
                if(mSymbolList == null) return;
                mSymbolList.setSymbolDirs(mConfiguration.getCreator().getSymbolList());
                mSymbolList.invalidate();
            }
        }
    }
}
