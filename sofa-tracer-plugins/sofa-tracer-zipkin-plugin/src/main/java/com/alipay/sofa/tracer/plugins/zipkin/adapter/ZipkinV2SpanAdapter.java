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
package com.alipay.sofa.tracer.plugins.zipkin.adapter;

import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.NetUtils;
import com.alipay.common.tracer.core.utils.StringUtils;
import io.opentracing.tag.Tags;
import zipkin2.Endpoint;
import zipkin2.Span;

import java.net.InetAddress;
import java.util.Map;

/***
 * ZipkinV2SpanAdapter : convent sofaTracer span model to zipkin span model
 * @author guolei.sgl 05/09/2018
 * @since v.2.3.0
 */
public class ZipkinV2SpanAdapter {

    /**
     * cache and performance improve
     */
    private InetAddress localIpAddress = null;

    /**
     * convent sofaTracerSpan model to zipKinSpan model
     *
     * @param sofaTracerSpan original span
     * @return zipkinSpan model
     */
    public Span convertToZipkinSpan(SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null) {
            return null;
        }
        // spanId、parentId、tracerId
        Span.Builder zipkinSpanBuilder = Span.newBuilder();
        SofaTracerSpanContext context = sofaTracerSpan.getSofaTracerSpanContext();
        zipkinSpanBuilder.traceId(getValidTraceId(context));
        zipkinSpanBuilder.id(spanIdToLong(context.getSpanId()));
        if (StringUtils.isNotBlank(context.getParentId())) {
            zipkinSpanBuilder.parentId(spanIdToLong(context.getParentId()));
        }

        // timestamp & duration
        long start = sofaTracerSpan.getStartTime() * 1000;
        long finish = sofaTracerSpan.getEndTime() * 1000;
        zipkinSpanBuilder.timestamp(start);
        zipkinSpanBuilder.duration(finish - start);

        // kind
        Map<String, String> tagsWithStr = sofaTracerSpan.getTagsWithStr();
        String kindStr = tagsWithStr.get(Tags.SPAN_KIND.getKey());
        if (StringUtils.isNotBlank(kindStr) && kindStr.equals(Tags.SPAN_KIND_SERVER)) {
            zipkinSpanBuilder.kind(Span.Kind.SERVER);
        } else {
            zipkinSpanBuilder.kind(Span.Kind.CLIENT);
        }

        // Endpoint
        Endpoint endpoint = getZipkinEndpoint(sofaTracerSpan);
        zipkinSpanBuilder.localEndpoint(endpoint);

        // Tags
        this.addZipkinTags(zipkinSpanBuilder, sofaTracerSpan);

        // span name
        String operationName = sofaTracerSpan.getOperationName();
        if (StringUtils.isNotBlank(operationName)) {
            zipkinSpanBuilder.name(operationName);
        } else {
            zipkinSpanBuilder.name(StringUtils.EMPTY_STRING);

            if (StringUtils.isNotBlank(sofaTracerSpan.getTagsWithStr().get("message.event.code"))) {
                // set messageTopic:messageEventCode as name
                String name = sofaTracerSpan.getTagsWithStr().get("message.topic") + ":"
                              + sofaTracerSpan.getTagsWithStr().get("message.event.code");
                zipkinSpanBuilder.name(name);
            } else if (StringUtils.isNotBlank(sofaTracerSpan.getTagsWithStr().get("table.name"))) {
                // set tableName as name
                zipkinSpanBuilder.name(sofaTracerSpan.getTagsWithStr().get("table.name"));
            } else if (StringUtils.isNotBlank(sofaTracerSpan.getTagsWithStr()
                .get("datasource.name"))) {
                // set datasourceName as name
                zipkinSpanBuilder.name(sofaTracerSpan.getTagsWithStr().get("datasource.name"));
            }
        }

        // Annotations
        this.addZipkinAnnotations(zipkinSpanBuilder, sofaTracerSpan);

        return zipkinSpanBuilder.build();
    }

    private String getValidTraceId(SofaTracerSpanContext context) {
        String traceId = context.getTraceId();
        if (traceId.endsWith("T")) {
            traceId = traceId.substring(0, traceId.length() - 1);
        }
        return traceId;
    }

    public static long spanIdToLong(String spanId) {
        return FNV64HashCode(spanId);
    }

    /**
     * from http://en.wikipedia.org/wiki/Fowler_Noll_Vo_hash
     *
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

    private Endpoint getZipkinEndpoint(SofaTracerSpan span) {
        if (localIpAddress == null) {
            localIpAddress = NetUtils.getLocalAddress();
        }
        String appName = span.getTagsWithStr().get(CommonSpanTags.LOCAL_APP);
        return Endpoint.newBuilder().serviceName(appName).ip(localIpAddress).build();
    }

    /**
     * Put the baggage data into the tags
     *
     * @param zipkinSpan
     * @param span
     */
    private void addZipkinTagsWithBaggage(Span.Builder zipkinSpan, SofaTracerSpan span) {
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

    /**
     * convent Annotations
     *
     * @param zipkinSpan
     * @param span
     */
    private void addZipkinAnnotations(Span.Builder zipkinSpan, SofaTracerSpan span) {
        for (LogData logData : span.getLogs()) {
            Map<String, ?> fields = logData.getFields();
            if (fields == null || fields.size() <= 0) {
                continue;
            }
            for (Map.Entry<String, ?> entry : fields.entrySet()) {
                // zipkin has been support default log event depend on span kind & serviceName
                if (!(entry.getValue().toString().equals(LogData.CLIENT_RECV_EVENT_VALUE)
                      || entry.getValue().toString().equals(LogData.CLIENT_SEND_EVENT_VALUE)
                      || entry.getValue().toString().equals(LogData.SERVER_RECV_EVENT_VALUE) || entry
                    .getValue().toString().equals(LogData.SERVER_SEND_EVENT_VALUE))) {
                    zipkinSpan.addAnnotation(logData.getTime() * 1000, entry.getValue().toString());
                }
            }
        }
    }

    /**
     * convent tags
     *
     * @param zipkinSpan
     * @param span
     */
    private void addZipkinTags(Span.Builder zipkinSpan, SofaTracerSpan span) {

        for (Map.Entry<String, String> e : span.getTagsWithStr().entrySet()) {
            zipkinSpan.putTag(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, Number> e : span.getTagsWithNumber().entrySet()) {
            zipkinSpan.putTag(e.getKey(), e.getValue().toString());
        }
        for (Map.Entry<String, Boolean> e : span.getTagsWithBool().entrySet()) {
            zipkinSpan.putTag(e.getKey(), e.getValue().toString());
        }

        SofaTracerSpanContext context = span.getSofaTracerSpanContext();
        zipkinSpan.putTag("origin.span.id", context.getSpanId());
        zipkinSpan.putTag("origin.parent.span.id", context.getParentId());

        addZipkinTagsWithBaggage(zipkinSpan, span);
    }
}
