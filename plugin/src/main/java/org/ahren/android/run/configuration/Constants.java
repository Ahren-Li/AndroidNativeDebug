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

public class Constants {
    public static final String LLDB_SERVER_START_SCRIPT = "start_lldb_server.sh";
    public static final String LLDB_SERVER = "lldb-server";
    public static final String DEVICE_TEMP_PATH = "/data/local/tmp";
    public static final String PLATFORM_SOCKET_SCHEME = "unix-abstract";
    public static final String LLDB_TARGET_DATA_DIR = "lldb";
    public static final String LLDB_BIN_TARGET_DATA_DIR = "bin";
    public static final String LLDB_LOG_TARGET_DATA_DIR = "log";
    public static final String LLDB_TMP_TARGET_DATA_DIR = "tmp";
    public static final String LLDB_SERVER_LOG_FILE = "gdb-server.log";
    public static final String LLDB_PLATFORM_LOG_FILE = "platform.log";
    public static final String YAMA_PTRACE_SCOPE = "/proc/sys/kernel/yama/ptrace_scope";

    public Constants() {
    }
}
