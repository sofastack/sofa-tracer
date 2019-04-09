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
import com.alipay.common.tracer.core.tracer.AbstractServerTracer;
import com.alipay.sofa.tracer.plugins.dubbo.encoder.DubboServerDigestJsonEncoder;
import com.alipay.sofa.tracer.plugins.dubbo.enums.DubboLogEnum;
import com.alipay.sofa.tracer.plugins.dubbo.stat.DubboServerStatJsonReporter;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/2/26 3:47 PM
 * @since:
 **/
public class DubboProviderSofaTracer extends AbstractServerTracer {

    private volatile static DubboProviderSofaTracer dubboProviderSofaTracer = null;

    public static DubboProviderSofaTracer getDubboProviderSofaTracerSingleton() {
        if (dubboProviderSofaTracer == null) {
            synchronized (DubboProviderSofaTracer.class) {
                if (dubboProviderSofaTracer == null) {
                    dubboProviderSofaTracer = new DubboProviderSofaTracer(
                        ComponentNameConstants.DUBBO_SERVER);
                }
            }
        }
        return dubboProviderSofaTracer;
    }

    public DubboProviderSofaTracer(String tracerType) {
        super(tracerType);
    }

    @Override
    protected String getServerDigestReporterLogName() {
        return DubboLogEnum.DUBBO_SERVER_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getServerDigestReporterRollingKey() {
        return DubboLogEnum.DUBBO_SERVER_DIGEST.getRollingKey();
    }

    @Override
    protected String getServerDigestReporterLogNameKey() {
        return DubboLogEnum.DUBBO_SERVER_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getServerDigestEncoder() {
        return new DubboServerDigestJsonEncoder();
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateServerStatReporter() {
        DubboLogEnum dubboClientStat = DubboLogEnum.DUBBO_SERVER_STAT;
        String statLog = dubboClientStat.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration.getRollingPolicy(dubboClientStat
            .getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(dubboClientStat
            .getLogNameKey());
        return new DubboServerStatJsonReporter(statLog, statRollingPolicy, statLogReserveConfig);
    }
}
