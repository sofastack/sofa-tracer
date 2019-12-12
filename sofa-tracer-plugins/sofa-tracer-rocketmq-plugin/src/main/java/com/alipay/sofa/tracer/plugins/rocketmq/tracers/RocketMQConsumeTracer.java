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
import com.alipay.common.tracer.core.tracer.AbstractServerTracer;
import com.alipay.sofa.tracer.plugins.rocketmq.encodes.RocketMQConsumeDigestEncoder;
import com.alipay.sofa.tracer.plugins.rocketmq.encodes.RocketMQConsumeDigestJsonEncoder;
import com.alipay.sofa.tracer.plugins.rocketmq.enums.RocketMQLogEnum;
import com.alipay.sofa.tracer.plugins.rocketmq.repoters.RocketMQConsumeStatJsonReporter;
import com.alipay.sofa.tracer.plugins.rocketmq.repoters.RocketMQConsumeStatReporter;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 10:52 AM
 * @since:
 **/
public class RocketMQConsumeTracer extends AbstractServerTracer {

    private volatile static RocketMQConsumeTracer rocketMQConsumeTracer = null;

    public static RocketMQConsumeTracer getRocketMQConsumeTracerSingleton() {
        if (rocketMQConsumeTracer == null) {
            synchronized (RocketMQConsumeTracer.class) {
                if (rocketMQConsumeTracer == null) {
                    rocketMQConsumeTracer = new RocketMQConsumeTracer();
                }
            }
        }
        return rocketMQConsumeTracer;
    }

    protected RocketMQConsumeTracer() {
        super(ComponentNameConstants.ROCKETMQ_CONSUMER);
    }

    @Override
    protected String getServerDigestReporterLogName() {
        return RocketMQLogEnum.MQ_CONSUME_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getServerDigestReporterRollingKey() {
        return RocketMQLogEnum.MQ_CONSUME_DIGEST.getRollingKey();
    }

    @Override
    protected String getServerDigestReporterLogNameKey() {
        return RocketMQLogEnum.MQ_CONSUME_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getServerDigestEncoder() {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new RocketMQConsumeDigestJsonEncoder();
        } else {
            return new RocketMQConsumeDigestEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateServerStatReporter() {
        RocketMQLogEnum logEnum = RocketMQLogEnum.MQ_CONSUME_STAT;
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
            return new RocketMQConsumeStatJsonReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        } else {
            return new RocketMQConsumeStatReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        }
    }
}
