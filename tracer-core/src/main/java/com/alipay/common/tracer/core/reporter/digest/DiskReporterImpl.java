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
package com.alipay.common.tracer.core.reporter.digest;

import com.alipay.common.tracer.core.appender.TraceAppender;
import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.appender.file.LoadTestAwareAppender;
import com.alipay.common.tracer.core.appender.manager.AsyncCommonDigestAppenderManager;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.reporter.digest.manager.SofaTracerDigestReporterAsyncManager;
import com.alipay.common.tracer.core.reporter.stat.SofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.AssertUtils;
import com.alipay.common.tracer.core.utils.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DiskReporterImpl
 * 内部定制化实现
 *
 * @author yangguanchao
 * @since 2017/07/14
 */
public class DiskReporterImpl extends AbstractDiskReporter {

    /***
     * 标识初始状态: lazy 初始化磁盘文件,用到在初始化,注意并发初始化逻辑
     */
    private final AtomicBoolean         isDigestFileInited = new AtomicBoolean(false);

    private final String                digestLogType;

    private final String                digestRollingPolicy;

    private String                      digestLogReserveConfig;

    private final SpanEncoder           contextEncoder;

    private String                      logNameKey;

    /***
     * 统计实现,需要用户实现一个如何统计的方法,默认提供了累加的操作
     */
    private SofaTracerStatisticReporter statReporter;

    public DiskReporterImpl(String digestLogType, SpanEncoder contextEncoder) {

        this(digestLogType, StringUtils.EMPTY_STRING, StringUtils.EMPTY_STRING, contextEncoder,
            null);
    }

    public DiskReporterImpl(String digestLogType, String digestRollingPolicy,
                            String digestLogReserveConfig, SpanEncoder contextEncoder) {

        this(digestLogType, digestRollingPolicy, digestLogReserveConfig, contextEncoder, null);
    }

    public DiskReporterImpl(String digestLogType, String digestRollingPolicy,
                            String digestLogReserveConfig, SpanEncoder contextEncoder,
                            SofaTracerStatisticReporter statReporter) {
        this(digestLogType, digestRollingPolicy, digestLogReserveConfig, contextEncoder,
            statReporter, null);
    }

    /***
     *
     * @param digestLogType 日志类型
     * @param digestRollingPolicy 滚动策略
     * @param digestLogReserveConfig 保留天数配置
     * @param contextEncoder 日志输出编码
     * @param statReporter 用户需要提供统计实现
     * @param logNameKey 日志文件配置关键字
     */
    public DiskReporterImpl(String digestLogType, String digestRollingPolicy,
                            String digestLogReserveConfig, SpanEncoder contextEncoder,
                            SofaTracerStatisticReporter statReporter, String logNameKey) {
        AssertUtils.hasText(digestLogType, "digestLogType can't be empty");

        this.digestLogType = digestLogType;
        this.digestRollingPolicy = digestRollingPolicy;
        this.digestLogReserveConfig = digestLogReserveConfig;
        this.contextEncoder = contextEncoder;
        //注册统计实现
        this.statReporter = statReporter;
        this.logNameKey = logNameKey;
    }

    public SofaTracerStatisticReporter getStatReporter() {
        return statReporter;
    }

    public void setStatReporter(SofaTracerStatisticReporter statReporter) {
        this.statReporter = statReporter;
    }

    @Override
    public String getDigestReporterType() {
        return this.digestLogType;
    }

    @Override
    public String getStatReporterType() {
        if (statReporter != null) {
            //日志文件名字
            return statReporter.getStatTracerName();
        }
        return StringUtils.EMPTY_STRING;
    }

    @Override
    public void digestReport(SofaTracerSpan span) {
        //lazy 初始化
        if (!this.isDigestFileInited.get()) {
            this.initDigestFile();
        }
        AsyncCommonDigestAppenderManager asyncDigestManager = SofaTracerDigestReporterAsyncManager
            .getSofaTracerDigestReporterAsyncManager();
        if (asyncDigestManager.isAppenderAndEncoderExist(this.digestLogType)) {
            //同时存在 appender 和 encoder 才打印
            asyncDigestManager.append(span);
        } else {
            SelfLog.warn(span.toString() + " have no logType set, so ignore data persistence.");
        }
    }

    @Override
    public void statisticReport(SofaTracerSpan span) {
        if (this.statReporter != null) {
            this.statReporter.reportStat(span);
        }
    }

    public AtomicBoolean getIsDigestFileInited() {
        return isDigestFileInited;
    }

    public String getDigestLogType() {
        return digestLogType;
    }

    public String getDigestRollingPolicy() {
        return digestRollingPolicy;
    }

    public String getDigestLogReserveConfig() {
        return digestLogReserveConfig;
    }

    public SpanEncoder getContextEncoder() {
        return contextEncoder;
    }

    public String getLogNameKey() {
        return logNameKey;
    }

    /***
     * 磁盘文件初始化创建完成
     */
    private synchronized void initDigestFile() {
        if (this.isDigestFileInited.get()) {
            //double check init
            return;
        }
        if (StringUtils.isNotBlank(logNameKey)) {
            String currentDigestLogReserveConfig = SofaTracerConfiguration
                .getLogReserveConfig(logNameKey);
            if (!currentDigestLogReserveConfig.equals(digestLogReserveConfig)) {
                SelfLog.info("the lognamekey : " + logNameKey
                             + " take effect. the old logreserveconfig is "
                             + digestLogReserveConfig + " and " + "the new logreverseconfig is "
                             + currentDigestLogReserveConfig);
                digestLogReserveConfig = currentDigestLogReserveConfig;
            }
        }
        TraceAppender digestTraceAppender = LoadTestAwareAppender
            .createLoadTestAwareTimedRollingFileAppender(this.digestLogType,
                this.digestRollingPolicy, this.digestLogReserveConfig);
        //注册 digest
        AsyncCommonDigestAppenderManager asyncDigestManager = SofaTracerDigestReporterAsyncManager
            .getSofaTracerDigestReporterAsyncManager();
        if (!asyncDigestManager.isAppenderAndEncoderExist(this.digestLogType)) {
            asyncDigestManager.addAppender(this.digestLogType, digestTraceAppender,
                this.contextEncoder);
        }
        //已经存在或者首次创建
        this.isDigestFileInited.set(true);
    }
}
