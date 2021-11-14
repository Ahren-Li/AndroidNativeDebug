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

package org.ahren.android.run.configuration.gdb;

import org.ahren.android.debug.AndroidDebugState;
import org.ahren.android.debug.BaseRunConfiguration;
import org.ahren.android.debug.IAndroidRunProfile;
import org.ahren.android.debug.SymbolCreator;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains;
import org.ahren.android.ui.editor.GDBEditor;
import org.ahren.android.ui.editor.SymbolEditor;
import org.ahren.android.utils.Configuration;
import kotlin.jvm.internal.Intrinsics;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GDBRunConfiguration extends BaseRunConfiguration
        implements RunConfigurationWithSuppressedDefaultRunAction, IAndroidRunProfile {

    private final boolean DEBUG = true;
    private final String  TAG   = "GDBRunConfiguration";

    private String mDebug;
    private String mUnknownToolchainName;
    private CPPToolchains.Toolchain mToolchain;


    GDBRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name, SymbolCreator.GDB_TYPE);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        SettingsEditorGroup<GDBRunConfiguration> group = new SettingsEditorGroup<>();
        group.addEditor("Config", new GDBEditor(getProject()));
        group.addEditor("Symbol", new SymbolEditor<>(getProject(),this));
        return group;
//        return new JetbrainsGDBEditor(getProject());
    }

    @Nullable
    @Override
    public AndroidDebugState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) {
        return new AndroidDebugState(new AndroidGDBLauncher(getProject(), createGDBToolchain(), getParameters()), executionEnvironment);
    }

    @Override
    public void checkConfiguration()  {

    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
//        Log.i(DEBUG, TAG + ":readExternal.");
        mDebug = element.getAttributeValue(Configuration.GDB_DEBUG_NAME);
        String toolchainName = element.getAttributeValue(Configuration.GDB_TOOLCHAIN_NAME);

        if(toolchainName != null && !toolchainName.isEmpty()){
            mToolchain = CPPToolchains.getInstance().getToolchainByNameOrDefault(toolchainName);
        }else{
            mToolchain = null;
        }

        if(mToolchain == null) mUnknownToolchainName = toolchainName;
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
//        Log.i(DEBUG, TAG + ":writeExternal.");
        if(mDebug != null){
            element.setAttribute(Configuration.GDB_DEBUG_NAME, FileUtil.toSystemIndependentName(mDebug));
        }

        String toolchainName;
        CPPToolchains.Toolchain toolchain = mToolchain;
        if(toolchain != null){
            toolchainName = toolchain.getName();
        }else{
            toolchainName = mUnknownToolchainName;
        }

        if(toolchainName != null){
            element.setAttribute(Configuration.GDB_TOOLCHAIN_NAME, toolchainName);
        }
    }

    public void setDebug(String debug) {
        this.mDebug = debug;
    }

    public String getDebug() {
        return mDebug;
    }

    @SuppressWarnings("WeakerAccess")
    public void setToolchain(CPPToolchains.Toolchain toolchain) {
        this.mToolchain = toolchain;
    }

    @SuppressWarnings("WeakerAccess")
    public CPPToolchains.Toolchain getToolchain() {
        return mToolchain;
    }

    @SuppressWarnings("WeakerAccess")
    public String getUnknownToolchainName() {
        return mUnknownToolchainName;
    }

    @SuppressWarnings("WeakerAccess,unused")
    public void setUnknownToolchainName(String unknownToolchainName) {
        this.mUnknownToolchainName = unknownToolchainName;
    }

    private CPPToolchains.Toolchain createGDBToolchain(){
        if(mToolchain == null){
            CPPToolchains cpptoolchains = CPPToolchains.getInstance();
            Intrinsics.checkExpressionValueIsNotNull(cpptoolchains, "CPPToolchains.getInstance()");
            return new ToolsetlessGDBToolchain(cpptoolchains.getOSType(), mDebug);
        }

        return mToolchain;
    }
}
