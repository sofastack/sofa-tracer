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
package com.sofa.alipay.tracer.plugins.kafkamq.tracers;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractClientTracer;
import com.sofa.alipay.tracer.plugins.kafkamq.encoders.KafkaMQSendDigestEncoder;
import com.sofa.alipay.tracer.plugins.kafkamq.encoders.KafkaMQSendDigestJsonEncoder;
import com.sofa.alipay.tracer.plugins.kafkamq.enums.KafkaMqLogEnum;
import com.sofa.alipay.tracer.plugins.kafkamq.repoters.KafkaMQSendStatJsonReporter;
import com.sofa.alipay.tracer.plugins.kafkamq.repoters.KafkaMQSendStatReporter;

/**
 *  KafkaMQSendTracer.
 *
 * @author chenchen6   2020/8/23 15:22
 */
public class KafkaMQSendTracer extends AbstractClientTracer {

    private volatile static KafkaMQSendTracer kafkaMQSendTracer;

    public KafkaMQSendTracer() {
        super(ComponentNameConstants.KAFKAMQ_SEND);
    }

    public static KafkaMQSendTracer getKafkaMQSendTracerSingleton() {
        if (kafkaMQSendTracer == null) {
            synchronized (KafkaMQSendTracer.class) {
                if (kafkaMQSendTracer == null) {
                    kafkaMQSendTracer = new KafkaMQSendTracer();
                }
            }
        }
        return kafkaMQSendTracer;
    }

    @Override
    protected String getClientDigestReporterLogName() {
        return KafkaMqLogEnum.MQ_SEND_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getClientDigestReporterRollingKey() {
        return KafkaMqLogEnum.MQ_SEND_DIGEST.getRollingKey();
    }

    @Override
    protected String getClientDigestReporterLogNameKey() {
        return KafkaMqLogEnum.MQ_SEND_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getClientDigestEncoder() {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new KafkaMQSendDigestJsonEncoder();
        } else {
            return new KafkaMQSendDigestEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateClientStatReporter() {
        KafkaMqLogEnum logEnum = KafkaMqLogEnum.MQ_SEND_STAT;
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
            return new KafkaMQSendStatJsonReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        } else {
            return new KafkaMQSendStatReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        }
    }
}
