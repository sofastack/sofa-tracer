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
import com.alipay.common.tracer.core.tracer.AbstractServerTracer;
import com.sofa.alipay.tracer.plugins.rabbitmq.encoders.RabbitMQConsumeDigestEncoder;
import com.sofa.alipay.tracer.plugins.rabbitmq.encoders.RabbitMQConsumeDigestJsonEncoder;
import com.sofa.alipay.tracer.plugins.rabbitmq.enums.RabbitMqLogEnum;
import com.sofa.alipay.tracer.plugins.rabbitmq.repoters.RabbitMQConsumeStatJsonReporter;
import com.sofa.alipay.tracer.plugins.rabbitmq.repoters.RabbitMQConsumeStatReporter;

/**
 * RabbitMQConsumeTracer.
 *
 * @author chenchen6 2020/8/19 20:44
 * @since 3.1.0-SNAPSHOT
 */
public class RabbitMQConsumeTracer extends AbstractServerTracer {

    private volatile static RabbitMQConsumeTracer rabbitMQConsumerTracer = null;

    public static RabbitMQConsumeTracer getRabbitMQSendTracerSingleton() {
        if (rabbitMQConsumerTracer == null) {
            synchronized (RabbitMQConsumeTracer.class) {
                if (rabbitMQConsumerTracer == null) {
                    rabbitMQConsumerTracer = new RabbitMQConsumeTracer();
                }
            }
        }
        return rabbitMQConsumerTracer;
    }

    public RabbitMQConsumeTracer() {
        super(ComponentNameConstants.RABBITMQ_CONSUMER);
    }

    @Override
    protected String getServerDigestReporterLogName() {
        return RabbitMqLogEnum.MQ_CONSUME_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getServerDigestReporterRollingKey() {
        return RabbitMqLogEnum.MQ_CONSUME_DIGEST.getRollingKey();
    }

    @Override
    protected String getServerDigestReporterLogNameKey() {
        return RabbitMqLogEnum.MQ_CONSUME_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getServerDigestEncoder() {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new RabbitMQConsumeDigestJsonEncoder();
        } else {
            return new RabbitMQConsumeDigestEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateServerStatReporter() {
        RabbitMqLogEnum logEnum = RabbitMqLogEnum.MQ_CONSUME_STAT;
        String statLog = logEnum.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration
            .getRollingPolicy(logEnum.getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(logEnum
            .getLogNameKey());
        return this.getStatJsonReporter(statLog, statRollingPolicy, statLogReserveConfig);
    }

    protected AbstractSofaTracerStatisticReporter getStatJsonReporter(String statTracerName,
                                                                      String statRollingPolicy,
                                                                      String statLogReserveConfig) {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new RabbitMQConsumeStatJsonReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        } else {
            return new RabbitMQConsumeStatReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        }
    }
}
