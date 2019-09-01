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
package com.alipay.sofa.tracer.plugins.httpclient;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractClientTracer;

/**
 * HttpClientTracer
 *
 * @author yangguanchao
 * @since 2018/08/07
 */
public class HttpClientTracer extends AbstractClientTracer {

    private volatile static HttpClientTracer httpClientTracer = null;

    /***
     * Http Client Tracer Singleton
     * @return singleton
     */
    public static HttpClientTracer getHttpClientTracerSingleton() {
        if (httpClientTracer == null) {
            synchronized (HttpClientTracer.class) {
                if (httpClientTracer == null) {
                    httpClientTracer = new HttpClientTracer();
                }
            }
        }
        return httpClientTracer;
    }

    protected HttpClientTracer() {
        super(ComponentNameConstants.HTTP_CLIENT);
    }

    @Override
    protected String getClientDigestReporterLogName() {
        return HttpClientLogEnum.HTTP_CLIENT_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getClientDigestReporterRollingKey() {
        return HttpClientLogEnum.HTTP_CLIENT_DIGEST.getRollingKey();
    }

    @Override
    protected String getClientDigestReporterLogNameKey() {
        return HttpClientLogEnum.HTTP_CLIENT_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getClientDigestEncoder() {
        //default json output
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new HttpClientDigestJsonEncoder();
        } else {
            return new HttpClientDigestEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateClientStatReporter() {
        HttpClientLogEnum httpClientLogEnum = HttpClientLogEnum.HTTP_CLIENT_STAT;
        String statLog = httpClientLogEnum.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration.getRollingPolicy(httpClientLogEnum
            .getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(httpClientLogEnum
            .getLogNameKey());
        //stat
        return this.getHttpClientStatReporter(statLog, statRollingPolicy, statLogReserveConfig);
    }

    protected AbstractSofaTracerStatisticReporter getHttpClientStatReporter(String statTracerName,
                                                                            String statRollingPolicy,
                                                                            String statLogReserveConfig) {

        if (SofaTracerConfiguration.isJsonOutput()) {
            return new HttpClientStatJsonReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        } else {
            return new HttpClientStatReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        }

    }
}
