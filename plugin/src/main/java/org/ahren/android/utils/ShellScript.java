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

import java.io.File;
import java.io.FileWriter;

public class ShellScript {
    private static final String HEAD = "#!/system/bin/sh";

    private StringBuffer mBuffer = new StringBuffer();

    public ShellScript(){
        mBuffer.append(HEAD);
        mBuffer.append("\n");
    }

    public void addCommand(String cmd){
        mBuffer.append(cmd);
        mBuffer.append("\n");
    }

    public void writeToFile(String path){
        if(path == null || path.isEmpty()) return;

        File file = new File(path);
//        if(!file.canWrite()) return;
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(mBuffer.toString());
            writer.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
