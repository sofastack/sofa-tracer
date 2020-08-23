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
import com.alipay.common.tracer.core.tracer.AbstractServerTracer;
import com.sofa.alipay.tracer.plugins.kafkamq.encoders.KafkaMQConsumeDigestEncoder;
import com.sofa.alipay.tracer.plugins.kafkamq.encoders.KafkaMQConsumeDigestJsonEncoder;
import com.sofa.alipay.tracer.plugins.kafkamq.enums.KafkaMqLogEnum;

/**
 * KafkaMQConsumeTracer.
 *
 * @author chenchen6  2020/8/23 15:17
 * @since 3.1.0-SNAPSHOT
 */
public class KafkaMQConsumeTracer extends AbstractServerTracer {

    private volatile static KafkaMQConsumeTracer kafkaMQConsumeTracer;

    public KafkaMQConsumeTracer() {
        super(ComponentNameConstants.KAFKAMQ_CONSUMER);
    }

    public static KafkaMQConsumeTracer getKafkaMQConsumeTracerSingleton() {
        if (kafkaMQConsumeTracer == null) {
            synchronized (KafkaMQConsumeTracer.class) {
                if (kafkaMQConsumeTracer == null) {
                    kafkaMQConsumeTracer = new KafkaMQConsumeTracer();
                }
            }
        }
        return kafkaMQConsumeTracer;
    }

    @Override
    protected String getServerDigestReporterLogName() {
        return KafkaMqLogEnum.MQ_CONSUME_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getServerDigestReporterRollingKey() {
        return KafkaMqLogEnum.MQ_CONSUME_DIGEST.getRollingKey();
    }

    @Override
    protected String getServerDigestReporterLogNameKey() {
        return KafkaMqLogEnum.MQ_CONSUME_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getServerDigestEncoder() {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new KafkaMQConsumeDigestJsonEncoder();
        } else {
            return new KafkaMQConsumeDigestEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateServerStatReporter() {

        // now, ignore.
        return null;
    }
}
