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
package com.alipay.sofa.tracer.spring.zipkin.adapter;

import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import zipkin2.Endpoint;
import zipkin2.Span;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Map;

/***
 * ZipkinV2SpanAdapter : convent sofaTracer span model to zipkin span model
 * @author guolei.sgl 05/09/2018
 */
public class ZipkinV2SpanAdapter {

    /**
     * cache and performance improve
     */
    private int                 ipAddressInt        = -1;

    private static final String SOFARPC_TRACER_TYPE = "RPC_TRACER";

    /**
     * convent sofaTracerSpan model to zipKinSpan model
     * @param sofaTracerSpan
     * @return
     */
    public Span convertToZipkinSpan(SofaTracerSpan sofaTracerSpan) {
        Span.Builder zipkinSpanBuilder = Span.newBuilder();
        zipkinSpanBuilder.timestamp(sofaTracerSpan.getStartTime() * 1000);
        // Zipkin is in nanosecond  cr-cs
        zipkinSpanBuilder
            .duration((sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime()) * 1000);
        //Annotations
        Endpoint endpoint = getZipkinEndpoint(sofaTracerSpan.getOperationName());
        this.addZipkinAnnotations(zipkinSpanBuilder, sofaTracerSpan, endpoint);
        this.addZipkinBinaryAnnotationsWithTags(zipkinSpanBuilder, sofaTracerSpan);
        this.addZipkinBinaryAnnotationsWithBaggage(zipkinSpanBuilder, sofaTracerSpan);
        //traceId
        SofaTracerSpanContext sofaTracerSpanContext = sofaTracerSpan.getSofaTracerSpanContext();
        // get current span's parentSpan
        SofaTracerSpan parentSofaTracerSpan = sofaTracerSpan.getParentSofaTracerSpan();
        // v2 span model will padLeft automatic
        zipkinSpanBuilder.traceId(sofaTracerSpanContext.getTraceId());
        String parentSpanId = sofaTracerSpanContext.getParentId();
        // convent parentSpanId
        if (sofaTracerSpan.isServer() && StringUtils.isNotBlank(parentSpanId)) {
            // v2 span model Unsets the {@link Span#parentId()} if the input is 0.
            zipkinSpanBuilder.parentId(spanIdToLong(parentSpanId));
        } else if (parentSofaTracerSpan != null) {
            SofaTracerSpanContext parentSofaTracerSpanContext = parentSofaTracerSpan
                .getSofaTracerSpanContext();
            if (parentSofaTracerSpanContext != null) {
                parentSpanId = parentSofaTracerSpanContext.getSpanId();
                if (StringUtils.isNotBlank(parentSpanId)) {
                    zipkinSpanBuilder.parentId(spanIdToLong(parentSpanId));
                }
            }
        }
        //convent spanId
        String spanId = sofaTracerSpanContext.getSpanId();
        zipkinSpanBuilder.id(spanIdToLong(spanId));
        //convent span.kind
        zipkinSpanBuilder.kind(sofaTracerSpan.isClient() ? Span.Kind.CLIENT : Span.Kind.SERVER);
        //convent name
        String operationName = sofaTracerSpan.getOperationName();
        if (StringUtils.isNotBlank(operationName)) {
            zipkinSpanBuilder.name(operationName);
        } else {
            zipkinSpanBuilder.name(StringUtils.EMPTY_STRING);
        }
        // adapter SOFARPC span model
        if (SOFARPC_TRACER_TYPE.equals(sofaTracerSpan.getSofaTracer().getTracerType())) {
            //if current span's kind is rpcClient,set localEndpoint by parentSofaTracerSpan's operationName
            if (sofaTracerSpan.isClient() && parentSofaTracerSpan != null) {
                zipkinSpanBuilder.localEndpoint(getZipkinEndpoint(parentSofaTracerSpan
                    .getOperationName()));
            }
        }
        return zipkinSpanBuilder.build();
    }

    public static long spanIdToLong(String spanId) {
        return FNV64HashCode(spanId);
    }

    /**
     * from http://en.wikipedia.org/wiki/Fowler_Noll_Vo_hash
     * @param data String data
     * @return fnv hash code
     */
    public static long FNV64HashCode(String data) {
        //hash FNVHash64 : http://www.isthe.com/chongo/tech/comp/fnv/index.html#FNV-param
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < data.length(); ++i) {
            char c = data.charAt(i);
            hash ^= c;
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    private Endpoint getZipkinEndpoint(String operationName) {
        InetAddress ipAddress = null;
        if (this.ipAddressInt <= 0) {
            try {
                ipAddress = InetAddress.getLocalHost();
                this.ipAddressInt = ByteBuffer.wrap(ipAddress.getAddress()).getInt();
            } catch (UnknownHostException e) {
                //127.0.0.1 256 进制
                this.ipAddressInt = 256 * 256 * 256 * 127 + 1;
            }
        }
        return Endpoint.newBuilder().serviceName(operationName).ip(ipAddress).build();
    }

    /**
     * Adds binary annotation from the Open Tracing Span
     */
    private void addZipkinBinaryAnnotationsWithTags(Span.Builder zipkinSpan, SofaTracerSpan span) {
        for (Map.Entry<String, String> e : span.getTagsWithStr().entrySet()) {
            zipkinSpan.putTag(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, Number> e : span.getTagsWithNumber().entrySet()) {
            zipkinSpan.putTag(e.getKey(), e.getValue().toString());
        }
        for (Map.Entry<String, Boolean> e : span.getTagsWithBool().entrySet()) {
            zipkinSpan.putTag(e.getKey(), e.getValue().toString());
        }
    }

    private void addZipkinBinaryAnnotationsWithBaggage(Span.Builder zipkinSpan, SofaTracerSpan span) {
        SofaTracerSpanContext sofaTracerSpanContext = span.getSofaTracerSpanContext();
        if (sofaTracerSpanContext != null) {
            Map<String, String> sysBaggage = sofaTracerSpanContext.getSysBaggage();
            for (Map.Entry<String, String> e : sysBaggage.entrySet()) {
                zipkinSpan.putTag(e.getKey(), e.getValue());
            }
            Map<String, String> bizBaggage = sofaTracerSpanContext.getBizBaggage();
            for (Map.Entry<String, String> e : bizBaggage.entrySet()) {
                zipkinSpan.putTag(e.getKey(), e.getValue());
            }
        }
    }

    private void addZipkinAnnotations(Span.Builder zipkinSpan, SofaTracerSpan span,
                                      Endpoint endpoint) {
        for (LogData logData : span.getLogs()) {
            Map<String, ?> fields = logData.getFields();
            if (fields == null || fields.size() <= 0) {
                continue;
            }
            for (Map.Entry<String, ?> entry : fields.entrySet()) {
                zipkinSpan.addAnnotation(logData.getTime() * 1000, entry.getValue().toString())
                    .localEndpoint(endpoint);
            }
        }
    }

}
