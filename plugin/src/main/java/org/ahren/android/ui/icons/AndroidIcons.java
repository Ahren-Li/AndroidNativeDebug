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

package org.ahren.android.ui.icons;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class AndroidIcons {

    public static class Ddms {
        public static final Icon AllocationTracker = AndroidIcons.load("/icons/ddms/allocation_tracker.png");
        public static final Icon AttachDebugger = AndroidIcons.load("/icons/ddms/attachDebugger.png");
        public static final Icon Gc = AndroidIcons.load("/icons/ddms/cause_garbage_collection.png");
        public static final Icon DumpHprof = AndroidIcons.load("/icons/ddms/dump_hprof_file.png");
        public static final Icon Emulator = AndroidIcons.load("/icons/ddms/emulator.png");
        public static final Icon Emulator2 = AndroidIcons.load("/icons/ddms/emulator_02.png");
        public static final Icon FileExplorer = AndroidIcons.load("/icons/ddms/file_explorer.png");
        public static final Icon Heap = AndroidIcons.load("/icons/ddms/heap.png");
        public static final Icon HeapInfo = AndroidIcons.load("/icons/ddms/heap_info.png");
        public static final Icon Logcat = AndroidIcons.load("/icons/ddms/logcat.png");
        public static final Icon LogcatAutoFilterSelectedPid = AndroidIcons.load("/icons/ddms/logcat_filter_pid.png");
        public static final Icon RealDevice = AndroidIcons.load("/icons/ddms/real_device.png");
        public static final Icon EmulatorDevice = AndroidIcons.load("/icons/ddms/emulator_device.png");
        public static final Icon ScreenCapture = AndroidIcons.load("/icons/ddms/screen_capture.png");
        public static final Icon StartMethodProfiling = AndroidIcons.load("/icons/ddms/start_method_profiling.png");
        public static final Icon Threads = AndroidIcons.load("/icons/ddms/threads.png");
        public static final Icon SysInfo = AndroidIcons.load("/icons/ddms/sysinfo.png");
        public static final Icon ScreenRecorder = AndroidIcons.load("/icons/ddms/screen_recorder.png");
    }

    private static Icon load(String path) {
        return IconLoader.getIcon(path, AndroidIcons.class);
    }
}
