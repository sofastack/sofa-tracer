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
import com.alipay.common.tracer.core.utils.CommonUtils;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.common.tracer.core.utils.TracerUtils;
import com.alipay.sofa.tracer.boot.zipkin.sender.ZipkinRestTemplateSender;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import zipkin.Annotation;
import zipkin.BinaryAnnotation;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ZipkinSofaTracerSpanRemoteReporter report {@link SofaTracerSpan} to Zipkin
 *
 * @author yangguanchao
 * @since 2018/05/01
 */
public class ZipkinSofaTracerSpanRemoteReporter implements SpanReportListener, Flushable, Closeable {

    private static final Charset           UTF_8        = Charset.forName("UTF-8");

    private static String                  processId    = TracerUtils.getPID();

    private final ZipkinRestTemplateSender sender;

    private final AsyncReporter<Span>      delegate;

    /***
     * cache and performance improve
     */
    private int                            ipAddressInt = -1;

    public ZipkinSofaTracerSpanRemoteReporter(RestTemplate restTemplate, String baseUrl,
                                              int flushInterval) {
        this.sender = new ZipkinRestTemplateSender(restTemplate, baseUrl);
        this.delegate = AsyncReporter.builder(this.sender).queuedMaxSpans(1000)
            .messageTimeout(flushInterval, TimeUnit.SECONDS).build();
    }

    @Override
    public void onSpanReport(SofaTracerSpan span) {
        if (span == null) {
            return;
        }
        //convert
        zipkin.Span zipkinSpan = convertToZipkinSpan(span);
        this.delegate.report(zipkinSpan);
    }

    @Override
    public void flush() throws IOException {
        this.delegate.flush();
    }

    @Override
    public void close() {
        this.delegate.close();
        this.sender.close();
    }

    private zipkin.Span convertToZipkinSpan(SofaTracerSpan sofaTracerSpan) {
        zipkin.Span.Builder zipkinSpanBuilder = Span.builder();
        Endpoint endpoint = getZipkinEndpoint(sofaTracerSpan.getOperationName());
        this.addZipkinAnnotations(zipkinSpanBuilder, sofaTracerSpan, endpoint);
        this.addZipkinBinaryAnnotationsWithTags(zipkinSpanBuilder, sofaTracerSpan, endpoint);
        //baggage
        this.addZipkinBinaryAnnotationsWithBaggage(zipkinSpanBuilder, sofaTracerSpan);
        // Zipkin is in nanosecond :timestamp reference : zipkin.storage.QueryRequest.test()
        zipkinSpanBuilder.timestamp(sofaTracerSpan.getStartTime() * 1000);
        // Zipkin is in nanosecond
        zipkinSpanBuilder
            .duration((sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime()) * 1000);
        //traceId
        SofaTracerSpanContext sofaTracerSpanContext = sofaTracerSpan.getSofaTracerSpanContext();

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
        //zipkinSpanBuilder.traceId(traceIdToId(sofaTracerSpanContext.getTraceId()));
        long[] traceIds = CommonUtils.hexToDualLong(sofaTracerSpanContext.getTraceId());
        zipkinSpanBuilder.traceIdHigh(traceIds[0]);
        zipkinSpanBuilder.traceId(traceIds[1]);

        String parentSpanId = sofaTracerSpanContext.getParentId();
        if (sofaTracerSpan.isServer() && parentSpanId != null
            && StringUtils.isNotBlank(parentSpanId)) {
            if (CommonUtils.isHexString(parentSpanId)) {
                zipkinSpanBuilder.parentId(CommonUtils.hexToLong(parentSpanId));
            } else {
                zipkinSpanBuilder.parentId(spanIdToLong(parentSpanId));
            }
        } else if (sofaTracerSpan.getParentSofaTracerSpan() != null) {
            SofaTracerSpanContext parentSofaTracerSpanContext = sofaTracerSpan
                .getParentSofaTracerSpan().getSofaTracerSpanContext();
            if (parentSofaTracerSpanContext != null) {
                parentSpanId = parentSofaTracerSpanContext.getSpanId();
                if (parentSpanId != null && StringUtils.isNotBlank(parentSpanId)) {
                    if (CommonUtils.isHexString(parentSpanId)) {
                        zipkinSpanBuilder.parentId(CommonUtils.hexToLong(parentSpanId));
                    } else {
                        zipkinSpanBuilder.parentId(spanIdToLong(parentSpanId));
                    }
                }
            }

        }

        //spanId
        String spanId = sofaTracerSpanContext.getSpanId();
        zipkinSpanBuilder.id(spanIdToLong(spanId));
        //name
        String operationName = sofaTracerSpan.getOperationName();
        if (StringUtils.isNotBlank(operationName)) {
            zipkinSpanBuilder.name(operationName);
        } else {
            zipkinSpanBuilder.name(StringUtils.EMPTY_STRING);
        }
        return zipkinSpanBuilder.build();
    }

    private Endpoint getZipkinEndpoint(String operationName) {
        if (this.ipAddressInt <= 0) {
            InetAddress ipAddress = null;
            try {
                ipAddress = InetAddress.getLocalHost();
                this.ipAddressInt = ByteBuffer.wrap(ipAddress.getAddress()).getInt();
            } catch (UnknownHostException e) {
                //127.0.0.1 256 进制
                this.ipAddressInt = 256 * 256 * 256 * 127 + 1;
            }
        }
        return Endpoint.builder().serviceName(operationName).ipv4(this.ipAddressInt).build();
    }

    private void addZipkinBinaryAnnotation(String key, String value, Endpoint endpoint,
                                           zipkin.Span.Builder zipkinSpan) {
        BinaryAnnotation binaryAnn = BinaryAnnotation.builder().type(BinaryAnnotation.Type.STRING)
            .key(key).value(value.getBytes(UTF_8)).endpoint(endpoint).build();
        zipkinSpan.addBinaryAnnotation(binaryAnn);
    }

    /**
     * Adds binary annotation from the Open Tracing Span
     */
    private void addZipkinBinaryAnnotationsWithTags(zipkin.Span.Builder zipkinSpan,
                                                    SofaTracerSpan span, Endpoint endpoint) {
        for (Map.Entry<String, String> e : span.getTagsWithStr().entrySet()) {
            addZipkinBinaryAnnotation(e.getKey(), e.getValue(), endpoint, zipkinSpan);
        }
        for (Map.Entry<String, Number> e : span.getTagsWithNumber().entrySet()) {
            addZipkinBinaryAnnotation(e.getKey(), e.getValue().toString(), endpoint, zipkinSpan);
        }
        for (Map.Entry<String, Boolean> e : span.getTagsWithBool().entrySet()) {
            addZipkinBinaryAnnotation(e.getKey(), e.getValue().toString(), endpoint, zipkinSpan);
        }
    }

    private void addZipkinBinaryAnnotationsWithBaggage(zipkin.Span.Builder zipkinSpan,
                                                       SofaTracerSpan span) {
        SofaTracerSpanContext sofaTracerSpanContext = span.getSofaTracerSpanContext();
        if (sofaTracerSpanContext != null) {
            Map<String, String> sysBaggage = sofaTracerSpanContext.getSysBaggage();
            for (Map.Entry<String, String> e : sysBaggage.entrySet()) {
                addZipkinBinaryAnnotation(e.getKey(), e.getValue(), null, zipkinSpan);
            }
            Map<String, String> bizBaggage = sofaTracerSpanContext.getBizBaggage();
            for (Map.Entry<String, String> e : bizBaggage.entrySet()) {
                addZipkinBinaryAnnotation(e.getKey(), e.getValue(), null, zipkinSpan);
            }
        }
    }

    private void addZipkinAnnotations(zipkin.Span.Builder zipkinSpan, SofaTracerSpan span,
                                      Endpoint endpoint) {
        for (LogData logData : span.getLogs()) {
            Map<String, ?> fields = logData.getFields();
            if (fields == null || fields.size() <= 0) {
                continue;
            }
            for (Map.Entry<String, ?> entry : fields.entrySet()) {
                //ignore event key
                Annotation zipkinAnnotation = Annotation.builder()
                    // Zipkin is in nanosecond
                    .timestamp(logData.getTime() * 1000).value(entry.getValue().toString())
                    .endpoint(endpoint).build();
                zipkinSpan.addAnnotation(zipkinAnnotation);
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
