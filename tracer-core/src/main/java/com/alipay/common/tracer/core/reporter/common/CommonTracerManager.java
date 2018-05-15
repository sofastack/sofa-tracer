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
package com.alipay.common.tracer.core.reporter.common;

import com.alipay.common.tracer.core.appender.TraceAppender;
import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.appender.file.LoadTestAwareAppender;
import com.alipay.common.tracer.core.appender.manager.AsyncCommonDigestAppenderManager;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.reporter.type.TracerSystemLogEnum;
import com.alipay.common.tracer.core.span.CommonLogSpan;
import com.alipay.common.tracer.core.utils.StringUtils;

/**
 * CommonTracerManager
 *
 * @author yangguanchao
 * @since 2017/06/28
 */
public class CommonTracerManager {

    /**
     *
     * 异步日志打印，所有的中间件公用一个来打印通用日志
     */
    private static volatile AsyncCommonDigestAppenderManager commonReporterAsyncManager = new AsyncCommonDigestAppenderManager(
                                                                                            1024);

    private static SpanEncoder                               commonSpanEncoder          = new CommonSpanEncoder();

    static {
        String logName = TracerSystemLogEnum.MIDDLEWARE_ERROR.getDefaultLogName();
        TraceAppender traceAppender = LoadTestAwareAppender
            .createLoadTestAwareTimedRollingFileAppender(logName, SofaTracerConfiguration
                .getProperty(TracerSystemLogEnum.MIDDLEWARE_ERROR.getRollingKey()),
                SofaTracerConfiguration.getProperty(TracerSystemLogEnum.MIDDLEWARE_ERROR
                    .getLogReverseKey()));
        commonReporterAsyncManager.addAppender(logName, traceAppender, commonSpanEncoder);

        String profileLogName = TracerSystemLogEnum.RPC_PROFILE.getDefaultLogName();
        TraceAppender profileTraceAppender = LoadTestAwareAppender
            .createLoadTestAwareTimedRollingFileAppender(profileLogName, SofaTracerConfiguration
                .getProperty(TracerSystemLogEnum.RPC_PROFILE.getRollingKey()),
                SofaTracerConfiguration.getProperty(TracerSystemLogEnum.RPC_PROFILE
                    .getLogReverseKey()));
        commonReporterAsyncManager.addAppender(profileLogName, profileTraceAppender,
            commonSpanEncoder);
        //start
        commonReporterAsyncManager.start("CommonProfileErrorAppender");
    }

    /***
     * 注册一个通用日志
     * @param logFileName 日志文件名字,同时也是作为日志文件的类型标识
     * @param rollingPolicy 滚动策略
     * @param logReserveDay 保留天数
     */
    public static void register(String logFileName, String rollingPolicy, String logReserveDay) {
        if (StringUtils.isBlank(logFileName)) {
            return;
        }
        if (commonReporterAsyncManager.isAppenderAndEncoderExist(logFileName)) {
            SelfLog.warn(logFileName + " has existed in CommonTracerManager");
            return;
        }
        TraceAppender traceAppender = LoadTestAwareAppender
            .createLoadTestAwareTimedRollingFileAppender(logFileName, rollingPolicy, logReserveDay);
        commonReporterAsyncManager.addAppender(logFileName, traceAppender, commonSpanEncoder);
    }

    /***
     * 废弃
     * @param logType 类型
     * @param logFileName 文件名称
     * @param rollingPolicy 滚动策略
     * @param logReserveDay 保留天数
     */
    @Deprecated
    public static void register(char logType, String logFileName, String rollingPolicy,
                                String logReserveDay) {
        String logTypeStr = new String(new char[] { logType });
        if (CommonTracerManager.isAppenderExist(logTypeStr)) {
            SelfLog.warn(logTypeStr + " has existed in CommonTracerManager");
            return;
        }
        TraceAppender traceAppender = LoadTestAwareAppender
            .createLoadTestAwareTimedRollingFileAppender(logFileName, rollingPolicy, logReserveDay);
        commonReporterAsyncManager.addAppender(logTypeStr, traceAppender, commonSpanEncoder);
    }

    /***
     * 判断指定日志类型的输出是否符合要求
     * @param logType 日志类型
     * @return true 已经存在
     */
    public static boolean isAppenderExist(String logType) {
        if (StringUtils.isBlank(logType)) {
            return false;
        }
        return commonReporterAsyncManager.isAppenderAndEncoderExist(logType);
    }

    /***
     * 注意:这个 {@link CommonLogSpan} 的 logType 一定要设置,否则无法打印
     * @param commonLogSpan 要被打印的 Span
     */
    public static void reportCommonSpan(CommonLogSpan commonLogSpan) {
        if (commonLogSpan == null) {
            return;
        }
        String logType = commonLogSpan.getLogType();
        if (StringUtils.isBlank(logType)) {
            SelfLog.error("Log Type can't be empty when report!");
            return;
        }
        commonReporterAsyncManager.append(commonLogSpan);
    }

    public static void reportProfile(CommonLogSpan sofaTracerSpan) {
        if (sofaTracerSpan == null) {
            return;
        }
        sofaTracerSpan.setLogType(TracerSystemLogEnum.RPC_PROFILE.getDefaultLogName());
        commonReporterAsyncManager.append(sofaTracerSpan);

    }

    public static void reportError(CommonLogSpan sofaTracerSpan) {
        if (sofaTracerSpan == null) {
            return;
        }
        sofaTracerSpan.setLogType(TracerSystemLogEnum.MIDDLEWARE_ERROR.getDefaultLogName());
        commonReporterAsyncManager.append(sofaTracerSpan);
    }
}
