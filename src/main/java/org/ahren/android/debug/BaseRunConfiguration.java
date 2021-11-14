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

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import org.ahren.android.utils.Configuration;
import kotlin.ranges.IntRange;
import kotlin.ranges.RangesKt;
import org.ahren.android.utils.Log;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class BaseRunConfiguration extends RunConfigurationBase
        implements RunConfigurationWithSuppressedDefaultRunAction, IAndroidRunProfile {

    private final static boolean DEBUG = false;
    private final static Logger LOG = Log.factory("BaseRunConfiguration");

    private SymbolCreator mCreator;
    private AndroidDebugParameters mParameters;

    protected BaseRunConfiguration(@NotNull Project project, @Nullable ConfigurationFactory factory, String name, int type) {
        super(project, factory, name);
        mParameters = new AndroidDebugParameters();
        mCreator = new SymbolCreator(type);
    }

    @SuppressWarnings("WeakerAccess")
    public void setParameters(AndroidDebugParameters parameters) {
        this.mParameters = parameters;
    }

    @SuppressWarnings("WeakerAccess")
    public AndroidDebugParameters getParameters() {
        return mParameters;
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new BaseJetbrainsEditor<>(getProject(), 0);
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);

        String remote = element.getAttributeValue(Configuration.REMOTE);
        String launch = element.getAttributeValue(Configuration.ANDROID_LAUNCH);
        String path = element.getAttributeValue(Configuration.ANDROID_PATH);
        String numS = element.getAttributeValue(Configuration.ANDROID_SYMBOL_NUM);
        String adbCanRoot = element.getAttributeValue(Configuration.ANDROID_ADB_CAN_ROOT);

        mCreator.createSymbolPaths(path, launch);
        if(!StringUtil.isEmpty(numS)){
            int num = Integer.parseInt(numS);
            IntRange iterable = RangesKt.until(0, num);
            for(int i : iterable){
                String symbol = element.getAttributeValue(Configuration.ANDROID_SYMBOL_START + i);
                if(!StringUtil.isEmpty(symbol)){
                    mCreator.addSymbol(symbol);
                }
            }
        }

        mParameters = new AndroidDebugParameters();
        mParameters.setAndroidSymbolList(mCreator.getSymbolList());
        mParameters.setRemote(remote);
        mParameters.setAndroidLaunch(launch);
        mParameters.setAndroidPath(path);
        try {
            mParameters.setAdbCanRoot(Boolean.parseBoolean(adbCanRoot));
        }catch (Exception ignore){}
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);


        if(DEBUG){
            String remote = mParameters.getRemote();
            String launch = mParameters.getAndroidLaunch();
            String path = mParameters.getAndroidPath();
            LOG.info("Remote:" + remote);
            LOG.info("Launch:" + launch);
            LOG.info("Android Path:" + path);
            LOG.info("Android Adb Root:" + mParameters.isAdbCanRoot());
        }

        if(mParameters.getRemote() != null){
            element.setAttribute(Configuration.REMOTE, mParameters.getRemote());
        }

        element.setAttribute(Configuration.ANDROID_ADB_CAN_ROOT, mParameters.isAdbCanRoot() + "");

        if(mParameters.getAndroidLaunch() != null){
            element.setAttribute(Configuration.ANDROID_LAUNCH, mParameters.getAndroidLaunch());
        }

        if(mParameters.getAndroidPath() != null){
            element.setAttribute(Configuration.ANDROID_PATH, FileUtil.toSystemIndependentName(mParameters.getAndroidPath()));
        }

        List<String> manual = mCreator.getManualSymbol();
        if(manual != null && manual.size() > 0){
            LOG.info("ANDROID_SYMBOL_NUM:" + manual.size());
            element.setAttribute(Configuration.ANDROID_SYMBOL_NUM, manual.size() + "");
            IntRange iterable = RangesKt.until(0, manual.size());
            for(int i : iterable){
                element.setAttribute(Configuration.ANDROID_SYMBOL_START + i, manual.get(i));
            }
        }
    }

    public SymbolCreator getCreator() {
        return mCreator;
    }
}
