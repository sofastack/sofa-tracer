/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.common.tracer.core.appender;

import java.io.File;
import java.nio.charset.Charset;

import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.appender.self.TracerDaemon;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.common.tracer.core.utils.TracerUtils;

/**
 * TracerLogRootDaemon
 * <p>
 * Not obtained from the configuration project, obtained directly from the system properties
 *
 * @author yangguanchao
 * @since 2017/06/25
 */
public class TracerLogRootDaemon {

    /**
     * Whether to add pid to log path
     */
    public static final String  TRACER_APPEND_PID_TO_LOG_PATH_KEY = "tracer_append_pid_to_log_path";

    /**
     * Log directory
     */
    public static String        LOG_FILE_DIR;
    /**
     * The encoding is determined by LANG or -Dfile.encoding,
     * so if the system determines the log encoding based on the system encoding,
     * make sure that the application's startup script or startup parameters do not override the LANG or -Dfile.encoding parameters.
     * Generally speaking, in the domestic system creation template,
     * there is LANG=zh_CN.GB18030 in deploy/bin/templates/jbossctl.sh,
     * so no matter what value LANG is set in the environment variable, it will be overwritten at startup.
     */
    static public final Charset DEFAULT_CHARSET                   = Charset.defaultCharset();

    static {
        String loggingRoot = System.getProperty("SOFA_TRACER_LOGGING_PATH");
        if (StringUtils.isBlank(loggingRoot)) {
            loggingRoot = System.getenv("SOFA_TRACER_LOGGING_PATH");
        }
        if (StringUtils.isBlank(loggingRoot)) {
            loggingRoot = System.getProperty("loggingRoot");
        }
        if (StringUtils.isBlank(loggingRoot)) {
            loggingRoot = System.getProperty("logging.path");
        }
        if (StringUtils.isBlank(loggingRoot)) {
            loggingRoot = System.getProperty("user.home") + File.separator + "logs";
        }

        String appendPidToLogPathString = System.getProperty(TRACER_APPEND_PID_TO_LOG_PATH_KEY);
        boolean appendPidToLogPath = "true".equalsIgnoreCase(appendPidToLogPathString);

        String tempLogFileDir = loggingRoot + File.separator + "tracelog";

        if (appendPidToLogPath) {
            tempLogFileDir = tempLogFileDir + File.separator + TracerUtils.getPID();
        }

        LOG_FILE_DIR = tempLogFileDir;

        try {
            TracerDaemon.start();
            SelfLog.info("LOG_FILE_DIR is " + LOG_FILE_DIR);
        } catch (Throwable e) {
            SelfLog.error("Failed to start Tracer Daemon Thread", e);
        }
    }
}
