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

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.ui.components.JBCheckBox;
import org.ahren.android.debug.AndroidDebugParameters;
import org.ahren.android.run.configuration.lldb.LLDBRunConfiguration;
import org.ahren.android.ui.PatchAccessor;
import org.ahren.android.ui.model.ListComboBoxModel;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.StatusText;
import org.ahren.android.utils.AndroidBundle;
import org.ahren.android.utils.Log;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LLDBEditor extends SettingsEditor<LLDBRunConfiguration> {

    private final static String NDK_BUNDLE = "ndk-bundle";
    private final static String SDK_LLDB = "lldb";

    private final static Logger  LOG = Log.factory("LLDBEditor");
    private final static boolean DEBUG = false;
    private final Project mProject;

    private ComponentWithBrowseButton<JComboBox<String>> mLLDBBrowseButton;
    private JBTextField mAndroidRemote;
    private TextFieldWithBrowseButton mNdkPath;
//    private JComboBox<Abi> mAbiBox;
//    private JComboBox<Android.Level> mAndroidLevel;
    private JBTextField mProcessName;
    private JPanel mPanel;
    private TextFieldWithBrowseButton mSdk;
    private JCheckBox mAdbRootCheckBox;
    private JComboBox<String> mLLDBPaths;

    public LLDBEditor(@NotNull Project project){
        super();
        mProject = project;
    }

    private void createUIComponents() {

        mLLDBPaths = new ComboBox<>();
        ListComboBoxModel<String> listMode = new ListComboBoxModel<>();
        mLLDBPaths.setModel(listMode);

        mLLDBBrowseButton = new ComponentWithBrowseButton<>(mLLDBPaths, null);
        FileChooserDescriptor descriptor =  FileChooserDescriptorFactory.createSingleFolderDescriptor();
        mLLDBBrowseButton.addBrowseFolderListener("LLDB Path", null, mProject, descriptor, new ActionAccessor());

        mAdbRootCheckBox = new JBCheckBox();
        mAdbRootCheckBox.addItemListener(e -> {
            if(mAdbRootCheckBox.isSelected()){
                mAndroidRemote.setEditable(false);
            }else {
                mAndroidRemote.setEditable(true);
            }
        });

        mAndroidRemote = new JBTextField();

        mSdk = new TextFieldWithBrowseButton();
        String titleSDK = "Android SDK Path";
        FileChooserDescriptor descriptorSDK =  FileChooserDescriptorFactory.createSingleFolderDescriptor();
        PatchAccessor.PatchAccessorListen listen = s -> {
            if(mNdkPath != null){
                String ndk_path = s + File.separator + NDK_BUNDLE;
                if(FileUtil.exists(ndk_path)){
                    mNdkPath.setText(ndk_path);
                }
            }
            if(mLLDBPaths != null){
                ListComboBoxModel<String> mode = (ListComboBoxModel<String>) mLLDBPaths.getModel();
                String lldb_path = s + File.separator + SDK_LLDB;
                if(FileUtil.exists(lldb_path)){
                    File lldb_dir = new File(lldb_path);
                    File[] dirs = lldb_dir.listFiles();
                    if(dirs != null){
                        for(File dir : dirs){
                            mode.add(dir.getAbsolutePath());
                        }
                        mode.setSelectedItem(dirs[dirs.length - 1].getAbsolutePath());
                        mLLDBPaths.setModel(mode);
                    }
                }
            }
        };
        mSdk.addBrowseFolderListener(titleSDK, null, mProject, descriptorSDK, new PatchAccessor(listen));

        mNdkPath = new TextFieldWithBrowseButton();
        String titleA = "Android NDK Path";
        FileChooserDescriptor descriptorA =  FileChooserDescriptorFactory.createSingleFolderDescriptor();
        mNdkPath.addBrowseFolderListener(titleA, null, mProject, descriptorA, new PatchAccessor());

        mProcessName = new JBTextField();
        StatusText textA = mProcessName.getEmptyText();
        textA.setText("example:surfaceflinger");

//        mAbiBox = new ComboBox<>(Abi.values());
//        mAndroidLevel = new ComboBox<>(Android.Level.values());
    }

    @Override
    protected void resetEditorFrom(@NotNull LLDBRunConfiguration lldbRunConfiguration) {
        AndroidDebugParameters parameters = lldbRunConfiguration.getParameters();

        if(mLLDBPaths != null){
            ListComboBoxModel<String> model = (ListComboBoxModel<String>) mLLDBPaths.getModel();
            String select = lldbRunConfiguration.getCurrentLLDBPath();
            List<String> paths = lldbRunConfiguration.getLLDBPaths();


            if(paths != null){
                if(!StringUtil.isEmpty(select)){
                    if(!paths.contains(select)){
                        paths.add(select);
                    }
                }
                model.reset(paths);
                model.setSelectedItem(select);
            }

        }

        if(mSdk != null){
            mSdk.setText(parameters.getSdkPath());
        }

        if(mNdkPath != null){
            mNdkPath.setText(parameters.getNdkPath());
        }

//        if(mAbiBox != null){
//            String abi = parameters.getArmAbi();
//            if(!StringUtil.isEmpty(abi)){
//                mAbiBox.setSelectedItem(Abi.getEnum(abi));
//            }
//        }
//
//        if(mAndroidLevel != null && !StringUtil.isEmpty(parameters.getApiLevel())){
//            mAndroidLevel.setSelectedItem(Android.Level.valueOf(parameters.getApiLevel()));
//        }

        if(mProcessName != null){
            mProcessName.setText(parameters.getProcessName());
        }

        if(mAndroidRemote != null){
            String remote = parameters.getRemote();
            if(remote == null || remote.isEmpty())
                remote = AndroidBundle.message("lldb.default.remote");
            mAndroidRemote.setText(remote);
        }

        if(mAdbRootCheckBox != null){
            mAdbRootCheckBox.setSelected(parameters.isAdbCanRoot());
        }

        if(mAdbRootCheckBox != null){
            mAdbRootCheckBox.setSelected(parameters.isAdbCanRoot());
        }
    }

    @Override
    protected void applyEditorTo(@NotNull LLDBRunConfiguration lldbRunConfiguration) {
        AndroidDebugParameters parameters = lldbRunConfiguration.getParameters();
        String processName = mProcessName.getText();
        processName = processName == null ? "" : processName;
        String remote = mAndroidRemote.getText();
        remote = remote == null ? "" : remote;

        String sdkPath = mSdk.getText();
        String ndkPath = mNdkPath.getText();
        parameters.setProcessName(processName);
        parameters.setRemote(remote);
        parameters.setAdbCanRoot(mAdbRootCheckBox.isSelected());
        parameters.setSdkPath(sdkPath);
        parameters.setNdkPath(ndkPath);
//        if(!StringUtil.isEmpty(processName)){
//        }
//        if(!StringUtil.isEmpty(remote)){
//        }
//        if(!StringUtil.isEmpty(sdkPath)){
//        }
//        if(!StringUtil.isEmpty(ndkPath)){
//        }

//        int index = mAbiBox.getSelectedIndex();
//        if(index != -1){
//            parameters.setArmAbi(mAbiBox.getItemAt(index).toString());
//        }
//        index = mAndroidLevel.getSelectedIndex();
//        if(index != -1){
//            parameters.setApiLevel(mAndroidLevel.getItemAt(index).name());
//        }

        int index = mLLDBPaths.getSelectedIndex();
        if(index != -1){
            parameters.setDebugExePath(mLLDBPaths.getItemAt(index));
            lldbRunConfiguration.setCurrentLLDBPath(mLLDBPaths.getItemAt(index));
        }

        int num = mLLDBPaths.getModel().getSize();
        ArrayList<String> paths = new ArrayList<>(num);
        for(int i = 0; i < num; i++){
            paths.add(mLLDBPaths.getItemAt(i));
        }

        lldbRunConfiguration.setLLDBPaths(paths);
//        lldbRunConfiguration.setParameters(parameters);
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return mPanel;
    }

    class ActionAccessor implements TextComponentAccessor<JComboBox<String>> {

        @Override
        public String getText(JComboBox<String> stringJComboBox) {
            return TextComponentAccessor.STRING_COMBOBOX_WHOLE_TEXT.getText(stringJComboBox);
        }

        @Override
        public void setText(JComboBox<String> stringJComboBox, @NotNull String s) {
            if(DEBUG) LOG.info("ActionAccessor:" + s);
//            s = s.replace("\\", "/");
            ListComboBoxModel<String> model = (ListComboBoxModel<String>) stringJComboBox.getModel();
            if(!model.contains(s)){
                model.add(s);
                model.setSelectedItem(s);
            }
        }
    }
}
