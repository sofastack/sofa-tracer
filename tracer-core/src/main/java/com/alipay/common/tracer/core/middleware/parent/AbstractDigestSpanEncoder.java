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
package com.alipay.common.tracer.core.middleware.parent;

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.appender.self.Timestamp;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import io.opentracing.tag.Tags;

import java.io.IOException;
import java.util.Map;

/**
 *
 * @author luoguimu123
 * @version $Id: AbstractDigestSpanEncoder.java, v 0.1 August 28, 2017 10:23 AM luoguimu123 Exp $
 */
public abstract class AbstractDigestSpanEncoder implements SpanEncoder<SofaTracerSpan> {

    @Override
    public String encode(SofaTracerSpan span) throws IOException {
        if ("false".equalsIgnoreCase(SofaTracerConfiguration
            .getProperty(SofaTracerConfiguration.JSON_FORMAT_OUTPUT))) {
            return encodeXsbSpan(span);
        } else {
            return encodeJsbSpan(span);
        }
    }

    /**
     * encodeJsbSpan
     * @param span
     * @return
     */
    private String encodeJsbSpan(SofaTracerSpan span) {
        JsonStringBuilder jsb = new JsonStringBuilder();
        // common tag
        appendJsonCommonSlot(jsb, span);
        // component tag
        appendComponentSlot(null, jsb, span);
        // baggage
        jsb.append(CommonSpanTags.SYS_BAGGAGE,
            baggageSystemSerialized(span.getSofaTracerSpanContext()));
        jsb.appendEnd(CommonSpanTags.BIZ_BAGGAGE,
            baggageSerialized(span.getSofaTracerSpanContext()));

        return jsb.toString();
    }

    /**
     * encodeXsbSpan
     * @param span
     * @return
     */
    private String encodeXsbSpan(SofaTracerSpan span) {
        XStringBuilder xsb = new XStringBuilder();
        // common tag
        appendXsbCommonSlot(xsb, span);
        // component tag
        appendComponentSlot(xsb, null, span);
        // sys baggage
        xsb.append(baggageSystemSerialized(span.getSofaTracerSpanContext()));
        // biz baggage
        xsb.appendEnd(baggageSerialized(span.getSofaTracerSpanContext()));
        return xsb.toString();
    }

    /**
     * override by sub class
     * @param xsb
     * @param jsb
     * @param span
     */
    protected abstract void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                                SofaTracerSpan span);

    /**
     * System transparent transmission of data
     * @param spanContext span context
     * @return String
     */
    protected String baggageSystemSerialized(SofaTracerSpanContext spanContext) {
        return spanContext.getSysSerializedBaggage();
    }

    /**
     * Business transparent transmission of data
     * @param spanContext span context
     * @return
     */
    protected String baggageSerialized(SofaTracerSpanContext spanContext) {
        return spanContext.getBizSerializedBaggage();
    }

    /**
     * common tag to json format
     * @param jsb
     * @param span
     */
    protected void appendJsonCommonSlot(JsonStringBuilder jsb, SofaTracerSpan span) {
        SofaTracerSpanContext context = span.getSofaTracerSpanContext();
        Map<String, String> tagWithStr = span.getTagsWithStr();
        //span end time
        jsb.appendBegin(CommonSpanTags.TIME, Timestamp.format(span.getEndTime()));
        //app
        jsb.append(CommonSpanTags.LOCAL_APP, tagWithStr.get(CommonSpanTags.LOCAL_APP));
        //TraceId
        jsb.append(CommonSpanTags.TRACE_ID, context.getTraceId());
        //SpanId
        jsb.append(CommonSpanTags.SPAN_ID, context.getSpanId());
        //Span Kind
        jsb.append(Tags.SPAN_KIND.getKey(), tagWithStr.get(Tags.SPAN_KIND.getKey()));
        // result code
        jsb.append(CommonSpanTags.RESULT_CODE, tagWithStr.get(CommonSpanTags.RESULT_CODE));
        // thread name
        jsb.append(CommonSpanTags.CURRENT_THREAD_NAME,
            tagWithStr.get(CommonSpanTags.CURRENT_THREAD_NAME));
        // time.cost.milliseconds
        jsb.append(CommonSpanTags.TIME_COST_MILLISECONDS, (span.getEndTime() - span.getStartTime())
                                                          + SofaTracerConstant.MS);
    }

    /**
     *  common tag to XStringBuilder format
     * @param xsb
     * @param span
     */
    protected void appendXsbCommonSlot(XStringBuilder xsb, SofaTracerSpan span) {
        SofaTracerSpanContext context = span.getSofaTracerSpanContext();
        Map<String, String> tagWithStr = span.getTagsWithStr();
        //span end time
        xsb.append(Timestamp.format(span.getEndTime()));
        //appName
        xsb.append(tagWithStr.get(CommonSpanTags.LOCAL_APP));
        //TraceId
        xsb.append(context.getTraceId());
        //RpcId
        xsb.append(context.getSpanId());
        //span kind
        xsb.append(tagWithStr.get(Tags.SPAN_KIND.getKey()));
        // result code
        xsb.append(tagWithStr.get(CommonSpanTags.RESULT_CODE));
        // thread name
        xsb.append(tagWithStr.get(CommonSpanTags.CURRENT_THREAD_NAME));
        // time.cost.milliseconds
        xsb.append((span.getEndTime() - span.getStartTime()) + SofaTracerConstant.MS);
    }
}