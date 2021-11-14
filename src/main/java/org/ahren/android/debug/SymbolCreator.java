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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import org.ahren.android.utils.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SymbolCreator {

    private static final boolean DEBUG = false;
    private static final Logger LOG = Log.factory("SymbolCreator");

    public static final int GDB_TYPE = 0;
    public static final int LLDB_TYPE = 1;

    private int mType;
    private String mPath;
    private String mLaunch;

    private List<String> mSymbolAuto;
    private List<String> mSymbolManual;

    public SymbolCreator(int type){
        mType = type;
        mSymbolAuto = new ArrayList<>();
        mSymbolManual = new ArrayList<>();
    }

    public void addSymbol(String symbolPath){
        if(!mSymbolManual.contains(symbolPath)){
            mSymbolManual.add(symbolPath);
        }
    }

    public void removeSymbol(String symbolPath){
        mSymbolManual.remove(symbolPath);
    }

    public boolean createSymbolPaths(String path, String launch){
        if(DEBUG) LOG.info("createSymbolPaths:path=" + path + ",launch=" + launch);
        if(!StringUtil.isEmpty(path) && !StringUtil.isEmpty(launch)){
            boolean refresh = false;
            if(!path.equals(mPath)){
                mPath = path;
                refresh = true;
            }
            if(!launch.equals(mLaunch)){
                mLaunch = launch;
                refresh = true;
            }

            if(refresh){
                create(mType);
            }
            return refresh;
        }

        return false;
    }

    public List<String> getSymbolList() {
        List<String> list = new ArrayList<>();
        list.addAll(mSymbolAuto);
        list.addAll(mSymbolManual);
        return list;
    }

    public List<String> getManualSymbol() {
        return new ArrayList<>(mSymbolManual);
    }

    private void create(int type){
        mSymbolAuto = new ArrayList<>();
        switch (type){
            case GDB_TYPE:
                createGDB(mSymbolAuto);
                break;
            case LLDB_TYPE:
                createLLDB(mSymbolAuto);
                break;
        }
    }

    private void createGDB(List<String> symbol){
        String path = mPath + "/out/target/product/" + mLaunch + "/symbols";
        symbol.add(path);
    }

    private void createLLDB(List<String> symbol){
        String path = mPath + "/out/target/product/" + mLaunch + "/symbols";
        if(DEBUG) LOG.info("createLLDB:" + path);
        recursiveAdd(new File(path + "/system/bin"), symbol);
        recursiveAdd(new File(path + "/system/lib"), symbol);
        recursiveAdd(new File(path + "/system/lib64"), symbol);
        recursiveAdd(new File(path + "/system/fake-libs"), symbol);
        recursiveAdd(new File(path + "/system/fake-libs64"), symbol);
        recursiveAdd(new File(path + "/system/vendor/lib"), symbol);
        recursiveAdd(new File(path + "/system/vendor/lib64"), symbol);
    }

    private void recursiveAdd(File root, List<String> symbol){
        if(root.exists() && root.isDirectory()){
            File[] files = root.listFiles();
            assert files != null;
            for(File file : files){
                if(file.isDirectory()){
                    recursiveAdd(file, symbol);
                }
            }
            if(DEBUG) LOG.info("add:" + root.getAbsolutePath());
            symbol.add(root.getAbsolutePath());
        }
    }
}
