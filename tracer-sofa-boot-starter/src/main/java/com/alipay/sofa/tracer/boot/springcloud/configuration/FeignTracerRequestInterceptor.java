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
package com.alipay.sofa.tracer.boot.springcloud.configuration;

import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import static com.alipay.common.tracer.core.registry.AbstractTextB3Formatter.BAGGAGE_KEY_PREFIX;
import static com.alipay.common.tracer.core.registry.AbstractTextB3Formatter.BAGGAGE_SYS_KEY_PREFIX;
import static com.alipay.common.tracer.core.registry.AbstractTextB3Formatter.PARENT_SPAN_ID_KEY_HEAD;
import static com.alipay.common.tracer.core.registry.AbstractTextB3Formatter.SAMPLED_KEY_HEAD;
import static com.alipay.common.tracer.core.registry.AbstractTextB3Formatter.SPAN_ID_KEY_HEAD;
import static com.alipay.common.tracer.core.registry.AbstractTextB3Formatter.TRACE_ID_KEY_HEAD;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/12/30 11:41 PM
 * @since:
 **/
public class FeignTracerRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        SofaTracerSpan currentSpan = SofaTraceContextHolder.getSofaTraceContext().getCurrentSpan();
        if (currentSpan != null) {
            SofaTracerSpanContext spanContext = currentSpan.getSofaTracerSpanContext();
            //Tracing Context
            template.header(TRACE_ID_KEY_HEAD, encodedValue(spanContext.getTraceId()));
            template.header(SPAN_ID_KEY_HEAD, encodedValue(spanContext.getSpanId()));
            template.header(PARENT_SPAN_ID_KEY_HEAD, encodedValue(spanContext.getParentId()));
            template.header(SPAN_ID_KEY_HEAD, encodedValue(spanContext.getSpanId()));
            template
                .header(SAMPLED_KEY_HEAD, encodedValue(String.valueOf(spanContext.isSampled())));
            //System Baggage items
            for (Map.Entry<String, String> entry : spanContext.getSysBaggage().entrySet()) {
                String key = BAGGAGE_SYS_KEY_PREFIX
                             + StringUtils.escapePercentEqualAnd(entry.getKey());
                String value = encodedValue(StringUtils.escapePercentEqualAnd(entry.getValue()));
                template.header(key, value);
            }
            //Business Baggage items
            for (Map.Entry<String, String> entry : spanContext.getBizBaggage().entrySet()) {
                String key = BAGGAGE_KEY_PREFIX + StringUtils.escapePercentEqualAnd(entry.getKey());
                String value = encodedValue(StringUtils.escapePercentEqualAnd(entry.getValue()));
                template.header(key, value);
            }
        }
    }

    protected String encodedValue(String value) {
        if (StringUtils.isBlank(value)) {
            return StringUtils.EMPTY_STRING;
        }
        try {
            return URLEncoder.encode(value, SofaTracerConstant.DEFAULT_UTF8_ENCODING);
        } catch (UnsupportedEncodingException e) {
            // not much we can do, try raw value
            return value;
        }
    }
}
