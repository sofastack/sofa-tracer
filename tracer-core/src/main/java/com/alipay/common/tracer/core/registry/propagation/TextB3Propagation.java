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
package com.alipay.common.tracer.core.registry.propagation;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.utils.StringUtils;
import io.opentracing.propagation.TextMap;

import java.util.HashMap;
import java.util.Map;

public class TextB3Propagation implements B3Propagation<TextMap> {
    private PropagationEncoder encoder;
    private PropagationDecoder decoder;

    public TextB3Propagation(PropagationEncoder encoder, PropagationDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    @Override
    public SofaTracerSpanContext extract(TextMap carrier) {
        boolean isHaveTraceId = false;
        String traceId = null;

        //Find out if there is a traceid. If not, considered that there is no trace context
        for (Map.Entry<String, String> entry : carrier) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (StringUtils.isBlank(key)) {
                continue;
            }
            if (TRACE_ID_KEY_HEAD.equalsIgnoreCase(key)) {
                isHaveTraceId = true;
                traceId = decoder.decodeValue(entry.getValue());
                break;
            }
        }
        if (!isHaveTraceId) {
            return null;
        }

        String spanId = null;
        String parentId = null;
        boolean sampled = false;
        boolean isGetSampled = false;
        //sys bizBaggage
        Map<String, String> sysBaggage = new HashMap<String, String>();
        //bizBaggage
        Map<String, String> bizBaggage = new HashMap<String, String>();

        //Get others trace context items, the first value wins.
        for (Map.Entry<String, String> entry : carrier) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (StringUtils.isBlank(key)) {
                continue;
            }
            if (spanId == null && SPAN_ID_KEY_HEAD.equalsIgnoreCase(key)) {
                spanId = decoder.decodeValue(value);
            }
            if (parentId == null && PARENT_SPAN_ID_KEY_HEAD.equalsIgnoreCase(key)) {
                parentId = decoder.decodeValue(value);
            }
            if (!isGetSampled && SAMPLED_KEY_HEAD.equalsIgnoreCase(key)) {
                String valueTmp = decoder.decodeValue(value);
                if ("1".equals(valueTmp)) {
                    sampled = true;
                } else if ("0".equals(valueTmp)) {
                    sampled = false;
                } else {
                    sampled = Boolean.parseBoolean(valueTmp);
                }
                isGetSampled = true;
            }
            if (key.indexOf(BAGGAGE_SYS_KEY_PREFIX) == 0) {
                String keyTmp = StringUtils.unescapeEqualAndPercent(key).substring(
                    BAGGAGE_SYS_KEY_PREFIX.length());
                String valueTmp = decoder.decodeValue(StringUtils.unescapeEqualAndPercent(value));
                sysBaggage.put(keyTmp, valueTmp);
            }
            if (key.indexOf(BAGGAGE_KEY_PREFIX) == 0) {
                String keyTmp = StringUtils.unescapeEqualAndPercent(key).substring(
                    BAGGAGE_KEY_PREFIX.length());
                String valueTmp = decoder.decodeValue(StringUtils.unescapeEqualAndPercent(value));
                bizBaggage.put(keyTmp, valueTmp);
            }
        }

        if (spanId == null) {
            spanId = SofaTracer.ROOT_SPAN_ID;
        }
        if (parentId == null) {
            parentId = StringUtils.EMPTY_STRING;
        }
        SofaTracerSpanContext sofaTracerSpanContext = new SofaTracerSpanContext(traceId, spanId,
            parentId, sampled);
        if (sysBaggage.size() > 0) {
            sofaTracerSpanContext.addSysBaggage(sysBaggage);
        }
        if (bizBaggage.size() > 0) {
            sofaTracerSpanContext.addBizBaggage(bizBaggage);
        }

        return sofaTracerSpanContext;
    }

    @Override
    public void inject(SofaTracerSpanContext spanContext, TextMap carrier) {
        if (carrier == null || spanContext == null) {
            return;
        }
        //Tracing Context
        carrier.put(TRACE_ID_KEY_HEAD, encoder.encodeValue(spanContext.getTraceId()));
        carrier.put(SPAN_ID_KEY_HEAD, encoder.encodeValue(spanContext.getSpanId()));
        carrier.put(PARENT_SPAN_ID_KEY_HEAD, encoder.encodeValue(spanContext.getParentId()));
        carrier.put(SPAN_ID_KEY_HEAD, encoder.encodeValue(spanContext.getSpanId()));
        //System Baggage items
        for (Map.Entry<String, String> entry : spanContext.getSysBaggage().entrySet()) {
            String key = BAGGAGE_SYS_KEY_PREFIX + StringUtils.escapePercentEqualAnd(entry.getKey());
            String value = StringUtils.escapePercentEqualAnd(entry.getValue());
            carrier.put(key, value);
        }
        //Business Baggage items
        for (Map.Entry<String, String> entry : spanContext.getBizBaggage().entrySet()) {
            String key = BAGGAGE_KEY_PREFIX + StringUtils.escapePercentEqualAnd(entry.getKey());
            String value = encoder.encodeValue(StringUtils.escapePercentEqualAnd(entry.getValue()));
            carrier.put(key, value);
        }
    }
}
