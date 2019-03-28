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
package com.sofa.alipay.tracer.plugins.springcloud.encodes;

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.self.Timestamp;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import io.opentracing.tag.Tags;

import java.io.IOException;
import java.util.Map;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 3:30 PM
 * @since:
 **/
public class OpenFeignDigestJsonEncoder extends AbstractDigestSpanEncoder {

    @Override
    public String encode(SofaTracerSpan span) throws IOException {
        JsonStringBuilder result = new JsonStringBuilder();
        //span end time
        result.appendBegin("time", Timestamp.format(span.getEndTime()));
        appendSlot(result, span);
        return result.toString();
    }

    private void appendSlot(JsonStringBuilder buffer, SofaTracerSpan sofaTracerSpan) {
        SofaTracerSpanContext context = sofaTracerSpan.getSofaTracerSpanContext();
        Map<String, String> tagStrMap = sofaTracerSpan.getTagsWithStr();
        Map<String, Number> tagNumMap = sofaTracerSpan.getTagsWithNumber();
        buffer.append(CommonSpanTags.LOCAL_APP, tagStrMap.get(CommonSpanTags.LOCAL_APP));
        buffer.append("traceId", context.getTraceId());
        buffer.append("spanId", context.getSpanId());
        buffer.append(CommonSpanTags.REQUEST_URL, tagStrMap.get(CommonSpanTags.REQUEST_URL));
        buffer.append(CommonSpanTags.METHOD, tagStrMap.get(CommonSpanTags.METHOD));
        buffer.append(CommonSpanTags.RESULT_CODE, tagStrMap.get(CommonSpanTags.RESULT_CODE));
        buffer.append(Tags.ERROR.getKey(), tagStrMap.get(Tags.ERROR.getKey()));
        Number requestSize = tagNumMap.get(CommonSpanTags.REQ_SIZE);
        buffer
            .append(CommonSpanTags.REQ_SIZE, (requestSize == null ? 0L : requestSize.longValue()));
        Number responseSize = tagNumMap.get(CommonSpanTags.RESP_SIZE);
        buffer.append(CommonSpanTags.RESP_SIZE,
            (responseSize == null ? 0L : responseSize.longValue()));
        long duration = sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime();
        //time-consuming ms
        buffer.append("time.cost.milliseconds", duration);
        buffer.append(CommonSpanTags.CURRENT_THREAD_NAME,
            tagStrMap.get(CommonSpanTags.CURRENT_THREAD_NAME));
        //target appName
        buffer.append(CommonSpanTags.REMOTE_HOST, tagStrMap.get(CommonSpanTags.REMOTE_HOST));
        buffer.append(CommonSpanTags.REMOTE_PORT, tagStrMap.get(CommonSpanTags.REMOTE_PORT));
        buffer.append(CommonSpanTags.COMPONENT_CLIENT,
            tagStrMap.get(CommonSpanTags.COMPONENT_CLIENT));
        this.appendBaggage(buffer, context);
    }

    private void appendBaggage(JsonStringBuilder jsonStringBuilder,
                               SofaTracerSpanContext sofaTracerSpanContext) {
        //baggage
        jsonStringBuilder.appendEnd("baggage", baggageSerialized(sofaTracerSpanContext));
    }
}
