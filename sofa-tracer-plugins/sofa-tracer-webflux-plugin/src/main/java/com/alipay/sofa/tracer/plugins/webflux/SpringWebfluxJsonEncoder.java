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
package com.alipay.sofa.tracer.plugins.webflux;

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
 * webflux json encoder
 *
 * @author xiang.sheng
 */
public class SpringWebfluxJsonEncoder extends AbstractDigestSpanEncoder {

    @Override
    public String encode(SofaTracerSpan span) throws IOException {
        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder();
        jsonStringBuilder.appendBegin("time", Timestamp.format(span.getEndTime()));
        appendSlot(jsonStringBuilder, span);
        return jsonStringBuilder.toString();
    }

    private void appendSlot(JsonStringBuilder json, SofaTracerSpan sofaTracerSpan) {

        SofaTracerSpanContext context = sofaTracerSpan.getSofaTracerSpanContext();
        Map<String, String> tagWithStr = sofaTracerSpan.getTagsWithStr();
        Map<String, Number> tagWithNumber = sofaTracerSpan.getTagsWithNumber();
        json.append(CommonSpanTags.LOCAL_APP, tagWithStr.get(CommonSpanTags.LOCAL_APP));
        //remote address, webflux
        if (tagWithStr.get(CommonSpanTags.REMOTE_APP) != null) {
            json.append(CommonSpanTags.REMOTE_APP, tagWithStr.get(CommonSpanTags.REMOTE_APP));
        }
        //TraceId
        json.append("traceId", context.getTraceId());
        //RpcId
        json.append("spanId", context.getSpanId());
        //request URL
        json.append(CommonSpanTags.REQUEST_URL, tagWithStr.get(CommonSpanTags.REQUEST_URL));
        //request Method
        json.append(CommonSpanTags.METHOD, tagWithStr.get(CommonSpanTags.METHOD));
        //Http code
        json.append(CommonSpanTags.RESULT_CODE, tagWithStr.get(CommonSpanTags.RESULT_CODE));
        //error info
        if (tagWithStr.containsKey(Tags.ERROR.getKey())) {
            json.append(Tags.ERROR.getKey(), tagWithStr.get(Tags.ERROR.getKey()));
        }
        Number requestSize = tagWithNumber.get(CommonSpanTags.REQ_SIZE);
        //Request Body Size
        json.append(CommonSpanTags.REQ_SIZE, (requestSize == null ? 0L : requestSize.longValue()));
        Number responseSize = tagWithNumber.get(CommonSpanTags.RESP_SIZE);
        //Response Body Size
        json.append(CommonSpanTags.RESP_SIZE,
            (responseSize == null ? 0L : responseSize.longValue()));
        json.append("time.cost.milliseconds",
            (sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime()));
        json.append(CommonSpanTags.CURRENT_THREAD_NAME,
            tagWithStr.get(CommonSpanTags.CURRENT_THREAD_NAME));
        json.appendEnd("baggage", baggageSerialized(context));
    }
}
