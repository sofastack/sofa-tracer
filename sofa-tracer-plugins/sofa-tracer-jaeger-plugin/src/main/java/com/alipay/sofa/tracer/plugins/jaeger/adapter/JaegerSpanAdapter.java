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
package com.alipay.sofa.tracer.plugins.jaeger.adapter;

import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.span.SofaTracerSpanReferenceRelationship;
import io.jaegertracing.internal.*;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * convert sofaTracerSpan to JaegerSpan and use UdpSender to send jaegerSpan to the jaeger agent
 */

public class JaegerSpanAdapter {

    public JaegerSpan convertAndReport(SofaTracerSpan sofaTracerSpan, JaegerTracer jaegerTracer) {
        final boolean computeDurationViaNanoTicks = false;
        final long startTimeNanoTicks = 0L;
        SofaTracerSpanContext context = sofaTracerSpan.getSofaTracerSpanContext();
        String operationName = sofaTracerSpan.getOperationName();
        long startTimeMicroseconds = sofaTracerSpan.getStartTime() * 1000;

        //construct tags in JaegerSpan
        Map<String, Object> tags = new LinkedHashMap<>();
        tags.putAll(sofaTracerSpan.getTagsWithStr());
        tags.putAll(sofaTracerSpan.getTagsWithBool());
        tags.putAll(sofaTracerSpan.getTagsWithNumber());

        //jaegerSpanContext
        JaegerSpanContext jaegerSpanContext = getJaegerSpanContext(context);

        List<Reference> references = getJaegerReference(sofaTracerSpan);

        //create JaegerSpan
        JaegerSpan jaegerSpan = new JaegerObjectFactory().createSpan(jaegerTracer, operationName,
            jaegerSpanContext, startTimeMicroseconds, startTimeNanoTicks,
            computeDurationViaNanoTicks, tags, references);

        //set durationMicroseconds, if the span is sampled will add AppendCommand to CommandQueue
        jaegerSpan.finish(sofaTracerSpan.getEndTime() * 1000);

        //set logs
        jaegerSpan = setLogData(sofaTracerSpan, jaegerSpan);

        //convert baggage
        jaegerSpan = convertBaggage(sofaTracerSpan, jaegerSpan);

        return jaegerSpan;
    }

    /**
     * convert SpanContext
     * @param sofaTracerSpanContext
     * @return JaegerSpanContext
     */
    private JaegerSpanContext getJaegerSpanContext(SofaTracerSpanContext sofaTracerSpanContext) {

        String sofaTraceId = sofaTracerSpanContext.getTraceId();
        //when length is less than 32 add 0 to the font
        sofaTraceId = "00000000000000000000000000000000".substring(sofaTraceId.length())
                      + sofaTraceId;
        long traceIdHigh = hexToLong(sofaTraceId.substring(0, 16));
        long traceIdLow = hexToLong(sofaTraceId.substring(16));

        long spanId = FNV64HashCode(sofaTracerSpanContext.getSpanId());
        long parentId = FNV64HashCode(sofaTracerSpanContext.getParentId());
        //when flag == 1 it means sampled
        byte flag = sofaTracerSpanContext.isSampled() ? (byte) 1 : (byte) 0;
        return new JaegerSpanContext(traceIdHigh, traceIdLow, spanId, parentId, flag);
    }

    /**
     * convert  businessBaggage and  systemBaggage in sofaTracer to the baggage  in jaeger
     * @param sofaTracerSpan
     * @param jaegerSpan
     * @return JaegerSpan
     */

    private JaegerSpan convertBaggage(SofaTracerSpan sofaTracerSpan, JaegerSpan jaegerSpan) {

        Iterable<Map.Entry<String, String>> baggages = sofaTracerSpan.getSofaTracerSpanContext()
            .baggageItems();
        for (Map.Entry<String, String> baggage : baggages) {
            jaegerSpan.setBaggageItem(baggage.getKey(), baggage.getValue());
        }
        return jaegerSpan;

    }

    /**
     * convert logdata
     */
    private JaegerSpan setLogData(SofaTracerSpan span, JaegerSpan jaegerSpan) {
        List<com.alipay.common.tracer.core.span.LogData> sofaLogDatas = span.getLogs();

        for (com.alipay.common.tracer.core.span.LogData sofalogData : sofaLogDatas) {
            jaegerSpan.log(sofalogData.getTime() * 1000, sofalogData.getFields());
        }
        return jaegerSpan;
    }

    /**
     * convert Reference
     * @param sofaTracerSpan
     * @return List<Reference>
     */
    private List<Reference> getJaegerReference(SofaTracerSpan sofaTracerSpan) {
        List<Reference> jaegerReferences = new ArrayList<>();
        List<SofaTracerSpanReferenceRelationship> sofaReferences = sofaTracerSpan
            .getSpanReferences();
        for (SofaTracerSpanReferenceRelationship sofaReference : sofaReferences) {
            // Type are constants in opentracing , but spanContext needs to  convert
            JaegerSpanContext jaegerSpanContext = getJaegerSpanContext(sofaReference
                .getSofaTracerSpanContext());
            Reference jaegerReference = new Reference(jaegerSpanContext,
                sofaReference.getReferenceType());
            jaegerReferences.add(jaegerReference);
        }
        return jaegerReferences;
    }

    /**
     * convert hexSting to long
     * @param hexString
     * @return converted long number
     */
    public long hexToLong(String hexString) {
        Assert.hasText(hexString, "Can't convert empty hex string to long");
        int length = hexString.length();
        if (length < 1) {
            throw new IllegalArgumentException("length must be more than zero : " + hexString);
        }
        if (length <= 16) {
            return Long.parseUnsignedLong(hexString, 16);
        }
        throw new IllegalArgumentException("length must  less than 16 :" + hexString);
    }

    /**
     * convert string to long
     * @param data origin string
     * @return long converted long
     */
    private static long FNV64HashCode(String data) {
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < data.length(); ++i) {
            char c = data.charAt(i);
            hash ^= c;
            hash *= 0x100000001b3L;
        }
        return hash;
    }

}
