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
import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractServerTracer;
import com.alipay.sofa.tracer.plugins.message.encodes.MessageSubDigestEncoder;
import com.alipay.sofa.tracer.plugins.message.encodes.MessageSubDigestJsonEncoder;
import com.alipay.sofa.tracer.plugins.message.enums.SpringMessageLogEnum;
import com.alipay.sofa.tracer.plugins.message.repoters.MessageSubStatJsonReporter;
import com.alipay.sofa.tracer.plugins.message.repoters.MessageSubStatReporter;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 10:52 AM
 * @since:
 **/
public class MessageSubTracer extends AbstractServerTracer {

    private volatile static MessageSubTracer messageSubTracer = null;

    public static MessageSubTracer getMessageSubTracerSingleton() {
        if (messageSubTracer == null) {
            synchronized (MessageSubTracer.class) {
                if (messageSubTracer == null) {
                    messageSubTracer = new MessageSubTracer();
                }
            }
        }
        return messageSubTracer;
    }

    protected MessageSubTracer() {
        super(ComponentNameConstants.MSG_SUB);
    }

    @Override
    protected String getServerDigestReporterLogName() {
        return SpringMessageLogEnum.MESSAGE_SUB_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getServerDigestReporterRollingKey() {
        return SpringMessageLogEnum.MESSAGE_SUB_DIGEST.getRollingKey();
    }

    @Override
    protected String getServerDigestReporterLogNameKey() {
        return SpringMessageLogEnum.MESSAGE_SUB_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getServerDigestEncoder() {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new MessageSubDigestJsonEncoder();
        } else {
            return new MessageSubDigestEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateServerStatReporter() {
        SpringMessageLogEnum logEnum = SpringMessageLogEnum.MESSAGE_SUB_STAT;
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
            return new MessageSubStatJsonReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        } else {
            return new MessageSubStatReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        }
    }
}
