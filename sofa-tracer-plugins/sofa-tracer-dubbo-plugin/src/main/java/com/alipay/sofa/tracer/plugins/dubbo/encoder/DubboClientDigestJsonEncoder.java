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
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/2/26 3:56 PM
 * @since:
 **/
public class DubboClientDigestJsonEncoder extends AbstractDigestSpanEncoder {

    @Override
    public String encode(SofaTracerSpan sofaTracerSpan) throws IOException {

        JsonStringBuilder data = new JsonStringBuilder();
        //span end time
        data.appendBegin("time", Timestamp.format(sofaTracerSpan.getEndTime()));

        Map<String, String> tagStr = sofaTracerSpan.getTagsWithStr();
        Map<String, Number> tagNum = sofaTracerSpan.getTagsWithNumber();
        SofaTracerSpanContext context = sofaTracerSpan.getSofaTracerSpanContext();

        // TraceId
        data.append("traceId", context.getTraceId());
        // SpanId
        data.append("spanId", context.getSpanId());
        // Span Type
        data.append(Tags.SPAN_KIND.getKey(), tagStr.get(Tags.SPAN_KIND.getKey()));
        // app name
        data.append(CommonSpanTags.LOCAL_APP, tagStr.get(CommonSpanTags.LOCAL_APP));
        // protocol
        data.append(CommonSpanTags.PROTOCOL, tagStr.get(CommonSpanTags.PROTOCOL));
        // serviceName
        data.append(CommonSpanTags.SERVICE, tagStr.get(CommonSpanTags.SERVICE));
        // method
        data.append(CommonSpanTags.METHOD, tagStr.get(CommonSpanTags.METHOD));
        //invoke type
        data.append(CommonSpanTags.INVOKE_TYPE, tagStr.get(CommonSpanTags.INVOKE_TYPE));
        //target ip
        data.append(CommonSpanTags.REMOTE_HOST, tagStr.get(CommonSpanTags.REMOTE_HOST));
        //target port
        data.append(CommonSpanTags.REMOTE_PORT, tagStr.get(CommonSpanTags.REMOTE_PORT));
        //local ip
        data.append(CommonSpanTags.LOCAL_HOST, tagStr.get(CommonSpanTags.LOCAL_HOST));
        //request serialize time
        data.append(CommonSpanTags.CLIENT_SERIALIZE_TIME,
            tagNum.get(CommonSpanTags.CLIENT_SERIALIZE_TIME));
        //response deserialize time
        data.append(CommonSpanTags.CLIENT_DESERIALIZE_TIME,
            tagNum.get(CommonSpanTags.CLIENT_DESERIALIZE_TIME));
        //Request Body bytes length
        Number reqSizeNum = tagNum.get(CommonSpanTags.REQ_SIZE);
        data.append(CommonSpanTags.REQ_SIZE, reqSizeNum == null ? 0 : reqSizeNum.longValue());
        //Response Body bytes length
        Number respSizeNum = tagNum.get(CommonSpanTags.REQ_SIZE);
        data.append(CommonSpanTags.RESP_SIZE, respSizeNum == null ? 0 : respSizeNum.longValue());
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
        data.append("time.cost.milliseconds",
            (sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime()));
        this.appendBaggage(data, context);
        return data.toString();
    }

    private void appendBaggage(JsonStringBuilder jsonStringBuilder,
                               SofaTracerSpanContext sofaTracerSpanContext) {
        //baggage
        jsonStringBuilder.appendEnd("baggage", baggageSerialized(sofaTracerSpanContext));
    }

}
