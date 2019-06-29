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
package com.alipay.sofa.tracer.plugins.webflux;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractServerTracer;

/**
 * webflux tracer
 *
 * @author @author xiang.sheng
 * @since 3.0.0
 */
public class SpringWebfluxTracer extends AbstractServerTracer {
    public static final String                  SPRING_WEBFLUX_JSON_FORMAT_OUTPUT = "spring_webflux_json_format_output";
    private volatile static SpringWebfluxTracer springWebfluxTracer               = null;

    /***
     * Spring MVC Tracer Singleton
     * @return singleton
     */
    public static SpringWebfluxTracer getSpringWebfluxTracerSingleton() {
        if (springWebfluxTracer == null) {
            synchronized (SpringWebfluxTracer.class) {
                if (springWebfluxTracer == null) {
                    springWebfluxTracer = new SpringWebfluxTracer();
                }
            }
        }
        return springWebfluxTracer;
    }

    private SpringWebfluxTracer() {
        super("springwebflux");
    }

    @Override
    protected String getServerDigestReporterLogName() {
        return SpringWebFluxLogEnum.SPRING_WEBFLUX_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getServerDigestReporterRollingKey() {
        return SpringWebFluxLogEnum.SPRING_WEBFLUX_DIGEST.getRollingKey();
    }

    @Override
    protected String getServerDigestReporterLogNameKey() {
        return SpringWebFluxLogEnum.SPRING_WEBFLUX_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getServerDigestEncoder() {
        if (Boolean.TRUE.toString().equalsIgnoreCase(
            SofaTracerConfiguration.getProperty(jsonPropertyName()))) {
            return new SpringWebfluxJsonEncoder();
        } else {
            return new SpringWebfluxDigestEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateServerStatReporter() {
        return generateSofaMvcStatReporter();
    }

    protected String jsonPropertyName() {
        return SPRING_WEBFLUX_JSON_FORMAT_OUTPUT;
    }

    @SuppressWarnings("Duplicates")
    protected SpringWebfluxStatReporter generateSofaMvcStatReporter() {
        SpringWebFluxLogEnum springWebFluxLogEnum = SpringWebFluxLogEnum.SPRING_WEBFLUX_STAT;
        String statLog = springWebFluxLogEnum.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration.getRollingPolicy(springWebFluxLogEnum
            .getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration
            .getLogReserveConfig(springWebFluxLogEnum.getLogNameKey());
        if (Boolean.TRUE.toString().equalsIgnoreCase(
            SofaTracerConfiguration.getProperty(jsonPropertyName()))) {
            return new SpringWebfluxJsonStatReporter(statLog, statRollingPolicy,
                statLogReserveConfig);
        } else {
            return new SpringWebfluxStatReporter(statLog, statRollingPolicy, statLogReserveConfig);
        }
    }
}
