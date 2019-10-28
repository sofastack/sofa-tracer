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
package com.alipay.sofa.tracer.plugins.dubbo.tracer;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractClientTracer;
import com.alipay.sofa.tracer.plugins.dubbo.encoder.DubboClientDigestEncoder;
import com.alipay.sofa.tracer.plugins.dubbo.encoder.DubboClientDigestJsonEncoder;
import com.alipay.sofa.tracer.plugins.dubbo.enums.DubboLogEnum;
import com.alipay.sofa.tracer.plugins.dubbo.stat.DubboClientStatJsonReporter;
import com.alipay.sofa.tracer.plugins.dubbo.stat.DubboClientStatReporter;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/2/26 3:33 PM
 * @since:
 **/
public class DubboConsumerSofaTracer extends AbstractClientTracer {

    private volatile static DubboConsumerSofaTracer dubboConsumerSofaTracer = null;

    public static DubboConsumerSofaTracer getDubboConsumerSofaTracerSingleton() {
        if (dubboConsumerSofaTracer == null) {
            synchronized (DubboConsumerSofaTracer.class) {
                if (dubboConsumerSofaTracer == null) {
                    dubboConsumerSofaTracer = new DubboConsumerSofaTracer(
                        ComponentNameConstants.DUBBO_CLIENT);
                }
            }
        }
        return dubboConsumerSofaTracer;
    }

    public DubboConsumerSofaTracer(String tracerType) {
        super(tracerType);
    }

    @Override
    protected String getDigestReporterLogName() {
        return DubboLogEnum.DUBBO_CLIENT_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getDigestReporterRollingKey() {
        return DubboLogEnum.DUBBO_CLIENT_DIGEST.getRollingKey();
    }

    @Override
    protected String getDigestReporterLogNameKey() {
        return DubboLogEnum.DUBBO_CLIENT_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getDigestEncoder() {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new DubboClientDigestJsonEncoder();
        } else {
            return new DubboClientDigestEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateStatReporter() {
        DubboLogEnum dubboClientStat = DubboLogEnum.DUBBO_CLIENT_STAT;
        String statLog = dubboClientStat.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration.getRollingPolicy(dubboClientStat
            .getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(dubboClientStat
            .getLogNameKey());
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new DubboClientStatJsonReporter(statLog, statRollingPolicy, statLogReserveConfig);
        } else {
            return new DubboClientStatReporter(statLog, statRollingPolicy, statLogReserveConfig);
        }
    }
}
