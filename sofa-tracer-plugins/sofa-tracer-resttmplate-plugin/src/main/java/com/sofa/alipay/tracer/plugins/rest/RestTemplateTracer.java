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
package com.sofa.alipay.tracer.plugins.rest;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractClientTracer;

/**
 * RestTemplateTracer
 * @author: guolei.sgl
 * @since: v2.3.0
 */
public class RestTemplateTracer extends AbstractClientTracer {

    protected RestTemplateTracer() {
        super(ComponentNameConstants.REST_TEMPLATE);
    }

    @Override
    protected String getClientDigestReporterLogName() {
        return RestTemplateLogEnum.REST_TEMPLATE_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getClientDigestReporterRollingKey() {
        return RestTemplateLogEnum.REST_TEMPLATE_DIGEST.getRollingKey();
    }

    @Override
    protected String getClientDigestReporterLogNameKey() {
        return RestTemplateLogEnum.REST_TEMPLATE_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getClientDigestEncoder() {
        return new RestTemplateDigestJsonEncoder();
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateClientStatReporter() {
        RestTemplateLogEnum httpClientLogEnum = RestTemplateLogEnum.REST_TEMPLATE_STAT;
        String statLog = httpClientLogEnum.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration.getRollingPolicy(httpClientLogEnum
            .getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(httpClientLogEnum
            .getLogNameKey());
        //stat
        return this.getRestTemplateStatReporter(statLog, statRollingPolicy, statLogReserveConfig);
    }

    protected AbstractSofaTracerStatisticReporter getRestTemplateStatReporter(String statTracerName,
                                                                              String statRollingPolicy,
                                                                              String statLogReserveConfig) {
        return new RestTemplateStatJsonReporter(statTracerName, statRollingPolicy,
            statLogReserveConfig);
    }
}
