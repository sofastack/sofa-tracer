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
package com.alipay.sofa.tracer.boot.zipkin;

import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.listener.SpanReportListener;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.common.tracer.core.utils.TracerUtils;
import com.alipay.sofa.tracer.boot.zipkin.sender.ZipkinRestTemplateSender;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * ZipkinSofaTracerSpanRemoteReporter report {@link SofaTracerSpan} to Zipkin
 *
 * @author yangguanchao
 * @since 2018/05/01
 */
public class ZipkinSofaTracerSpanRemoteReporter implements SpanReportListener, Flushable, Closeable {

    private static String                  processId           = TracerUtils.getPID();

    private final ZipkinRestTemplateSender sender;

    private final AsyncReporter<Span>      delegate;

    private static final String            SOFARPC_TRACER_TYPE = "RPC_TRACER";

    /***
     * cache and performance improve
     */
    private int                            ipAddressInt        = -1;

    public ZipkinSofaTracerSpanRemoteReporter(RestTemplate restTemplate, String baseUrl) {
        this.sender = new ZipkinRestTemplateSender(restTemplate, baseUrl);
        this.delegate = AsyncReporter.create(sender);
    }

    @Override
    public void onSpanReport(SofaTracerSpan span) {
        if (span == null) {
            return;
        }
        //convert
        Span zipkinSpan = convertToZipkinSpan(span);
        this.delegate.report(zipkinSpan);
    }

    @Override
    public void flush() throws IOException {
        this.delegate.flush();
    }

    @Override
    public void close() {
        this.delegate.close();
    }

    /**
     * convent sofaTracerSpan model to zipKinSpan model
     * @param sofaTracerSpan
     * @return
     */
    private Span convertToZipkinSpan(SofaTracerSpan sofaTracerSpan) {
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
        /**
         * Changes:
         * 1.From using zipkin span's traceId alone, to using both traceid and traceIdHigh
         * 2.From using part of SpanContext's traceId as radix 10, to using full traceId as hexadecimal(radix 16)
         * So that the traceId in the zipkin trace data is consistent with the traceId in the application log files.
         *
         * 3.When traceId is received from the previous node, the original algorithm will not be able to cut
         *   off the pid in tail of the traceId, because it does not know the pid of the sender.
         *   resulting in over range when convert it to long type.
         */

        // v2 span model will padLeft automatic
        zipkinSpanBuilder.traceId(sofaTracerSpanContext.getTraceId());
        String parentSpanId = sofaTracerSpanContext.getParentId();
        // convent parentSpanId
        if (sofaTracerSpan.isServer() && parentSpanId != null
            && StringUtils.isNotBlank(parentSpanId)) {
            // v2 span model Unsets the {@link Span#parentId()} if the input is 0.
            zipkinSpanBuilder.parentId(spanIdToLong(parentSpanId));
        } else if (parentSofaTracerSpan != null) {
            SofaTracerSpanContext parentSofaTracerSpanContext = parentSofaTracerSpan
                .getSofaTracerSpanContext();
            if (parentSofaTracerSpanContext != null) {
                parentSpanId = parentSofaTracerSpanContext.getSpanId();
                if (parentSpanId != null && StringUtils.isNotBlank(parentSpanId)) {
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
        // adapter SOFARpc span model
        if (sofaTracerSpan.getSofaTracer().getTracerType().equals(SOFARPC_TRACER_TYPE)) {
            //if current span's kind is rpcClient,set localEndpoint by parentSofaTracerSpan's operationName
            if (sofaTracerSpan.isClient()) {
                zipkinSpanBuilder.localEndpoint(getZipkinEndpoint(parentSofaTracerSpan
                    .getOperationName()));
            }
        }
        return zipkinSpanBuilder.build();
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

    /***
     * 功能:将 16 进制字符串转换为:十进制整数
     * @param hexString 16 进制字符串
     * @return 十进制整数
     */
    public static long traceIdToId(String hexString) {
        Assert.hasText(hexString, "Can't convert empty hex string to long");
        int length = hexString.length();
        if (length < 1) {
            throw new IllegalArgumentException("Malformed id(length must be more than zero): "
                                               + hexString);
        }
        if (length <= 8) {
            //hex
            return Long.parseLong(hexString, 16);
        } else if (hexString.endsWith(processId)) {
            //time
            return Long.parseLong(hexString.substring(8, hexString.lastIndexOf(processId)), 10);
        } else {
            //delete ip and processor id
            return Long.parseLong(hexString.substring(8), 10);
        }
    }

}
