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

package org.ahren.android.adb;

public class AdbProcess {

    private String mName;
    private int mPid;

    public AdbProcess(String name, int pid){
        mName = name;
        mPid = pid;
    }

    public String getName() {
        return mName;
    }

    public int getPid() {
        return mPid;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AdbProcess){
            return mPid == ((AdbProcess) obj).mPid;
        }
        return false;
    }
}
