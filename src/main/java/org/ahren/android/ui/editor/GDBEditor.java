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
import org.ahren.android.run.configuration.gdb.GDBRunConfiguration;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.ui.components.JBTextField;
import com.jetbrains.cidr.cpp.CPPBundle;
import com.jetbrains.cidr.cpp.execution.remote.CLionRemoteRunConfigurationKt;
import com.jetbrains.cidr.cpp.toolchains.CPPDebugger;
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains;
import com.jetbrains.cidr.ui.*;
import kotlin.comparisons.ComparisonsKt;
import kotlin.jvm.internal.Intrinsics;
import org.ahren.android.ui.DefaultDebuggerComboItem;
import org.ahren.android.ui.MyDebuggersRenderer;
import org.ahren.android.utils.AndroidBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("GtkPreferredJComboBoxRenderer")
public class GDBEditor extends SettingsEditor<GDBRunConfiguration> {
    private JPanel mPanel;

    private final Project mProject;

    private JComboBox<Object> mDebuggersCombo;
    private ComponentWithBrowseButton<JComboBox<Object>> mCWBrowseButton;
    private JBTextField mRemote;

    private CustomEditableComboItem mCustomExecutableComboItem;
    private SortedCollectionComboBoxModel<Object> mDebuggersModel;

    public GDBEditor(Project project){
        mProject = project;
    }

    @Override
    protected void resetEditorFrom(@NotNull GDBRunConfiguration gdbRunConfiguration) {
        Intrinsics.checkParameterIsNotNull(gdbRunConfiguration, "s");
        Object o = DefaultDebuggerComboItem.AHREN;
        resetDebuggersModel(o);

        String remote = gdbRunConfiguration.getParameters().getRemote();
        if(remote != null){
            mRemote.setText(remote);
        }
    }

    @Override
    protected void applyEditorTo(@NotNull GDBRunConfiguration gdbRunConfiguration) {
        Object tool   = mDebuggersCombo.getSelectedItem();

        if(tool instanceof String ){
            String pathTool = (String)tool;
            pathTool = pathTool.trim();
            if(!pathTool.isEmpty()){
                gdbRunConfiguration.setDebug(pathTool);
            }
        }else if(tool instanceof File){
            File toolFile = (File) tool;
            if(toolFile.canExecute()){
                gdbRunConfiguration.setDebug(toolFile.getAbsolutePath());
            }
        }else if(tool instanceof CPPToolchains.Toolchain){
            gdbRunConfiguration.setToolchain((CPPToolchains.Toolchain) tool);
        }

        AndroidDebugParameters parameters = gdbRunConfiguration.getParameters();
        String remote = mRemote.getText();
        if(remote != null){
            parameters.setRemote(remote);
        }
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return mPanel;
    }

    @SuppressWarnings("unchecked,warnings")
    private void createUIComponents() {
        mDebuggersCombo = new ActionItemsComboBox<>((objectActionItemsComboBox, o) -> {
            Intrinsics.checkParameterIsNotNull(objectActionItemsComboBox, "$receiver");
            Intrinsics.checkParameterIsNotNull(o, "it");
            return o instanceof File || o instanceof String;
        });

        String builder = "Custom " + AndroidBundle.message("android.gdb") + " executable";
        mCustomExecutableComboItem = new CustomEditableComboItem(builder);
        mDebuggersModel = new SortedCollectionComboBoxModel<>(new GdbComparator<>());

        mDebuggersCombo.setModel(mDebuggersModel);
        resetDebuggersModel(null);

        /*
        try {
            Class MyDebuggersRenderer = Class.forName("com.jetbrains.cidr.cpp.execution.remote.MyDebuggersRenderer");
            Field o = MyDebuggersRenderer.getDeclaredField("INSTANCE");
            o.setAccessible(true);
            ListCellRenderer<Object> t = (ListCellRenderer<Object>) o.get(MyDebuggersRenderer);
            if(t != null){
                mDebuggersCombo.setRenderer(t);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }*/

        mDebuggersCombo.setRenderer(new MyDebuggersRenderer(true));

        mDebuggersCombo.setPrototypeDisplayValue(DefaultDebuggerComboItem.AHREN);
        mDebuggersCombo.addActionListener(e ->
                ComboBoxModelKt.selectInputAsExecutableFile(mDebuggersCombo, false, false)
        );

        mCWBrowseButton = new ComponentWithBrowseButton<>(mDebuggersCombo, null);
        String title = AndroidBundle.message("android.gdb") + " executable";
        FileChooserDescriptor descriptor =  FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor();
        descriptor = descriptor.withFileFilter(virtualFile -> new File(virtualFile.getPath()).canExecute());
        mCWBrowseButton.addBrowseFolderListener(title, null, mProject, descriptor, new ActionAccessor());
    }

    private void resetDebuggersModel(Object object){
        ArrayList<Object> arrayList = new ArrayList<>();
        CPPToolchains toolchains = CPPToolchains.getInstance();
        Intrinsics.checkExpressionValueIsNotNull(toolchains, "CPPToolchains.getInstance()");
        List<CPPToolchains.Toolchain> list = toolchains.getToolchains();
        Intrinsics.checkExpressionValueIsNotNull(list, "cppToolchains.toolchains");

        arrayList.add(DefaultDebuggerComboItem.AHREN);

        ArrayList<CPPToolchains.Toolchain> custom = new ArrayList<>();

        for(CPPToolchains.Toolchain toolchain : list){
            if(toolchain.getDebuggerKind() == CPPDebugger.Kind.CUSTOM_GDB){
                custom.add(toolchain);
            }
        }

        ArrayList<CPPToolchains.Toolchain> remote = new ArrayList<>();
        for(CPPToolchains.Toolchain o : custom){
            if(CLionRemoteRunConfigurationKt.getRemoteRunToolchainProblem(o, false) == null){
                remote.add(o);
            }
        }

        ArrayList<Object> items = new ArrayList<>();
        for(CPPToolchains.Toolchain toolchain : remote){
//            Object o = CLionRemoteRunConfigurationEditorKt.getComboItemFor(null, toolchain, null);
            items.add(toolchain);
        }

        arrayList.addAll(items);


        arrayList.add(mCustomExecutableComboItem);

        if(object != null && !arrayList.contains(object)){
            arrayList.add(object);
        }
        mDebuggersModel.reset(arrayList);
        if(object != null){
            mDebuggersModel.setSelectedItem(object);
        }else {
            mDebuggersModel.setSelectedItem(DefaultDebuggerComboItem.AHREN);
        }
    }

    class GdbComparator<E> implements Comparator<E> {


        @Override
        public int compare(E o1, E o2) {
            int comparable1, comparable2;
            if(Intrinsics.areEqual(o1, DefaultDebuggerComboItem.AHREN)){
                comparable1 = 0;
            }else if(o1 instanceof CPPToolchains.Toolchain){
                comparable1 = 1;
            }else if(o1 instanceof InvalidItem){
                comparable1 = 2;
            }else if(o1 instanceof File){
                comparable1 = 3;
            }else if(o1 instanceof CustomEditableComboItem){
                comparable1 = 10;
            }else{
                comparable1 = 4;
            }

            if(Intrinsics.areEqual(o2, DefaultDebuggerComboItem.AHREN)){
                comparable2 = 0;
            }else if(o2 instanceof CPPToolchains.Toolchain){
                comparable2 = 1;
            }else if(o2 instanceof InvalidItem){
                comparable2 = 2;
            }else if(o2 instanceof File){
                comparable2 = 3;
            }else if(o2 instanceof CustomEditableComboItem){
                comparable2 = 10;
            }else{
                comparable2 = 4;
            }

            return ComparisonsKt.compareValues(comparable1, comparable2);
        }
    }

    class ActionAccessor implements TextComponentAccessor<JComboBox<Object>> {

        @Override
        public String getText(JComboBox<Object> jComboBox) {
            return TextComponentAccessor.STRING_COMBOBOX_WHOLE_TEXT.getText(jComboBox);
        }

        @Override
        public void setText(JComboBox<Object> jComboBox, @NotNull String s) {
            Intrinsics.checkParameterIsNotNull(jComboBox, "comboBox");
            Intrinsics.checkParameterIsNotNull(s, "text");

            Object obj = ComboBoxModelKt.rememberAsExecutableFile(jComboBox, s,false, false);
            jComboBox.setSelectedItem(obj);
        }
    }
}
