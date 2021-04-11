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

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChooseSymbolDirectoryPanel extends JPanel {
    private final Project mProject;
    private final JBList<String> mSymDirsList;
    private CollectionListModel<String> mSymDirsModel;
    private SymbolAddAction mListen;

    public interface SymbolAddAction{
        void onManualSymbolAdd(String symbol);
        void onManualSymbolRemove(String symbol);
    }

    public ChooseSymbolDirectoryPanel(@NotNull Project project, SymbolAddAction listen) {
        super(new BorderLayout());
        mProject = project;
        mListen = listen;
        mSymDirsModel = new CollectionListModel<>();
        mSymDirsList = new JBList<>(mSymDirsModel);
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(mSymDirsList).setAddAction((button) -> {
            String path = this.chooseDirectory();
            if (path != null) {
                mSymDirsModel.add(path);
                mSymDirsList.setSelectedValue(path, true);
                if(mListen != null){
                    mListen.onManualSymbolAdd(path);
                }
            }

        }).setRemoveAction(anActionButton -> {
          String path = mSymDirsList.getSelectedValue();
          if(mSymDirsModel.contains(path)){
              mSymDirsModel.remove(path);
              if(mListen != null){
                  mListen.onManualSymbolRemove(path);
              }
          }
        });
        add(decorator.createPanel());


    }

    public ChooseSymbolDirectoryPanel(@NotNull Project project) {
        this(project, null);
    }

    @Nullable
    private String chooseDirectory() {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        VirtualFile file = FileChooser.chooseFile(descriptor, mSymDirsList, mProject, mProject.getProjectFile());
        return file != null ? FileUtil.toSystemDependentName(file.getPath()) : null;
    }

    public void setSymbolAddAction(SymbolAddAction mListen) {
        this.mListen = mListen;
    }

    @NotNull
    public List<String> getSymbolDirs() {
        return mSymDirsModel.getItems();
    }

    public void setSymbolDirs(@NotNull List<String> symDirs) {
        mSymDirsModel = new CollectionListModel<>(symDirs);
        mSymDirsList.setModel(mSymDirsModel);
    }
}
