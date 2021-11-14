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

package org.ahren.android.ui.dialog;

import org.ahren.android.adb.AdbProcess;
import org.ahren.android.ui.ProcessRenderer;
import org.ahren.android.ui.model.AndroidListModel;
import com.android.ddmlib.IShellOutputReceiver;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import org.ahren.android.adb.devices.ConnectedAndroidDevice;
import org.ahren.android.utils.Android;
import org.ahren.android.utils.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;

public class AndroidProcessDialog extends DialogWrapper implements Disposable {

    private final static Logger LOG = Log.factory("AndroidProcessDialog");
    private final static boolean DEBUG = false;

    private JPanel mPanel;
    private JBList<AdbProcess> mProcessList;
    private JScrollPane mScrollPane;
    private JBTextField mSearchText;
    private ArrayList<AdbProcess> mData;
    private AdbProcess mSelectProcess;
    private String mSearchString = "";

    public AndroidProcessDialog(@Nullable Project project, @NotNull ConnectedAndroidDevice device) {
        super(project);
        mData = new ArrayList<>();
        try {
            String ps = "ps";
            if (device.getVersion().getApiLevel() > Android.Level.Android711.getApi()){
                ps += " -A";
            }
            device.getDevice().executeShellCommand(ps , new ProcessListen());
        }catch (Exception e){
            e.printStackTrace();
        }

        AndroidListModel<AdbProcess> model = new AndroidListModel<>(mData);
        mProcessList.setModel(model);
        mProcessList.setCellRenderer(new ProcessRenderer());
        mProcessList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        new ProcessDoubleClickListener().installOn(mProcessList);
        setTitle("Select Debug Process");
        setModal(true);
        setOKActionEnabled(true);
        init();
    }

    private void createUIComponents() {
        mProcessList = new JBList<>();
        mProcessList.setEmptyText("no debug process");
        mProcessList.setAutoscrolls(true);
        mScrollPane = ScrollPaneFactory.createScrollPane(mProcessList);
        mScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mSearchText = new JBTextField();
        mSearchText.addCaretListener(new TextChangeListen());
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return mPanel;
    }

    @Override
    public void dispose() {
        super.dispose();

    }

    @Override
    protected void doOKAction() {
        mSelectProcess = mProcessList.getSelectedValue();
        super.doOKAction();
    }

    public boolean needShow(String processName){
        if(mData.isEmpty()) return false;

        for(AdbProcess process : mData){
            if(process.getName().contains(processName)){
                return false;
            }
        }

        return true;
    }

    public AdbProcess getSelectProcess() {
        return mSelectProcess;
    }

    private void doSearch(String text){

        ArrayList<AdbProcess> searchList = new ArrayList<>();
        if(text.isEmpty()){
            searchList.addAll(mData);
        }else {
            for(AdbProcess process : mData){
                if(process.getName().contains(text)){
                    searchList.add(process);
                }
            }
        }

        AndroidListModel<AdbProcess> model = new AndroidListModel<>(searchList);
        mProcessList.setModel(model);
    }

    class ProcessListen implements IShellOutputReceiver{

        StringBuffer buffer = new StringBuffer();
        boolean isCancelled = false;

        @Override
        public void addOutput(byte[] bytes, int i, int i1) {
            buffer.append(new String(bytes, i, i1));
        }

        @Override
        public void flush() {
            if(DEBUG) LOG.info("flush: ");
            try {
                StringReader reader = new StringReader(buffer.toString());
                BufferedReader buffer = new BufferedReader(reader);
                String line;
                while ((line = buffer.readLine()) != null){
                    if(DEBUG) LOG.info("line: " + line);
                    if(line.contains("PID")) continue;
                    String[] process = line.split(" +");
                    if(process.length >= 2){
                        String name = process[process.length - 1];
                        String pid = process[process.length == 2 ? 0 : 1];
                        if(!name.trim().isEmpty()){
                            try {
                                int iPid = Integer.parseInt(pid);
                                AdbProcess adbProcess = new AdbProcess(name, iPid);
                                if(DEBUG) LOG.info("process: " + name + " " + pid);
                                if(!mData.contains(adbProcess)){
                                    mData.add(adbProcess);
                                }
                            }catch (Exception ignore){}
                        }
                    }
                }

                if(DEBUG) LOG.info("mData.size(): " + mData.size());
                AndroidListModel<AdbProcess> model = new AndroidListModel<>(mData);
                mProcessList.setModel(model);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public boolean isCancelled() {
            return isCancelled;
        }
    }

    class TextChangeListen implements CaretListener {

        @Override
        public void caretUpdate(CaretEvent e) {
            String text = mSearchText.getText();
            if(text != null && !text.equals(mSearchString)){
                mSearchString = text;
                doSearch(text);
            }
        }
    }

    class ProcessDoubleClickListener extends DoubleClickListener {

        @Override
        protected boolean onDoubleClick(MouseEvent mouseEvent) {
            Action okAction = getOKAction();
            if (okAction.isEnabled()) {
                okAction.actionPerformed(null);
                return true;
            } else {
                return false;
            }
        }
    }
}
