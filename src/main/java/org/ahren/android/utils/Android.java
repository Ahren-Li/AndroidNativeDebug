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

import com.android.sdklib.SdkVersionInfo;
import com.intellij.openapi.util.SystemInfo;

public class Android {

    public static final String ADB_PATH = "/platform-tools/adb" + (SystemInfo.isWindows ? ".exe" : "");

    public enum Level{
        Android100(1),
        Android110(2),
        Android150(3),
        Android160(4),
        Android200(5),
        Android201(6),
        Android210(7),
        Android220(8),
        Android230(9),
        Android233(10),
        Android300(11),
        Android310(12),
        Android320(13),
        Android400(14),
        Android403(15),
        Android410(16),
        Android420(17),
        Android430(18),
        Android440(19),
        Android44W(20),
        Android500(21),
        Android510(22),
        Android600(23),
        Android700(24),
        Android711(25),
        Android800(26),
        Android810(27);


        int api;
        Level(int api){
            this.api = api;
        }


        public int getApi() {
            return api;
        }

        @Override
        public String toString() {
            return "Android " + SdkVersionInfo.getVersionString(api);
        }
    }

}
