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

package org.ahren.android.run.configuration.lldb;

import org.ahren.android.adb.devices.ConnectedAndroidDevice;
import org.ahren.android.utils.AndroidBundle;

import java.io.File;

public class LLDBTools {

    public static String getLLDB_server(String execPath, ConnectedAndroidDevice device){
        if(execPath == null || device == null || execPath.isEmpty()) return "";

        String path = execPath + File.separator + "android" + File.separator;
        switch (device.getAbis().get(0).toString()){
            case "arm64-v8a":
                path += "arm64-v8a";
                break;
            case "armeabi-v7a":
            case "armeabi":
                path += "armeabi";
                break;
            case "x86":
                path += "x86";
                break;
            case "x86_64":
                path += "x86_64";
                break;
        }
        path += File.separator + AndroidBundle.message("android.lldb_server.name");
        return path;
    }
}
