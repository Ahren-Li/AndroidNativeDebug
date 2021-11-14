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

package org.ahren.android.utils;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.log4j.Level;

public class Log {
    private static final boolean DEBUG = true;
    private static final String  TAG = Configuration.PLUGIN_ID;

    public static Logger factory(String tag){
        Logger logger = Logger.getInstance(TAG + "." + tag);
        logger.setLevel(DEBUG ? Level.ALL : Level.OFF);
        return logger;
    }
}
