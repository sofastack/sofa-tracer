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
package com.alipay.sofa.tracer.plugins.dubbo.encoder;

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.self.Timestamp;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import io.opentracing.tag.Tags;

import java.io.IOException;
import java.util.Map;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/2/26 4:19 PM
 * @since:
 **/
public class DubboServerDigestJsonEncoder extends AbstractDigestSpanEncoder {

    @Override
    public String encode(SofaTracerSpan sofaTracerSpan) throws IOException {
        JsonStringBuilder data = new JsonStringBuilder();
        //span end time
        data.appendBegin(CommonSpanTags.TIME, Timestamp.format(sofaTracerSpan.getEndTime()));
        Map<String, String> tagStr = sofaTracerSpan.getTagsWithStr();
        Map<String, Number> tagNum = sofaTracerSpan.getTagsWithNumber();
        SofaTracerSpanContext context = sofaTracerSpan.getSofaTracerSpanContext();
        //TraceId
        data.append(CommonSpanTags.TRACE_ID, context.getTraceId());
        //SpanId
        data.append(CommonSpanTags.SPAN_ID, context.getSpanId());
        //Span Type
        data.append(Tags.SPAN_KIND.getKey(), tagStr.get(Tags.SPAN_KIND.getKey()));
        //local appName
        data.append(CommonSpanTags.LOCAL_APP, tagStr.get(CommonSpanTags.LOCAL_APP));
        //serviceName
        data.append(CommonSpanTags.SERVICE, tagStr.get(CommonSpanTags.SERVICE));
        //method
        data.append(CommonSpanTags.METHOD, tagStr.get(CommonSpanTags.METHOD));
        //local ip
        data.append(CommonSpanTags.LOCAL_HOST, tagStr.get(CommonSpanTags.LOCAL_HOST));
        //local port
        data.append(CommonSpanTags.LOCAL_PORT, tagStr.get(CommonSpanTags.LOCAL_PORT));
        //protocol
        data.append(CommonSpanTags.PROTOCOL, tagStr.get(CommonSpanTags.PROTOCOL));
        long serializeTime = getTime(tagNum.get(CommonSpanTags.SERVER_SERIALIZE_TIME));
        data.append(CommonSpanTags.SERVER_SERIALIZE_TIME, serializeTime);
        long deserializeTime = getTime(tagNum.get(CommonSpanTags.SERVER_DESERIALIZE_TIME));
        data.append(CommonSpanTags.SERVER_DESERIALIZE_TIME, deserializeTime);
        //Http status code
        data.append(CommonSpanTags.RESULT_CODE, tagStr.get(CommonSpanTags.RESULT_CODE));
        //error message
        if (StringUtils.isNotBlank(tagStr.get(Tags.ERROR.getKey()))) {
            data.append(Tags.ERROR.getKey(), tagStr.get(Tags.ERROR.getKey()));
        }
        //thread name
        data.append(CommonSpanTags.CURRENT_THREAD_NAME,
            tagStr.get(CommonSpanTags.CURRENT_THREAD_NAME));
        //time-consuming ms
        data.append(CommonSpanTags.TIME_COST_MILLISECONDS,
            (sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime()));
        this.appendBaggage(data, context);
        return data.toString();
    }

    private long getTime(Number number) {
        if (number != null) {
            return number.longValue();
        }
        return 0;
    }

    private void appendBaggage(JsonStringBuilder jsonStringBuilder,
                               SofaTracerSpanContext sofaTracerSpanContext) {
        //baggage
        jsonStringBuilder.appendEnd(CommonSpanTags.BAGGAGE,
            baggageSerialized(sofaTracerSpanContext));
    }
}
