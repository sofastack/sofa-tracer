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
package com.sofa.alipay.tracer.plugins.rest;

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.self.Timestamp;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.io.IOException;
import java.util.Map;

/**
 * RestTemplateDigestJsonEncoder
 * @author: guolei.sgl
 * @since: v2.3.0
 */
public class RestTemplateDigestJsonEncoder extends AbstractDigestSpanEncoder {

    @Override
    public String encode(SofaTracerSpan span) throws IOException {
        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder();
        //span end time
        jsonStringBuilder.appendBegin("time", Timestamp.format(span.getEndTime()));
        appendSlot(jsonStringBuilder, span);
        return jsonStringBuilder.toString();
    }

    private void appendSlot(JsonStringBuilder jsonStringBuilder, SofaTracerSpan sofaTracerSpan) {
        SofaTracerSpanContext context = sofaTracerSpan.getSofaTracerSpanContext();
        Map<String, String> tagWithStr = sofaTracerSpan.getTagsWithStr();
        Map<String, Number> tagWithNumber = sofaTracerSpan.getTagsWithNumber();
        //app
        jsonStringBuilder
            .append(CommonSpanTags.LOCAL_APP, tagWithStr.get(CommonSpanTags.LOCAL_APP));
        //TraceId
        jsonStringBuilder.append("traceId", context.getTraceId());
        //SpanId
        jsonStringBuilder.append("spanId", context.getSpanId());
        //URL
        jsonStringBuilder.append(CommonSpanTags.REQUEST_URL,
            tagWithStr.get(CommonSpanTags.REQUEST_URL));
        //POST/GET
        jsonStringBuilder.append(CommonSpanTags.METHOD, tagWithStr.get(CommonSpanTags.METHOD));
        //Http status code
        jsonStringBuilder.append(CommonSpanTags.RESULT_CODE,
            tagWithStr.get(CommonSpanTags.RESULT_CODE));
        Number responseSize = tagWithNumber.get(CommonSpanTags.RESP_SIZE);
        //Response Body bytes length
        jsonStringBuilder.append(CommonSpanTags.RESP_SIZE, (responseSize == null ? 0L
            : responseSize.longValue()));
        //time-consuming ms
        jsonStringBuilder.append("time.cost.milliseconds",
            (sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime()));
        jsonStringBuilder.append(CommonSpanTags.CURRENT_THREAD_NAME,
            tagWithStr.get(CommonSpanTags.CURRENT_THREAD_NAME));
        //target appName
        jsonStringBuilder.append(CommonSpanTags.REMOTE_APP,
            tagWithStr.get(CommonSpanTags.REMOTE_APP));
        this.appendBaggage(jsonStringBuilder, context);
    }

    private void appendBaggage(JsonStringBuilder jsonStringBuilder,
                               SofaTracerSpanContext sofaTracerSpanContext) {
        //baggage
        jsonStringBuilder.appendEnd("baggage", baggageSerialized(sofaTracerSpanContext));
    }
}
