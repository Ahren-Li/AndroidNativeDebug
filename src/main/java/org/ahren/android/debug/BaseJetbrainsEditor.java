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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StatusText;
import org.ahren.android.utils.Log;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;

public class BaseJetbrainsEditor<E extends BaseRunConfiguration> extends SettingsEditor <E>{

    private static final boolean DEBUG = true;
    private static final Logger LOG = Log.factory("BaseJetbrainsEditor");

    private final SymbolCreator mCreator;

    private TextFieldWithBrowseButton mAndroidPath;
    private JBTextField mAndroidLaunch;
    private JBTextField mAndroidRemote;
    private AndroidDebugParameters mParameters;

    protected Project mProject;
    protected GridBag mGridBag;

    public BaseJetbrainsEditor(Project project, int type){
        mProject = project;
        mParameters = new AndroidDebugParameters();
        mCreator = new SymbolCreator(type);
        mGridBag = new GridBag();
        mGridBag.setDefaultFill(GridBag.HORIZONTAL);
        mGridBag.setDefaultAnchor(GridBagConstraints.CENTER);
        mGridBag.setDefaultWeightX(1, 1.0D);
        mGridBag.setDefaultInsets(0, JBUI.insets(0,0, 4, 10));
        mGridBag.setDefaultInsets(1, JBUI.insetsBottom(4));
    }

    protected void baseResetEditorFrom(@NotNull E runConfiguration){
        String remote = runConfiguration.getParameters().getRemote();
        if(mAndroidRemote != null){
            mAndroidRemote.setText(remote);
        }

        String path = runConfiguration.getParameters().getAndroidPath();
        if(mAndroidPath != null){
            mAndroidPath.setText(path);
        }
        String launch = runConfiguration.getParameters().getAndroidLaunch();
        if(mAndroidLaunch != null){
            mAndroidLaunch.setText(launch);
        }

    }

    protected void baseApplyEditorTo(@NotNull E runConfiguration){
        String path   = mAndroidPath.getText();
        String launch = mAndroidLaunch.getText();
        String remote = mAndroidRemote.getText();

        mParameters.setAndroidPath(path);
        mParameters.setAndroidLaunch(launch);
        mParameters.setRemote(remote);


        runConfiguration.setParameters(mParameters);
    }

    @Override
    protected void resetEditorFrom(@NotNull E baseRunConfiguration) {
        baseResetEditorFrom(baseRunConfiguration);
    }

    @Override
    protected void applyEditorTo(@NotNull E baseRunConfiguration) {
        baseApplyEditorTo(baseRunConfiguration);
    }

    protected void onCreateEditorStart(JPanel panel, GridBag gridBag) {

    }

    protected void onCreateEditorEnd(JPanel panel, GridBag gridBag) {

    }

    @Override
    protected @NotNull JComponent createEditor() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBag gridBag = new GridBag();
        gridBag.setDefaultFill(GridBag.HORIZONTAL);
        gridBag.setDefaultAnchor(GridBagConstraints.CENTER);
        gridBag.setDefaultWeightX(1, 1.0D);
        gridBag.setDefaultInsets(0, JBUI.insets(0,0, 4, 10));
        gridBag.setDefaultInsets(1, JBUI.insetsBottom(4));

        onCreateEditorStart(panel, gridBag);

        CaretListener listener = new DebugCaretListener();
//        panel.add(new JLabel("Android Launch:"), gridBag.nextLine().next().insetBottom(GridBag.NORTHEAST));
        mAndroidLaunch = new JBTextField();
        mAndroidLaunch.addCaretListener(listener);
        StatusText textA = mAndroidLaunch.getEmptyText();
        textA.setText("please input launch id");
//        panel.add(mAndroidLaunch, gridBag.next().coverLine(1).insetBottom(GridBag.NORTHEAST));

//        panel.add(new JLabel("Android Remote:"), gridBag.next().coverLine(1).insetBottom(GridBag.NORTHEAST));
        mAndroidRemote = new JBTextField();
        StatusText textR = mAndroidRemote.getEmptyText();
        textR.setText("unix-connect:///");
//        panel.add(mAndroidRemote, gridBag.next().coverLine().insetBottom(GridBag.NORTHEAST));

        JPanel remote = new JPanel(new GridBagLayout());
        GridBag gridBagRemote = new GridBag();
        gridBagRemote.setDefaultFill(GridBag.BOTH);
        gridBagRemote.setDefaultAnchor(GridBagConstraints.CENTER);
        gridBagRemote.setDefaultWeightX(1, 1.0D);
        gridBagRemote.setDefaultInsets(0, JBUI.insets(0,0, 4, 10));
        gridBagRemote.setDefaultInsets(1, JBUI.insetsBottom(4));

        remote.add(new JLabel("Android Remote:"), gridBagRemote.nextLine().next().insetBottom(GridBag.NORTHEAST));
        remote.add(mAndroidRemote, gridBagRemote.next().coverLine(3).insetBottom(GridBag.NORTHEAST));
        remote.add(new JLabel("Android Launch:"), gridBagRemote.next().coverLine(1).insetBottom(GridBag.NORTHEAST));
        remote.add(mAndroidLaunch, gridBagRemote.next().coverLine(1).insetBottom(GridBag.NORTHEAST));
        panel.add(remote, gridBag.next().coverLine().insetBottom(GridBag.NORTHEAST));

        panel.add(new JLabel("Android Source Code:"), gridBag.nextLine().next().insetBottom(GridBag.NORTHEAST));
        mAndroidPath = new TextFieldWithBrowseButton();
//        mAndroidPath.addCaretListener(listener);
        String titleA = "Android Source Code";
        FileChooserDescriptor descriptorA =  FileChooserDescriptorFactory.createSingleFolderDescriptor();
        mAndroidPath.addBrowseFolderListener(titleA, null, mProject, descriptorA);
        panel.add(mAndroidPath, gridBag.next().coverLine().insetBottom(GridBag.NORTHEAST));

        onCreateEditorEnd(panel, gridBag);

        return panel;
    }

    protected void onSymbolChange(){

    }

    public SymbolCreator getCreator(){
        return mCreator;
    }

    private class DebugCaretListener implements CaretListener {

        @Override
        public void caretUpdate(CaretEvent e) {
            if(DEBUG) LOG.info("caretUpdate:" + e);
            if(mCreator.createSymbolPaths(mAndroidPath.getText(), mAndroidLaunch.getText())){
                onSymbolChange();
            }
        }
    }
}
