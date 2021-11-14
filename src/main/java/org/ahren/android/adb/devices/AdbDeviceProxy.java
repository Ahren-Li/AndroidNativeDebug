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

package org.ahren.android.adb.devices;

import com.android.ddmlib.*;
import com.intellij.openapi.diagnostic.Logger;
import org.ahren.android.utils.Log;

public class AdbDeviceProxy {
    private final static Logger LOG = Log.factory("AdbDeviceProxy");

    private boolean mSupportToyBox = false;
    private ConnectedAndroidDevice mDevice;

    public AdbDeviceProxy(String serial){
        IDevice device = getDeviceBySerial(serial);
        if(device != null){
            mDevice = new ConnectedAndroidDevice(device);
            initDevice(mDevice);
        }
    }

    public ConnectedAndroidDevice getDevice(){
        return mDevice;
    }

    public boolean doRootWait(){
        if(mDevice == null){
            LOG.error("doRootWait: mDevice is null!!");
            return false;
        }

        if(mDevice.isRoot()) return true;

        mDevice.doRoot();

        IDevice newDevice = waitDevice();
        if(newDevice == null){
            LOG.error("doRootWait: can't wait new device:" + mDevice.getSerial());
            return false;
        }

        mDevice = new ConnectedAndroidDevice(newDevice);
        return true;
    }

    public boolean pushFile(String src, String dts){
        if(mDevice == null) return false;
        return mDevice.pushFile(src,dts);
    }

    public boolean exec(String cmd){
        if(mDevice == null) return false;

        StringBuffer buffer = new StringBuffer();
        boolean result = execWithResult(cmd, buffer);
//        mDevice.execCmd(cmd, new IShellOutputReceiver() {
//            @Override
//            public void addOutput(byte[] bytes, int i, int i1) {
//                StringReader reader = new StringReader(new String(bytes, i, i1));
//                BufferedReader buffer = new BufferedReader(reader);
//                String line;
//                try {
//                    while ((line = buffer.readLine()) != null){
//                        LOG.info("exec:" + line);
//                    }
//                }catch (Exception ignore){}
//            }
//
//            @Override
//            public void flush() {
//
//            }
//
//            @Override
//            public boolean isCancelled() {
//                return false;
//            }
//        });
        LOG.info("exec end");
        return result;
    }

    public boolean isSupportToybox(){
        return mSupportToyBox;
    }

    private boolean execWithResult(String cmd, StringBuffer buffer){
        if(mDevice == null) return false;
        boolean result =  mDevice.execCmd(cmd, new ShellOutputReceiver(buffer));
        LOG.info("execWithResult:" + buffer.toString());
        return result;
    }

    private void initDevice(ConnectedAndroidDevice device){
        if(mDevice == null) return ;
        StringBuffer result = new StringBuffer();
        execWithResult("which toybox", result);
        mSupportToyBox = result.toString().contains("toybox");
    }

    private IDevice waitDevice(){
        if(mDevice == null) return null;
        int retry = 20;
        IDevice newDevice;
        do{
            retry --;
            newDevice = getDeviceBySerial(mDevice.getSerial());
            LOG.info("wait adb device:" + mDevice.getSerial());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while (newDevice == null && retry > 0);

        return newDevice;
    }

    private IDevice getDeviceBySerial(String serial){
        AndroidDebugBridge bridge = AndroidDebugBridge.getBridge();
        if (bridge != null && bridge.isConnected()){
            IDevice[] devices = bridge.getDevices();
            for(IDevice device : devices){
                if(serial.equals(device.getSerialNumber())){
                    return device;
                }
            }
        }

        return null;
    }

    public static class ShellOutputReceiver implements IShellOutputReceiver{

        StringBuffer buffer;

        public ShellOutputReceiver(StringBuffer buffer){
            this.buffer = buffer;
        }

        @Override
        public void addOutput(byte[] bytes, int i, int i1) {
            if(buffer != null) buffer.append(new String(bytes, i, i1));
        }

        @Override
        public void flush() {}

        @Override
        public boolean isCancelled() {
            return false;
        }
    }
}
