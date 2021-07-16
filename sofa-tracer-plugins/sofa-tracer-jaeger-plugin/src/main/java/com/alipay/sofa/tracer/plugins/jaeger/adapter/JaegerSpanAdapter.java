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

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.span.SofaTracerSpanReferenceRelationship;
import io.jaegertracing.internal.*;
import io.jaegertracing.internal.reporters.RemoteReporter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JaegerSpanAdapter {

    public JaegerSpan convertToJaegerSpan(SofaTracerSpan sofaTracerSpan, RemoteReporter reporter) {
        final boolean computeDurationViaNanoTicks = false;
        final long startTimeNanoTicks = 0L;
        SofaTracerSpanContext context = sofaTracerSpan.getSofaTracerSpanContext();
        String operationName = sofaTracerSpan.getOperationName();
        long startTimeMicroseconds = sofaTracerSpan.getStartTime() * 1000;

        //创建JaegerSpan需要传入的Map<String,Object> tags
        Map<String, Object> tags = new LinkedHashMap<>();
        tags.putAll(sofaTracerSpan.getTagsWithStr());
        tags.putAll(sofaTracerSpan.getTagsWithBool());
        tags.putAll(sofaTracerSpan.getTagsWithNumber());

        // JaegerTracer
        JaegerTracer jaegerTracer = getJaegerTracer(sofaTracerSpan, reporter);

        //jaegerSpanContext
        JaegerSpanContext jaegerSpanContext = getJaegerSpanContext(context);

        List<Reference> references = getJaegerReference(sofaTracerSpan);

        //create JaegerSpan
        JaegerSpan jaegerSpan = new JaegerObjectFactory().createSpan(jaegerTracer, operationName,
            jaegerSpanContext, startTimeMicroseconds, startTimeNanoTicks,
            computeDurationViaNanoTicks, tags, references);

        //设置durationMicroseconds
        jaegerSpan.finish(sofaTracerSpan.getEndTime() * 1000);

        //设置logs
        jaegerSpan = setLogData(sofaTracerSpan, jaegerSpan);
        return jaegerSpan;
    }

    /**
     * 创建JaegerTracer对象
     * @param sofaTracerSpan
     * @param reporter
     * @return JaegerTracer
     */
    private JaegerTracer getJaegerTracer(SofaTracerSpan sofaTracerSpan, RemoteReporter reporter) {
        String serviceName = sofaTracerSpan.getTagsWithStr().get(CommonSpanTags.LOCAL_APP);
        serviceName = JaegerTracer.Builder.checkValidServiceName(serviceName);
        JaegerTracer.Builder jaegerBuilder = new JaegerTracer.Builder(serviceName)
            .withReporter(reporter);
        //add tags
        jaegerBuilder = addJaegerTracerTags(jaegerBuilder, sofaTracerSpan);
        //把sofa的traceid转换成jaeger的traceid，如果单独使用一个long无法转换
        jaegerBuilder.withTraceId128Bit();
        return jaegerBuilder.build();
    }

    /**
     * 转换SpanContext
     * @param sofaTracerSpanContext
     * @return JaegerSpanContext
     */
    private JaegerSpanContext getJaegerSpanContext(SofaTracerSpanContext sofaTracerSpanContext) {

        String sofaTraceId = sofaTracerSpanContext.getTraceId();
        //长度不够32位高位补0
        sofaTraceId = "00000000000000000000000000000000".substring(sofaTraceId.length())
                      + sofaTraceId;
        long traceIdHigh = Utils.hexToLong(sofaTraceId.substring(0, 16));
        long traceIdLow = Utils.hexToLong(sofaTraceId.substring(16));

        long spanId = FNV64HashCode(sofaTracerSpanContext.getSpanId());
        long parentId = FNV64HashCode(sofaTracerSpanContext.getParentId());
        //如果设置成1会发送两个span为什么？？
        byte flag = 1;
        return new JaegerSpanContext(traceIdHigh, traceIdLow, spanId, parentId, flag);
    }

    /**
     * 转换tags
     * @param builder
     * @param span
     * @return JaegerTracer.Builder
     */

    private JaegerTracer.Builder addJaegerTracerTags(JaegerTracer.Builder builder,
                                                     SofaTracerSpan span) {

        for (Map.Entry<String, String> e : span.getTagsWithStr().entrySet()) {
            builder.withTag(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, Number> e : span.getTagsWithNumber().entrySet()) {
            builder.withTag(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, Boolean> e : span.getTagsWithBool().entrySet()) {
            builder.withTag(e.getKey(), e.getValue());
        }

        return builder;
    }

    /**
     * 转化logdata，zipkin里面使用annotation来表示
     */
    private JaegerSpan setLogData(SofaTracerSpan span, JaegerSpan jaegerSpan) {
        List<com.alipay.common.tracer.core.span.LogData> sofaLogDatas = span.getLogs();

        for (com.alipay.common.tracer.core.span.LogData sofalogData : sofaLogDatas) {
            jaegerSpan.log(sofalogData.getTime() * 1000, sofalogData.getFields());
        }
        return jaegerSpan;
    }

    /**
     * 把字符串转换成long
     * @param data
     * @return long
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

    /**
     * 转换Reference
     * @param sofaTracerSpan
     * @return List<Reference>
     */
    private List<Reference> getJaegerReference(SofaTracerSpan sofaTracerSpan) {
        List<Reference> jaegerReferences = new ArrayList<>();
        List<SofaTracerSpanReferenceRelationship> sofaReferences = sofaTracerSpan
            .getSpanReferences();
        for (SofaTracerSpanReferenceRelationship sofaReference : sofaReferences) {
            // type 都是使用的opentracing中的常量，但是spancontext这两者需要互相转换
            JaegerSpanContext jaegerSpanContext = getJaegerSpanContext(sofaReference
                .getSofaTracerSpanContext());
            Reference jaegerReference = new Reference(jaegerSpanContext,
                sofaReference.getReferenceType());
            jaegerReferences.add(jaegerReference);
        }
        return jaegerReferences;
    }

}
