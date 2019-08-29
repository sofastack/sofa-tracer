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
package com.alipay.sofa.tracer.plugin.flexible;

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.self.Timestamp;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.util.Map;
import java.util.Set;

/**
 * FlexibleDigestJsonEncoder for flexible biz tracer
 *
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/8/2 11:39 AM
 * @since:
 **/
public class FlexibleDigestJsonEncoder extends AbstractDigestSpanEncoder {

    @Override
    public String encode(SofaTracerSpan span) {
        JsonStringBuilder bizJsonBuilder = new JsonStringBuilder();
        //span end time
        bizJsonBuilder.appendBegin(CommonSpanTags.TIME, Timestamp.format(span.getEndTime()));
        appendSlot(bizJsonBuilder, span);
        return bizJsonBuilder.toString();
    }

    private void appendSlot(JsonStringBuilder builder, SofaTracerSpan sofaTracerSpan) {
        SofaTracerSpanContext context = sofaTracerSpan.getSofaTracerSpanContext();
        Map<String, String> strTags = sofaTracerSpan.getTagsWithStr();
        Map<String, Number> numTags = sofaTracerSpan.getTagsWithNumber();
        Map<String, Number> boolTags = sofaTracerSpan.getTagsWithNumber();
        //app
        builder.append(CommonSpanTags.LOCAL_APP, strTags.get(CommonSpanTags.LOCAL_APP));
        //TraceId
        builder.append(CommonSpanTags.TRACE_ID, context.getTraceId());
        //SpanId
        builder.append(CommonSpanTags.SPAN_ID, context.getSpanId());
        //method
        builder.append(CommonSpanTags.METHOD, strTags.get(CommonSpanTags.METHOD));

        Set<String> strKeys = strTags.keySet();

        strKeys.forEach(key->{
            if (!isFlexible(key)){
                builder.append(key,strTags.get(key));
            }
        });
        Set<String> numKeys = numTags.keySet();
        numKeys.forEach(key->{
            if (!isFlexible(key)){
                builder.append(key,numTags.get(key));
            }
        });
        Set<String> boolKeys = boolTags.keySet();
        boolKeys.forEach(key->{
            if (!isFlexible(key)){
                builder.append(key,boolTags.get(key));
            }
        });

        //time-consuming ms
        builder.append(CommonSpanTags.TIME_COST_MILLISECONDS,
            (sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime()));
        builder.append(CommonSpanTags.CURRENT_THREAD_NAME,
                strTags.get(CommonSpanTags.CURRENT_THREAD_NAME));

        this.appendBaggage(builder, context);
    }

    private void appendBaggage(JsonStringBuilder builder,
                               SofaTracerSpanContext sofaTracerSpanContext) {
        //baggage
        builder.appendEnd(CommonSpanTags.BAGGAGE, baggageSerialized(sofaTracerSpanContext));
    }

    private boolean isFlexible(String key) {
        return CommonSpanTags.LOCAL_APP.equalsIgnoreCase(key)
               || CommonSpanTags.TRACE_ID.equalsIgnoreCase(key)
               || CommonSpanTags.SPAN_ID.equalsIgnoreCase(key)
               || CommonSpanTags.CURRENT_THREAD_NAME.equalsIgnoreCase(key)
               || CommonSpanTags.METHOD.equalsIgnoreCase(key);
    }
}