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
package com.alipay.sofa.tracer.plugins.springcloud.tracers;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractClientTracer;
import com.alipay.sofa.tracer.plugins.springcloud.encodes.MessagePubDigestEncoder;
import com.alipay.sofa.tracer.plugins.springcloud.encodes.MessagePubEncoder;
import com.alipay.sofa.tracer.plugins.springcloud.enums.StreamLogEnum;
import com.alipay.sofa.tracer.plugins.springcloud.repoters.MessagePubStatJsonReporter;
import com.alipay.sofa.tracer.plugins.springcloud.repoters.MessagePubStatReporter;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/11/28 9:55 PM
 * @since:
 **/
public class StreamPubSofaTracer extends AbstractClientTracer {

    private volatile static StreamPubSofaTracer streamPubSofaTracer = null;

    /***
     * Http Client Tracer Singleton
     * @return singleton
     */
    public static StreamPubSofaTracer getStreamPubSofaTracerSingleton() {
        if (streamPubSofaTracer == null) {
            synchronized (StreamPubSofaTracer.class) {
                if (streamPubSofaTracer == null) {
                    streamPubSofaTracer = new StreamPubSofaTracer();
                }
            }
        }
        return streamPubSofaTracer;
    }

    public StreamPubSofaTracer() {
        super(ComponentNameConstants.MESSAGE_PUB);
    }

    @Override
    protected String getClientDigestReporterLogName() {
        return StreamLogEnum.PUB_MESSAGE_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getClientDigestReporterRollingKey() {
        return StreamLogEnum.PUB_MESSAGE_DIGEST.getRollingKey();
    }

    @Override
    protected String getClientDigestReporterLogNameKey() {
        return StreamLogEnum.PUB_MESSAGE_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getClientDigestEncoder() {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new MessagePubDigestEncoder();
        } else {
            return new MessagePubEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateClientStatReporter() {
        StreamLogEnum streamLogEnum = StreamLogEnum.PUB_MESSAGE_STAT;
        String statLog = streamLogEnum.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration.getRollingPolicy(streamLogEnum
            .getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(streamLogEnum
            .getLogNameKey());
        return this.getOpenFeignStatJsonReporter(statLog, statRollingPolicy, statLogReserveConfig);
    }

    protected AbstractSofaTracerStatisticReporter getOpenFeignStatJsonReporter(String statTracerName,
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
