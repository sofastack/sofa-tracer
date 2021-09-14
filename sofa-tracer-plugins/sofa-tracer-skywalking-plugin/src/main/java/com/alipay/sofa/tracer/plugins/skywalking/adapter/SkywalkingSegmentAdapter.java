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
package com.alipay.sofa.tracer.plugins.skywalking.adapter;

import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.span.SofaTracerSpanReferenceRelationship;
import com.alipay.common.tracer.core.utils.NetUtils;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.skywalking.model.Segment;
import com.alipay.sofa.tracer.plugins.skywalking.model.Span;
import com.alipay.sofa.tracer.plugins.skywalking.model.SpanType;
import com.alipay.sofa.tracer.plugins.skywalking.model.SpanLayer;
import com.alipay.sofa.tracer.plugins.skywalking.model.SegmentReference;
import com.alipay.sofa.tracer.plugins.skywalking.model.Log;
import com.alipay.sofa.tracer.plugins.skywalking.model.RefType;
import com.alipay.sofa.tracer.plugins.skywalking.utils.ComponentName2ComponentId;
import com.alipay.sofa.tracer.plugins.skywalking.utils.ComponentName2SpanLayer;
import io.opentracing.References;
import io.opentracing.tag.Tags;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SkywalkingSegmentAdapter
 * @author zhaochen
 */

public class SkywalkingSegmentAdapter {
    /**
     * convert sofaTracerSpan to segment in Skywalking
     * @param sofaTracerSpan
     * @return the segment in Skywalking
     */
    public Segment convertToSkywalkingSegment(SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null) {
            return null;
        }
        Segment segment = new Segment();
        segment.setTraceSegmentId(generateSegmentId(sofaTracerSpan));
        segment.setTraceId(sofaTracerSpan.getSofaTracerSpanContext().getTraceId());
        segment.setSizeLimited(false);
        segment.setService(constructServiceName(sofaTracerSpan));
        segment.setServiceInstance(constructServiceInstanceName(sofaTracerSpan));
        segment.addSpan(constructSpan(sofaTracerSpan));
        return segment;
    }

    /**
     * generate segmentId  traceId + FNV64HashCode(SpanId) + 0/1
     * the client and server span generate by dubbo or sofaRpc share the same traceId and spanId,
     * so we need to append 0(server),1(client) to the end of segmentId.
     * @param sofaTracerSpan
     * @return segmentId
     */
    private String generateSegmentId(SofaTracerSpan sofaTracerSpan) {
        String prefix = sofaTracerSpan.getSofaTracerSpanContext().getTraceId()
                        + FNV64HashCode(sofaTracerSpan.getSofaTracerSpanContext().getSpanId());
        // when tracerType equals flexible-biz, span kind is always client
        if (sofaTracerSpan.getSofaTracer().getTracerType().equals(ComponentNameConstants.FLEXIBLE)) {
            return prefix + SofaTracerConstant.SERVER;
        }
        return prefix
               + (sofaTracerSpan.isServer() ? SofaTracerConstant.SERVER : SofaTracerConstant.CLIENT);
    }

    /**
     * construct EntrySpan or ExitSpan determined by the type of sofaTracerSpan
     * @param sofaTracerSpan
     * @return EntrySpan or ExitSpan
     */
    private Span constructSpan(SofaTracerSpan sofaTracerSpan) {
        Span span = new Span();
        // only have one span every segment
        span.setSpanId(0);
        span.setParentSpanId(-1);
        span.setStartTime(sofaTracerSpan.getStartTime());
        span.setEndTime(sofaTracerSpan.getEndTime());
        span.setOperationName(sofaTracerSpan.getOperationName());
        if (sofaTracerSpan.isServer()) {
            span.setSpanType(SpanType.Entry);
        } else {
            span.setSpanType(SpanType.Exit);
        }
        //map tracerType in sofaTracer to SpanLayer in skyWalking
        span.setSpanLayer(ComponentName2SpanLayer.map.get(sofaTracerSpan.getSofaTracer()
            .getTracerType()));

        //map tracerType in sofaTracer to ComponentId in skyWalking
        span.setComponentId(getComponentId(sofaTracerSpan));
        span.setError(sofaTracerSpan.getTagsWithStr().containsKey("error"));
        span.setSkipAnalysis(false);
        span = convertSpanTags(sofaTracerSpan, span);
        convertSpanLogs(sofaTracerSpan, span);
        // if has patentId then need to add segmentReference
        if (!StringUtils.isBlank(sofaTracerSpan.getSofaTracerSpanContext().getParentId())) {
            span = addSegmentReference(sofaTracerSpan, span);
        }
        String remoteHost = sofaTracerSpan.getTagsWithStr().get(CommonSpanTags.REMOTE_HOST);
        String remotePort = sofaTracerSpan.getTagsWithStr().get(CommonSpanTags.REMOTE_PORT);
        // sofaRpc
        String remoteIp = sofaTracerSpan.getTagsWithStr().get("remote.ip");
        // mongodb
        String peerHost = sofaTracerSpan.getTagsWithStr().get(Tags.PEER_HOSTNAME.getKey());
        String peerPort = String.valueOf(sofaTracerSpan.getTagsWithNumber().get(
            Tags.PEER_PORT.getKey()));
        if (remoteHost != null && remotePort != null) {
            span.setPeer(remoteHost + ":" + remotePort);
        }
        if (peerHost != null && peerPort != null) {
            span.setPeer(peerHost + ":" + peerPort);
        }
        // if the span is formed by sofaRPC, we can only get  ip of the server  to generate networkAddressUsedAtPeer
        if (sofaTracerSpan.getSofaTracer().getTracerType().equals(ComponentNameConstants.SOFA_RPC)
            && remoteIp != null) {
            span.setPeer(remoteIp.split(":")[0]);
        }
        return span;
    }

    /**
     * construct serviceName
     * @param sofaTracerSpan
     * @return serviceName
     */
    private String constructServiceName(SofaTracerSpan sofaTracerSpan) {
        return sofaTracerSpan.getTagsWithStr().get(CommonSpanTags.LOCAL_APP);
    }

    /**
     * ServiceInstanceName
     * @param sofaTracerSpan
     * @return instanceName
     */
    private String constructServiceInstanceName(SofaTracerSpan sofaTracerSpan) {
        InetAddress localIpAddress = NetUtils.getLocalAddress();
        return constructServiceName(sofaTracerSpan) + "@" + localIpAddress.getHostAddress();
    }

    /**
     * get  parentSegmentId
     * @param sofaTracerSpan
     * @return parentSegmentId
     */
    private String getParentSegmentId(SofaTracerSpan sofaTracerSpan) {
        String traceId = sofaTracerSpan.getSofaTracerSpanContext().getTraceId();
        //in sofaRPC and Dubbo  server->server is possible
        // if the span is the server span of RPC, then it's parentSegmentId is traceId + FNV64HashCode(spanId) + SofaTracerConstant.CLIENT
        if (sofaTracerSpan.isServer()
            && ComponentName2SpanLayer.map.get(sofaTracerSpan.getSofaTracer().getTracerType())
                .equals(SpanLayer.RPCFramework)) {
            // server and client span share the same traceId and spanId
            return traceId + FNV64HashCode(sofaTracerSpan.getSofaTracerSpanContext().getSpanId())
                   + SofaTracerConstant.CLIENT;
        }
        return traceId
               + FNV64HashCode(sofaTracerSpan.getSofaTracerSpanContext().getParentId())
               + (sofaTracerSpan.isServer() ? SofaTracerConstant.CLIENT : SofaTracerConstant.SERVER);

    }

    /**
     * convert tags
     * @param sofaTracerSpan
     * @param swSpan span in SkyWalking format
     * @return span with tags in SkyWalking format
     */
    private Span convertSpanTags(SofaTracerSpan sofaTracerSpan, Span swSpan) {
        Map<String, Object> tags = new LinkedHashMap<>();
        tags.putAll(sofaTracerSpan.getTagsWithStr());
        tags.putAll(sofaTracerSpan.getTagsWithBool());
        tags.putAll(sofaTracerSpan.getTagsWithNumber());
        for (Map.Entry<String, Object> tag : tags.entrySet()) {
            swSpan.addTag(tag.getKey(), tag.getValue().toString());
        }
        return swSpan;
    }

    /**
     *
     * @param sofaTracerSpan
     * @param swSpan
     * @return span with logs in SkyWalking format
     */
    private Span convertSpanLogs(SofaTracerSpan sofaTracerSpan, Span swSpan) {
        List<LogData> logs = sofaTracerSpan.getLogs();
        for (LogData sofaLog : logs) {
            Log log = new Log();
            log.setTime(sofaLog.getTime());
            //KeyStringValuePair
            for (Map.Entry<String, ?> entry : sofaLog.getFields().entrySet()) {
                log.addLogs(entry.getKey(), entry.getValue().toString());
            }
            swSpan.addLog(log);
        }
        return swSpan;
    }

    /**
     * when sofaTracer Span is not root span we add add reference to point to parent segment
     * @param sofaTracerSpan
     * @param swSpan
     * @return span with segment reference in SkyWalking format
     */
    private Span addSegmentReference(SofaTracerSpan sofaTracerSpan, Span swSpan) {
        SofaTracerSpanContext spanContext = sofaTracerSpan.getSofaTracerSpanContext();
        SegmentReference segmentReference = new SegmentReference();
        //default set to crossProcess
        segmentReference.setRefType(RefType.CrossProcess);
        segmentReference.setTraceId(sofaTracerSpan.getSofaTracerSpanContext().getTraceId());
        segmentReference.setParentTraceSegmentId(getParentSegmentId(sofaTracerSpan));
        //because there is only one span in each segment so parentId is 0
        segmentReference.setParentSpanId(0);
        segmentReference.setParentService(spanContext.getParentService());
        segmentReference.setParentServiceInstance(spanContext.getParentServiceInstance());
        segmentReference.setParentEndpoint(spanContext.getParentOperationName());

        String networkAddressUsedAtPeer = getNetworkAddressUsedAtPeer(sofaTracerSpan);
        if (networkAddressUsedAtPeer != null) {
            segmentReference.setNetworkAddressUsedAtPeer(networkAddressUsedAtPeer);
        }
        swSpan.addSegmentReference(segmentReference);
        return swSpan;
    }

    private String getNetworkAddressUsedAtPeer(SofaTracerSpan sofaTracerSpan) {
        // if is sofaRpc get localIp
        if (sofaTracerSpan.getSofaTracer().getTracerType().equals(ComponentNameConstants.SOFA_RPC)) {
            return NetUtils.getLocalIpv4();
        }
        String tracerType = sofaTracerSpan.getSofaTracer().getTracerType();
        if (tracerType.equals(ComponentNameConstants.DUBBO_SERVER)
            || tracerType.equals(ComponentNameConstants.DUBBO_CLIENT)) {
            Map<String, String> strTags = sofaTracerSpan.getTagsWithStr();
            String host = strTags.get(CommonSpanTags.LOCAL_HOST);
            String port = strTags.get(CommonSpanTags.LOCAL_PORT);
            if (host != null && port != null) {
                return host + ":" + port;
            }
        }
        if (sofaTracerSpan.getSpanReferences().size() >= 1) {
            SofaTracerSpanContext parentSpanContext = preferredReference(sofaTracerSpan
                .getSpanReferences());
            return parentSpanContext.getPeer();
        }
        return null;
    }

    private int getComponentId(SofaTracerSpan sofaTracerSpan) {
        String tracerType = sofaTracerSpan.getSofaTracer().getTracerType();
        final int UNKNOWN = ComponentName2ComponentId.componentName2IDMap.get("UNKNOWN");
        if (StringUtils.isBlank(tracerType)) {
            return UNKNOWN;
        }
        // specific type of database instead of datasource
        if (tracerType.equals(ComponentNameConstants.DATA_SOURCE)) {
            String database = sofaTracerSpan.getTagsWithStr().get("database.type");
            if (StringUtils.isBlank(database)) {
                return UNKNOWN;
            }
            if (ComponentName2ComponentId.componentName2IDMap.containsKey(database)) {
                return ComponentName2ComponentId.componentName2IDMap.get(database);
            }
            return UNKNOWN;
        }
        if (ComponentName2ComponentId.componentName2IDMap.containsKey(tracerType)) {
            return ComponentName2ComponentId.componentName2IDMap.get(tracerType);
        }
        return UNKNOWN;

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

    private SofaTracerSpanContext preferredReference(List<SofaTracerSpanReferenceRelationship> references) {
        SofaTracerSpanReferenceRelationship preferredReference = references.get(0);
        for (SofaTracerSpanReferenceRelationship reference : references) {
            // childOf takes precedence as a preferred parent
            String referencedType = reference.getReferenceType();
            if (References.CHILD_OF.equals(referencedType)
                && !References.CHILD_OF.equals(preferredReference.getReferenceType())) {
                preferredReference = reference;
                break;
            }
        }
        return preferredReference.getSofaTracerSpanContext();
    }

}
