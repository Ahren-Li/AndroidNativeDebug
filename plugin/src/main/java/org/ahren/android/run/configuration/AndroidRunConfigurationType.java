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

package org.ahren.android.run.configuration;

import org.ahren.android.run.configuration.gdb.GDBConfigurationFactory;
import org.ahren.android.run.configuration.lldb.LLDBConfigurationFactory;
import org.ahren.android.utils.Configuration;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AndroidRunConfigurationType implements ConfigurationType {

    @Nls
    @Override
    public String getDisplayName() {
        return Configuration.PLUGIN_NAME;
    }

    @Nls
    @Override
    public String getConfigurationTypeDescription() {
        return Configuration.PLUGIN_DESCRIPTION;
    }

    @Override
    public Icon getIcon() {
        return AllIcons.RunConfigurations.Remote;
    }

    @NotNull
    @Override
    public String getId() {
        return Configuration.PLUGIN_ID;
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{new LLDBConfigurationFactory(this),
                new GDBConfigurationFactory(this)};
    }
}
