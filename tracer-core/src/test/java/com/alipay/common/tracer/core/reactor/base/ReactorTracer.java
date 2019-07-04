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
package com.alipay.common.tracer.core.reactor.base;

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.appender.self.Timestamp;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractServerTracer;
import com.alipay.common.tracer.core.utils.StringUtils;
import io.opentracing.tag.Tags;

import java.io.IOException;
import java.util.Map;

public class ReactorTracer extends AbstractServerTracer {
    public enum ReactorLogEnum {

        // REACTOR 日志
        REACTOR_DIGEST("reactor_digest_log_name", "reactor-digest.log", "reactor_digest_rolling"), //
        REACTOR_STAT("reactor_stat_log_name", "reactor-stat.log", "reactor_stat_rolling");

        private String logNameKey;
        private String defaultLogName;
        private String rollingKey;

        ReactorLogEnum(String logNameKey, String defaultLogName, String rollingKey) {
            this.logNameKey = logNameKey;
            this.defaultLogName = defaultLogName;
            this.rollingKey = rollingKey;
        }

        public String getLogNameKey() {
            return logNameKey;
        }

        public String getDefaultLogName() {
            return defaultLogName;
        }

        public String getRollingKey() {
            return rollingKey;
        }
    }

    private volatile static ReactorTracer reactorTracer = null;

    /***
     * Spring MVC Tracer Singleton
     * @return singleton
     */
    public static ReactorTracer getReactorTracerSingleton() {
        if (reactorTracer == null) {
            synchronized (ReactorTracer.class) {
                if (reactorTracer == null) {
                    reactorTracer = new ReactorTracer();
                }
            }
        }
        return reactorTracer;
    }

    public ReactorTracer() {
        super("reactor");
    }

    @Override
    protected String getServerDigestReporterLogName() {
        return ReactorLogEnum.REACTOR_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getServerDigestReporterRollingKey() {
        return ReactorLogEnum.REACTOR_DIGEST.getRollingKey();
    }

    @Override
    protected String getServerDigestReporterLogNameKey() {
        return ReactorLogEnum.REACTOR_DIGEST.getLogNameKey();
    }

    class ReactorDigestJsonEncoder extends AbstractDigestSpanEncoder {

        @Override
        public String encode(SofaTracerSpan span) throws IOException {
            JsonStringBuilder jsonStringBuilder = new JsonStringBuilder();
            jsonStringBuilder.appendBegin("time", Timestamp.format(span.getEndTime()));
            appendSlot(jsonStringBuilder, span);
            return jsonStringBuilder.toString();
        }

        private void appendSlot(JsonStringBuilder jsonStringBuilder, SofaTracerSpan sofaTracerSpan) {

            SofaTracerSpanContext context = sofaTracerSpan.getSofaTracerSpanContext();
            Map<String, String> tagWithStr = sofaTracerSpan.getTagsWithStr();
            Map<String, Number> tagWithNumber = sofaTracerSpan.getTagsWithNumber();
            jsonStringBuilder.append(CommonSpanTags.LOCAL_APP,
                tagWithStr.get(CommonSpanTags.LOCAL_APP));
            //remote address, webflux
            if (tagWithStr.get(CommonSpanTags.REMOTE_APP) != null) {
                jsonStringBuilder.append(CommonSpanTags.REMOTE_APP,
                    tagWithStr.get(CommonSpanTags.REMOTE_APP));
            }
            jsonStringBuilder.append("operation", sofaTracerSpan.getOperationName());
            jsonStringBuilder.append("traceId", context.getTraceId());
            jsonStringBuilder.append("spanId", context.getSpanId());
            jsonStringBuilder.append(CommonSpanTags.REQUEST_URL,
                tagWithStr.get(CommonSpanTags.REQUEST_URL));
            jsonStringBuilder.append(CommonSpanTags.METHOD, tagWithStr.get(CommonSpanTags.METHOD));
            if (StringUtils.isNotBlank(tagWithStr.get(Tags.ERROR.getKey()))) {
                jsonStringBuilder.append(Tags.ERROR.getKey(), tagWithStr.get(Tags.ERROR.getKey()));
            }
            jsonStringBuilder.append(CommonSpanTags.RESULT_CODE,
                tagWithStr.get(CommonSpanTags.RESULT_CODE));
            Number requestSize = tagWithNumber.get(CommonSpanTags.REQ_SIZE);
            jsonStringBuilder.append(CommonSpanTags.REQ_SIZE, (requestSize == null ? 0L
                : requestSize.longValue()));
            Number responseSize = tagWithNumber.get(CommonSpanTags.RESP_SIZE);
            jsonStringBuilder.append(CommonSpanTags.RESP_SIZE, (responseSize == null ? 0L
                : responseSize.longValue()));
            jsonStringBuilder.append("time.cost.milliseconds",
                (sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime()));
            jsonStringBuilder.append(CommonSpanTags.CURRENT_THREAD_NAME,
                tagWithStr.get(CommonSpanTags.CURRENT_THREAD_NAME));
            jsonStringBuilder.appendEnd("baggage", baggageSerialized(context));
        }
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getServerDigestEncoder() {
        return new ReactorDigestJsonEncoder();
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateServerStatReporter() {
        return generateSofaMvcStatReporter();
    }

    private AbstractSofaTracerStatisticReporter generateSofaMvcStatReporter() {
        ReactorLogEnum reactorLogEnum = ReactorLogEnum.REACTOR_STAT;
        String statLog = reactorLogEnum.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration.getRollingPolicy(reactorLogEnum
            .getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(reactorLogEnum
            .getLogNameKey());

        return new AbstractSofaTracerStatisticReporter(statLog, statRollingPolicy,
            statLogReserveConfig) {
            @Override
            public void doReportStat(SofaTracerSpan sofaTracerSpan) {

            }
        };
    }

}
