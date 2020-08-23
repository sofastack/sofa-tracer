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
        if(SofaTracerConfiguration.isJsonOutput()) {
            return new KafkaMQSendDigestJsonEncoder();
        } else {
            return new KafkaMQSendDigestEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateClientStatReporter() {
        return null;
    }
}
