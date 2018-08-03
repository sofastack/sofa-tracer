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

import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.appender.self.TracerDaemon;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.common.tracer.core.utils.TracerUtils;

import java.io.File;
import java.nio.charset.Charset;

/**
 * TracerLogRootDaemon
 *
 * 不从配置项目获取,直接从系统属性获取
 *
 * @author yangguanchao
 * @since 2017/06/25
 */
public class TracerLogRootDaemon {

    /***
     * 是否添加 pid 到 log path
     */
    public static final String  TRACER_APPEND_PID_TO_LOG_PATH_KEY = "tracer_append_pid_to_log_path";

    /***
     * 日志目录
     */
    public static String        LOG_FILE_DIR;
    /**
     * 该编码由LANG或者-Dfile.encoding决定，因此如系统根据系统编码来决定日志编码， 请确保应用的启动脚本或者启动参数中不覆盖LANG或者-Dfile.encoding的参数。 一般来说国内的系统创建模板里，deploy/bin/templates/jbossctl.sh里有
     * LANG=zh_CN.GB18030，这样无论环境变量里LANG设置成何值，启动时都会被覆盖
     */
    static public final Charset DEFAULT_CHARSET                   = Charset.defaultCharset();

    static {
        String loggingRoot = System.getProperty("loggingRoot");
        if (StringUtils.isBlank(loggingRoot)) {
            loggingRoot = System.getProperty("logging.path");
        }

        String appendPidToLogPathString = System.getProperty(TRACER_APPEND_PID_TO_LOG_PATH_KEY);
        boolean appendPidToLogPath = "true".equalsIgnoreCase(appendPidToLogPathString);

        if (StringUtils.isBlank(loggingRoot)) {
            loggingRoot = System.getProperty("user.home") + File.separator + "logs";
        }

        String tempLogFileDir = loggingRoot + File.separator + "tracelog";

        if (appendPidToLogPath) {
            tempLogFileDir = tempLogFileDir + File.separator + TracerUtils.getPID();
        }

        LOG_FILE_DIR = tempLogFileDir;

        try {
            TracerDaemon.start();
        } catch (Throwable e) {
            SelfLog.error("Failed to start Tracer Daemon Thread", e);
        }
    }
}
