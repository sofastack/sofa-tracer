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
package com.alipay.sofa.tracer.plugins.springmvc;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractServerTracer;

/**
 * SpringMvcTracer
 *
 * @author yangguanchao
 * @since 2018/04/30
 */
public class SpringMvcTracer extends AbstractServerTracer {

    public static final String              SPRING_MVC_JSON_FORMAT_OUTPUT = "spring_mvc_json_format_output";

    private volatile static SpringMvcTracer springMvcTracer               = null;

    /***
     * Spring MVC Tracer Singleton
     * @return singleton
     */
    public static SpringMvcTracer getSpringMvcTracerSingleton() {
        if (springMvcTracer == null) {
            synchronized (SpringMvcTracer.class) {
                if (springMvcTracer == null) {
                    springMvcTracer = new SpringMvcTracer();
                }
            }
        }
        return springMvcTracer;
    }

    private SpringMvcTracer() {
        super("springmvc");
    }

    @Override
    protected String getServerDigestReporterLogName() {
        return SpringMvcLogEnum.SPRING_MVC_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getServerDigestReporterRollingKey() {
        return SpringMvcLogEnum.SPRING_MVC_DIGEST.getRollingKey();
    }

    @Override
    protected String getServerDigestReporterLogNameKey() {
        return SpringMvcLogEnum.SPRING_MVC_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getServerDigestEncoder() {
        if (Boolean.TRUE.toString().equalsIgnoreCase(
            SofaTracerConfiguration.getProperty(SPRING_MVC_JSON_FORMAT_OUTPUT))) {
            return new SpringMvcDigestJsonEncoder();
        } else {
            return new SpringMvcDigestEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateServerStatReporter() {
        return generateSofaMvcStatReporter();
    }

    private SpringMvcStatReporter generateSofaMvcStatReporter() {
        SpringMvcLogEnum springMvcLogEnum = SpringMvcLogEnum.SPRING_MVC_STAT;
        String statLog = springMvcLogEnum.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration.getRollingPolicy(springMvcLogEnum
            .getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(springMvcLogEnum
            .getLogNameKey());
        if (Boolean.TRUE.toString().equalsIgnoreCase(
            SofaTracerConfiguration.getProperty(SPRING_MVC_JSON_FORMAT_OUTPUT))) {
            return new SpringMvcJsonStatReporter(statLog, statRollingPolicy, statLogReserveConfig);
        } else {
            return new SpringMvcStatReporter(statLog, statRollingPolicy, statLogReserveConfig);
        }
    }
}