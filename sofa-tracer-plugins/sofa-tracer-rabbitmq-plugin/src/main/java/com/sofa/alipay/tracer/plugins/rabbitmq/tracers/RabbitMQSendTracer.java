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
package com.sofa.alipay.tracer.plugins.rabbitmq.tracers;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractClientTracer;
import com.sofa.alipay.tracer.plugins.rabbitmq.encoders.RabbitMQConsumeDigestEncoder;
import com.sofa.alipay.tracer.plugins.rabbitmq.encoders.RabbitMQConsumeDigestJsonEncoder;
import com.sofa.alipay.tracer.plugins.rabbitmq.encoders.RabbitMQSendDigestEncoder;
import com.sofa.alipay.tracer.plugins.rabbitmq.encoders.RabbitMQSendDigestJsonEncoder;
import com.sofa.alipay.tracer.plugins.rabbitmq.enums.RabbitMqLogEnum;
import com.sofa.alipay.tracer.plugins.rabbitmq.repoters.RabbitMQConsumeStatJsonReporter;
import com.sofa.alipay.tracer.plugins.rabbitmq.repoters.RabbitMQConsumeStatReporter;
import com.sofa.alipay.tracer.plugins.rabbitmq.repoters.RabbitMQSendStatJsonReporter;
import com.sofa.alipay.tracer.plugins.rabbitmq.repoters.RabbitMQSendStatReporter;

/**
 *  RabbitMQSendTracer.
 *
 * @author chenchen6  2020/7/19 21:07
 * @since 3.1.0-SNAPSHOT
 */
public class RabbitMQSendTracer extends AbstractClientTracer {

    private volatile static RabbitMQSendTracer rabbitMQSendTracer = null;

    public static RabbitMQSendTracer getRabbitMQSendTracerSingleton() {
        if (rabbitMQSendTracer == null) {
            synchronized (RabbitMQSendTracer.class) {
                if (rabbitMQSendTracer == null) {
                    rabbitMQSendTracer = new RabbitMQSendTracer();
                }
            }
        }
        return rabbitMQSendTracer;
    }

    public RabbitMQSendTracer() {
        super(ComponentNameConstants.RABBITMQ_SEND);
    }

    @Override
    protected String getClientDigestReporterLogName() {
        return RabbitMqLogEnum.MQ_SEND_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getClientDigestReporterRollingKey() {
        return RabbitMqLogEnum.MQ_SEND_DIGEST.getRollingKey();
    }

    @Override
    protected String getClientDigestReporterLogNameKey() {
        return RabbitMqLogEnum.MQ_SEND_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getClientDigestEncoder() {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new RabbitMQSendDigestJsonEncoder();
        } else {
            return new RabbitMQSendDigestEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateClientStatReporter() {
        RabbitMqLogEnum logEnum = RabbitMqLogEnum.MQ_SEND_STAT;
        String statLog = logEnum.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration
            .getRollingPolicy(logEnum.getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(logEnum
            .getLogNameKey());
        return getStatJsonReporter(statLog, statRollingPolicy, statLogReserveConfig);
    }

    protected AbstractSofaTracerStatisticReporter getStatJsonReporter(String statTracerName,
                                                                      String statRollingPolicy,
                                                                      String statLogReserveConfig) {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new RabbitMQSendStatJsonReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        } else {
            return new RabbitMQSendStatReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        }
    }
}
