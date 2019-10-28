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
package com.alipay.sofa.tracer.plugins.okhttp;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractClientTracer;

/**
 * @author xianglong.chen
 * @since  2019/1/3 16:03
 */
public class OkHttpTracer extends AbstractClientTracer {

    private volatile static OkHttpTracer okHttpTracer = null;

    protected OkHttpTracer() {
        super(ComponentNameConstants.OK_HTTP);
    }

    public static OkHttpTracer getOkHttpTracerSingleton() {
        if (okHttpTracer == null) {
            synchronized (OkHttpTracer.class) {
                if (okHttpTracer == null) {
                    okHttpTracer = new OkHttpTracer();
                }
            }
        }
        return okHttpTracer;
    }

    @Override
    protected String getDigestReporterLogName() {
        return OkHttpLogEnum.OK_HTTP_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getDigestReporterRollingKey() {
        return OkHttpLogEnum.OK_HTTP_DIGEST.getRollingKey();
    }

    @Override
    protected String getDigestReporterLogNameKey() {
        return OkHttpLogEnum.OK_HTTP_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getDigestEncoder() {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new OkHttpDigestJsonEncoder();
        } else {
            return new OkHttpDigestEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateStatReporter() {
        OkHttpLogEnum okHttpLogEnum = OkHttpLogEnum.OK_HTTP_STAT;
        String statLog = okHttpLogEnum.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration.getRollingPolicy(okHttpLogEnum
            .getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(okHttpLogEnum
            .getLogNameKey());
        return this.getOkHttpStatReporter(statLog, statRollingPolicy, statLogReserveConfig);
    }

    private AbstractSofaTracerStatisticReporter getOkHttpStatReporter(String statLog,
                                                                      String statRollingPolicy,
                                                                      String statLogReserveConfig) {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new OkHttpStatJsonReporter(statLog, statRollingPolicy, statLogReserveConfig);
        } else {
            return new OkHttpStatReporter(statLog, statRollingPolicy, statLogReserveConfig);
        }
    }
}
