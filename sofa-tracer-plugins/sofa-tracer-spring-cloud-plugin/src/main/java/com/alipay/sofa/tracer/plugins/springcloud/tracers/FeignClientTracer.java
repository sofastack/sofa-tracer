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
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractClientTracer;
import com.alipay.sofa.tracer.plugins.springcloud.encodes.OpenFeignDigestEncoder;
import com.alipay.sofa.tracer.plugins.springcloud.repoters.OpenFeignStatJsonReporter;
import com.alipay.sofa.tracer.plugins.springcloud.encodes.OpenFeignDigestJsonEncoder;
import com.alipay.sofa.tracer.plugins.springcloud.enums.FeignClientLogEnum;
import com.alipay.sofa.tracer.plugins.springcloud.repoters.OpenFeignStatReporter;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 10:52 AM
 * @since:
 **/
public class FeignClientTracer extends AbstractClientTracer {

    private volatile static FeignClientTracer feignClientTracer = null;

    /***
     * Http Client Tracer Singleton
     * @return singleton
     */
    public static FeignClientTracer getFeignClientTracerSingleton() {
        if (feignClientTracer == null) {
            synchronized (FeignClientTracer.class) {
                if (feignClientTracer == null) {
                    feignClientTracer = new FeignClientTracer();
                }
            }
        }
        return feignClientTracer;
    }

    protected FeignClientTracer() {
        super("open-feign");
    }

    @Override
    protected String getClientDigestReporterLogName() {
        return FeignClientLogEnum.FEIGN_CLIENT_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getClientDigestReporterRollingKey() {
        return FeignClientLogEnum.FEIGN_CLIENT_DIGEST.getRollingKey();
    }

    @Override
    protected String getClientDigestReporterLogNameKey() {
        return FeignClientLogEnum.FEIGN_CLIENT_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getClientDigestEncoder() {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new OpenFeignDigestJsonEncoder();
        } else {
            return new OpenFeignDigestEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateClientStatReporter() {
        FeignClientLogEnum feignClientLogEnum = FeignClientLogEnum.FEIGN_CLIENT_STAT;
        String statLog = feignClientLogEnum.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration.getRollingPolicy(feignClientLogEnum
            .getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration
            .getLogReserveConfig(feignClientLogEnum.getLogNameKey());
        return this.getOpenFeignStatJsonReporter(statLog, statRollingPolicy, statLogReserveConfig);
    }

    protected AbstractSofaTracerStatisticReporter getOpenFeignStatJsonReporter(String statTracerName,
                                                                               String statRollingPolicy,
                                                                               String statLogReserveConfig) {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new OpenFeignStatJsonReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        } else {
            return new OpenFeignStatReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        }
    }
}
