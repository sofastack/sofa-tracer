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
package com.alipay.common.tracer.core.reporter.digest.event;

import com.alipay.common.tracer.core.appender.TraceAppender;
import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.appender.file.LoadTestAwareAppender;
import com.alipay.common.tracer.core.appender.manager.AsyncCommonDigestAppenderManager;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.reporter.digest.AbstractDiskReporter;
import com.alipay.common.tracer.core.reporter.digest.manager.SofaTracerDigestReporterAsyncManager;
import com.alipay.common.tracer.core.reporter.stat.SofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.AssertUtils;
import com.alipay.common.tracer.core.utils.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Span event disk reporter.
 *
 * @author yuqian
 * @version : SpanEventDiskReporterImpl.java, v 0.1 2025-03-10 14:53 yuqian Exp $$
 */
public class SpanEventDiskReporter extends AbstractDiskReporter {

    private final AtomicBoolean isEventFileInited = new AtomicBoolean(false);

    private final String        eventLogType;

    private final String        eventRollingPolicy;

    private String              eventLogReserveConfig;

    private final SpanEncoder   contextEncoder;

    private String              logNameKey;

    /**
     * Instantiates a new Span event disk reporter.
     *
     * @param eventLogType           the event log type
     * @param eventRollingPolicy     the event rolling policy
     * @param eventLogReserveConfig the digest log reserve config
     * @param contextEncoder         the context encoder
     * @param logNameKey             the log name key
     */
    public SpanEventDiskReporter(String eventLogType, String eventRollingPolicy,
                                 String eventLogReserveConfig, SpanEncoder contextEncoder,
                                 String logNameKey) {
        AssertUtils.hasText(eventLogType, "digestLogType can't be empty");
        this.eventLogType = eventLogType;
        this.eventRollingPolicy = eventRollingPolicy;
        this.eventLogReserveConfig = eventLogReserveConfig;
        this.contextEncoder = contextEncoder;
        this.logNameKey = logNameKey;
    }

    /**
     * Get digest reporter instance type
     *
     * @return
     */
    @Override
    public String getDigestReporterType() {
        return this.eventLogType;
    }

    /**
     * Get stat reporter instance type
     *
     * @return
     */
    @Override
    public String getStatReporterType() {
        return StringUtils.EMPTY_STRING;
    }

    /**
     * print digest log
     *
     * @param span span
     */
    @Override
    public void digestReport(SofaTracerSpan span) {
        //lazy initialization
        if (!this.isEventFileInited.get()) {
            this.initDigestFile();
        }
        AsyncCommonDigestAppenderManager asyncDigestManager = SofaTracerDigestReporterAsyncManager
            .getSofaTracerDigestReporterAsyncManager();
        if (asyncDigestManager.isAppenderAndEncoderExist(this.eventLogType)) {
            //Print only when appender and encoder are present
            asyncDigestManager.append(span);
        } else {
            SelfLog.warn(span.toString() + " have no logType set, so ignore data persistence.");
        }
    }

    /**
     * print stat log
     *
     * @param span span
     */
    @Override
    public void statisticReport(SofaTracerSpan span) {
        return;
    }

    /**
     * Gets event log type.
     *
     * @return the event log type
     */
    public String getEventLogType() {
        return eventLogType;
    }

    /**
     * Gets is event file inited.
     *
     * @return the is event file inited
     */
    public AtomicBoolean getIsEventFileInited() {
        return isEventFileInited;
    }

    public String getEventRollingPolicy() {
        return eventRollingPolicy;
    }

    public String getEventLogReserveConfig() {
        return eventLogReserveConfig;
    }

    public SpanEncoder getContextEncoder() {
        return contextEncoder;
    }

    public String getLogNameKey() {
        return logNameKey;
    }

    private synchronized void initDigestFile() {
        if (this.isEventFileInited.get()) {
            //double check init
            return;
        }
        if (StringUtils.isNotBlank(logNameKey)) {
            String currentDigestLogReserveConfig = SofaTracerConfiguration
                .getLogReserveConfig(logNameKey);
            if (!currentDigestLogReserveConfig.equals(eventLogReserveConfig)) {
                SelfLog.info("the lognamekey : " + logNameKey
                             + " take effect. the old logreserveconfig is " + eventLogReserveConfig
                             + " and " + "the new logreverseconfig is "
                             + currentDigestLogReserveConfig);
                eventLogReserveConfig = currentDigestLogReserveConfig;
            }
        }
        TraceAppender digestTraceAppender = LoadTestAwareAppender
            .createLoadTestAwareTimedRollingFileAppender(this.eventLogType,
                this.eventRollingPolicy, this.eventLogReserveConfig);
        //registry digest
        AsyncCommonDigestAppenderManager asyncDigestManager = SofaTracerDigestReporterAsyncManager
            .getSofaTracerDigestReporterAsyncManager();
        if (!asyncDigestManager.isAppenderAndEncoderExist(this.eventLogType)) {
            asyncDigestManager.addAppender(this.eventLogType, digestTraceAppender,
                this.contextEncoder);
        }
        //Already exists or created for the first time
        this.isEventFileInited.set(true);
    }
}