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
package com.alipay.common.tracer.core.appender.self;

import com.alipay.common.tracer.core.appender.file.AbstractRollingFileAppender;
import com.alipay.common.tracer.core.appender.file.TimedRollingFileAppender;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.common.tracer.core.utils.TracerUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * Self-synchronized SelfLog, used only in the middleware,
 * after Solving SelfLog asynchronous, it is used when printing logs in some Manager initialization
 *
 * @author luoguimu123
 * @version $Id: SynchronizingSelfLog.java, v 0.1 November 21, 2017 7:45 PM luoguimu123 Exp $
 */
public class SynchronizingSelfLog {

    static private final String                ERROR_PREFIX  = "[ERROR] ";
    static private final String                WARN_PREFIX   = "[WARN]  ";
    static private final String                INFO_PREFIX   = "[INFO]  ";

    /**
     * Log file name
     */
    static protected final String              SELF_LOG_FILE = "sync.log";

    static private AbstractRollingFileAppender selfAppender;

    static {

        String globalLogReserveDay = SofaTracerConfiguration.getProperty(
            SofaTracerConfiguration.TRACER_GLOBAL_LOG_RESERVE_DAY,
            String.valueOf(SofaTracerConfiguration.DEFAULT_LOG_RESERVE_DAY));
        String rollingPolicy = SofaTracerConfiguration
            .getProperty(SofaTracerConfiguration.TRACER_GLOBAL_ROLLING_KEY);

        if (StringUtils.isBlank(rollingPolicy)) {
            rollingPolicy = TimedRollingFileAppender.DAILY_ROLLING_PATTERN;
        }

        selfAppender = new TimedRollingFileAppender(SELF_LOG_FILE, rollingPolicy,
            String.valueOf(globalLogReserveDay));
    }

    /**
     * @param log
     * @param e
     */
    public static void error(String log, Throwable e) {
        try {
            String timestamp = Timestamp.currentTime();
            StringWriter sw = new StringWriter(4096);
            PrintWriter pw = new PrintWriter(sw, false);
            pw.append(timestamp).append(ERROR_PREFIX).append(log).append(StringUtils.NEWLINE);
            e.printStackTrace(pw);
            pw.println();
            pw.flush();
            selfAppender.append(sw.toString());
            selfAppender.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Print error log with current thread's TraceId
     *
     * @param log
     * @param e
     */
    public static void errorWithTraceId(String log, Throwable e) {
        try {
            String timestamp = Timestamp.currentTime();
            StringWriter sw = new StringWriter(4096);
            PrintWriter pw = new PrintWriter(sw, false);
            pw.append(timestamp).append(ERROR_PREFIX).append("[").append(TracerUtils.getTraceId())
                .append("]").append(log).append(StringUtils.NEWLINE);
            e.printStackTrace(pw);
            pw.println();
            pw.flush();
            selfAppender.append(sw.toString());
            selfAppender.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void error(String log) {
        doLog(log, ERROR_PREFIX);
    }

    /**
     * Print error log with current thread's TraceId
     *
     * @param log
     * @param traceId traceId
     */
    public static void errorWithTraceId(String log, String traceId) {
        doLog(log, ERROR_PREFIX + "[" + traceId + "]");
    }

    /**
     * Print error log with current thread's TraceId
     *
     * @param log
     */
    public static void errorWithTraceId(String log) {
        doLog(log, ERROR_PREFIX + "[" + TracerUtils.getTraceId() + "]");
    }

    public static void warn(String log) {
        doLog(log, WARN_PREFIX);
    }

    public static void info(String log) {
        doLog(log, INFO_PREFIX);
    }

    public static void infoWithTraceId(String log) {
        doLog(log, INFO_PREFIX + "[" + TracerUtils.getTraceId() + "]");
    }

    public static void flush() {
        selfAppender.flush();
    }

    static private void doLog(String log, String prefix) {
        try {
            String timestamp = Timestamp.currentTime();
            StringBuilder sb = new StringBuilder();
            sb.append(timestamp).append(prefix).append(log).append(StringUtils.NEWLINE);
            selfAppender.append(sb.toString());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}