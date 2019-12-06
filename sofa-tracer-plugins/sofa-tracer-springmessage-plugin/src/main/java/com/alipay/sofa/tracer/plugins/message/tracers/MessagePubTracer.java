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
package com.alipay.sofa.tracer.plugins.message.tracers;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractClientTracer;
import com.alipay.sofa.tracer.plugins.message.encodes.MessagePubDigestEncoder;
import com.alipay.sofa.tracer.plugins.message.encodes.MessagePubDigestJsonEncoder;
import com.alipay.sofa.tracer.plugins.message.enums.SpringMessageLogEnum;
import com.alipay.sofa.tracer.plugins.message.repoters.MessagePubStatJsonReporter;
import com.alipay.sofa.tracer.plugins.message.repoters.MessagePubStatReporter;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 10:52 AM
 * @since:
 **/
public class MessagePubTracer extends AbstractClientTracer {

    private volatile static MessagePubTracer messagePubTracer = null;

    public static MessagePubTracer getMessagePubTracerSingleton() {
        if (messagePubTracer == null) {
            synchronized (MessagePubTracer.class) {
                if (messagePubTracer == null) {
                    messagePubTracer = new MessagePubTracer();
                }
            }
        }
        return messagePubTracer;
    }

    protected MessagePubTracer() {
        super("message-pub");
    }

    @Override
    protected String getClientDigestReporterLogName() {
        return SpringMessageLogEnum.MESSAGE_PUB_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getClientDigestReporterRollingKey() {
        return SpringMessageLogEnum.MESSAGE_PUB_DIGEST.getRollingKey();
    }

    @Override
    protected String getClientDigestReporterLogNameKey() {
        return SpringMessageLogEnum.MESSAGE_PUB_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getClientDigestEncoder() {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new MessagePubDigestJsonEncoder();
        } else {
            return new MessagePubDigestEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateClientStatReporter() {
        SpringMessageLogEnum logEnum = SpringMessageLogEnum.MESSAGE_PUB_STAT;
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
            return new MessagePubStatJsonReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        } else {
            return new MessagePubStatReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        }
    }
}
