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
import com.alipay.common.tracer.core.tracer.AbstractServerTracer;
import com.alipay.sofa.tracer.plugins.springcloud.encodes.MessageSubDigestEncoder;
import com.alipay.sofa.tracer.plugins.springcloud.encodes.MessageSubEncoder;
import com.alipay.sofa.tracer.plugins.springcloud.enums.StreamLogEnum;
import com.alipay.sofa.tracer.plugins.springcloud.repoters.MessageSubStatJsonReporter;
import com.alipay.sofa.tracer.plugins.springcloud.repoters.MessageSubStatReporter;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/11/28 9:55 PM
 * @since:
 **/
public class StreamSubSofaTracer extends AbstractServerTracer {

    private volatile static StreamSubSofaTracer streamSubSofaTracer = null;

    /***
     * Http Client Tracer Singleton
     * @return singleton
     */
    public static StreamSubSofaTracer getStreamSubSofaTracerSingleton() {
        if (streamSubSofaTracer == null) {
            synchronized (StreamSubSofaTracer.class) {
                if (streamSubSofaTracer == null) {
                    streamSubSofaTracer = new StreamSubSofaTracer();
                }
            }
        }
        return streamSubSofaTracer;
    }

    public StreamSubSofaTracer() {
        super(ComponentNameConstants.MESSAGE_SUB);
    }

    @Override
    protected String getServerDigestReporterLogName() {
        return StreamLogEnum.SUB_MESSAGE_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getServerDigestReporterRollingKey() {
        return StreamLogEnum.SUB_MESSAGE_DIGEST.getRollingKey();
    }

    @Override
    protected String getServerDigestReporterLogNameKey() {
        return StreamLogEnum.SUB_MESSAGE_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getServerDigestEncoder() {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new MessageSubDigestEncoder();
        } else {
            return new MessageSubEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateServerStatReporter() {
        StreamLogEnum streamLogEnum = StreamLogEnum.SUB_MESSAGE_STAT;
        String statLog = streamLogEnum.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration.getRollingPolicy(streamLogEnum
            .getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(streamLogEnum
            .getLogNameKey());
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new MessageSubStatJsonReporter(statLog, statRollingPolicy, statLogReserveConfig);
        } else {
            return new MessageSubStatReporter(statLog, statRollingPolicy, statLogReserveConfig);
        }
    }
}
