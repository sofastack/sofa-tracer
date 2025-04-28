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
package com.alipay.common.tracer.core.tracertest.encoder;

import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.appender.self.Timestamp;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;

import java.io.IOException;
import java.util.Map;

/**
 * The type Client span event encoder.
 *
 * @author yuqian
 * @version : ClientSpanEventEncoder.java, v 0.1 2025-03-10 17:02 yuqian Exp $$
 */
public class ClientSpanEventEncoder implements SpanEncoder<SofaTracerSpan> {

    private XStringBuilder xsb = new XStringBuilder();

    @Override
    public String encode(SofaTracerSpan span) throws IOException {
        SofaTracerSpanContext spanContext = span.getSofaTracerSpanContext();
        xsb.reset();
        //
        xsb.append(Timestamp.format(span.getEventData().getTimestamp()));
        //traceId
        xsb.append(spanContext.getTraceId());
        //spanId
        xsb.append(spanContext.getSpanId());
        //tags string
        xsb.append(StringUtils.mapToString(span.getEventData().getEventTagWithStr()));
        //tags bool
        Map<String, Boolean> tagsBool = span.getEventData().getEventTagWithBool();
        StringBuilder tagsBoolBuild = new StringBuilder();
        for (Map.Entry<String, Boolean> entry : tagsBool.entrySet()) {
            tagsBoolBuild.append(entry.getKey()).append(StringUtils.EQUAL)
                .append(entry.getValue().toString()).append(StringUtils.AND);
        }
        xsb.append(tagsBoolBuild.toString());

        //tags number
        Map<String, Number> tagsNum = span.getEventData().getEventTagWithNumber();
        StringBuilder tagsNumBuild = new StringBuilder();
        for (Map.Entry<String, Number> entry : tagsNum.entrySet()) {
            tagsNumBuild.append(entry.getKey()).append(StringUtils.EQUAL)
                .append(entry.getValue().toString()).append(StringUtils.AND);
        }
        xsb.append(tagsNumBuild.toString());

        //baggage
        Map<String, String> baggage = spanContext.getBizBaggage();
        xsb.appendEnd(StringUtils.mapToString(baggage));
        return xsb.toString();
    }
}