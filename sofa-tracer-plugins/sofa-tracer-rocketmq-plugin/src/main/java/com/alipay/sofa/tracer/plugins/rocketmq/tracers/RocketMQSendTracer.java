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
package com.alipay.sofa.tracer.plugins.rocketmq.tracers;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractClientTracer;
import com.alipay.sofa.tracer.plugins.rocketmq.encodes.RocketMQSendDigestEncoder;
import com.alipay.sofa.tracer.plugins.rocketmq.encodes.RocketMQSendDigestJsonEncoder;
import com.alipay.sofa.tracer.plugins.rocketmq.enums.RocketMQLogEnum;
import com.alipay.sofa.tracer.plugins.rocketmq.repoters.RocketMQSendStatJsonReporter;
import com.alipay.sofa.tracer.plugins.rocketmq.repoters.RocketMQSendStatReporter;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 10:52 AM
 * @since:
 **/
public class RocketMQSendTracer extends AbstractClientTracer {

    private volatile static RocketMQSendTracer rocketMQSendTracer = null;

    public static RocketMQSendTracer getRocketMQSendTracerSingleton() {
        if (rocketMQSendTracer == null) {
            synchronized (RocketMQSendTracer.class) {
                if (rocketMQSendTracer == null) {
                    rocketMQSendTracer = new RocketMQSendTracer();
                }
            }
        }
        return rocketMQSendTracer;
    }

    protected RocketMQSendTracer() {
        super(ComponentNameConstants.ROCKETMQ_SEND);
    }

    @Override
    protected String getClientDigestReporterLogName() {
        return RocketMQLogEnum.MQ_SEND_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getClientDigestReporterRollingKey() {
        return RocketMQLogEnum.MQ_SEND_DIGEST.getRollingKey();
    }

    @Override
    protected String getClientDigestReporterLogNameKey() {
        return RocketMQLogEnum.MQ_SEND_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getClientDigestEncoder() {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new RocketMQSendDigestJsonEncoder();
        } else {
            return new RocketMQSendDigestEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateClientStatReporter() {
        RocketMQLogEnum logEnum = RocketMQLogEnum.MQ_SEND_STAT;
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
            return new RocketMQSendStatJsonReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        } else {
            return new RocketMQSendStatReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        }
    }
}
