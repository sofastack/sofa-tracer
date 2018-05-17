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

import com.alipay.common.tracer.core.appender.manager.AsyncCommonAppenderManager;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.common.tracer.core.utils.TracerUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * tracer自己的日志，仅供内部试用
 *
 * @author yangyanzhao
 */
public class SelfLog {

    static private final String               ERROR_PREFIX  = "[ERROR] ";
    static private final String               WARN_PREFIX   = "[WARN]  ";
    static private final String               INFO_PREFIX   = "[INFO]  ";

    /***
     * 日志文件名称
     */
    static protected final String             SELF_LOG_FILE = "tracer-self.log";

    static private AsyncCommonAppenderManager selfLogAppenderManager;

    static {
        selfLogAppenderManager = new AsyncCommonAppenderManager(1024, SELF_LOG_FILE);
        selfLogAppenderManager.start("SelfLogAppender");
    }

    /**
     * @param log 日志内容
     * @param e 异常
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
            selfLogAppenderManager.append(sw.toString());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * 打印错误日志，附加当前线程的 TraceId
     *
     * @param log 错误信息
     * @param e   异常
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
            selfLogAppenderManager.append(sw.toString());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void error(String log) {
        doLog(log, ERROR_PREFIX);
    }

    /**
     * 打印错误日志，附加当前线程的 TraceId
     *
     * @param log 错误消息
     * @param traceId traceId
     */
    public static void errorWithTraceId(String log, String traceId) {
        doLog(log, ERROR_PREFIX + "[" + traceId + "]");
    }

    /**
     * 打印错误日志，附加当前线程的 TraceId
     *
     * @param log 错误消息
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
        //async flush, do nothing.
    }

    static private void doLog(String log, String prefix) {
        try {
            String timestamp = Timestamp.currentTime();
            StringBuilder sb = new StringBuilder();
            sb.append(timestamp).append(prefix).append(log).append(StringUtils.NEWLINE);
            selfLogAppenderManager.append(sb.toString());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
