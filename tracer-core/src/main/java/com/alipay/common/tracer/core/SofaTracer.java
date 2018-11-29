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
package com.alipay.common.tracer.core;

import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.generator.TraceIdGenerator;
import com.alipay.common.tracer.core.listener.SpanReportListener;
import com.alipay.common.tracer.core.listener.SpanReportListenerHolder;
import com.alipay.common.tracer.core.registry.RegistryExtractorInjector;
import com.alipay.common.tracer.core.registry.TracerFormatRegistry;
import com.alipay.common.tracer.core.reporter.facade.Reporter;
import com.alipay.common.tracer.core.samplers.Sampler;
import com.alipay.common.tracer.core.samplers.SamplerFactory;
import com.alipay.common.tracer.core.samplers.SamplingStatus;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.span.SofaTracerSpanReferenceRelationship;
import com.alipay.common.tracer.core.utils.AssertUtils;
import com.alipay.common.tracer.core.utils.StringUtils;
import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SofaTracer
 *
 * @author yangguanchao
 * @since 2017/06/17
 */
public class SofaTracer implements Tracer {

    /**
     * 正常 TRACE 开始的 spanId
     */
    public static final String        ROOT_SPAN_ID = "0";

    /***
     * 标示 tracer 的类型
     */
    private final String              tracerType;

    /***
     * 作为客户端运行时的 Reporter
     */
    private final Reporter            clientReporter;

    /***
     * 作为服务端运行时的 Reporter
     */
    private final Reporter            serverReporter;

    /***
     * 这个 tracerTags 主要用于缓存和 tracer 全局相关的一些信息
     */
    private final Map<String, Object> tracerTags   = new ConcurrentHashMap<String, Object>();

    /**
     * 支持 sampler 即根据 rate 采样（主要在入口起作用）
     */
    private final Sampler             sampler;

    private SofaTracer(String tracerType, Reporter clientReporter, Reporter serverReporter,
                       Sampler sampler, Map<String, Object> tracerTags) {
        this.tracerType = tracerType;
        this.clientReporter = clientReporter;
        this.serverReporter = serverReporter;
        this.sampler = sampler;
        if (tracerTags != null && tracerTags.size() > 0) {
            this.tracerTags.putAll(tracerTags);
        }
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return new SofaTracerSpanBuilder(operationName);
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        RegistryExtractorInjector<C> registryInjector = TracerFormatRegistry.getRegistry(format);
        if (registryInjector == null) {
            throw new IllegalArgumentException("Unsupported injector format: " + format);
        }
        registryInjector.inject((SofaTracerSpanContext) spanContext, carrier);
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {

        RegistryExtractorInjector<C> registryExtractor = TracerFormatRegistry.getRegistry(format);
        if (registryExtractor == null) {
            throw new IllegalArgumentException("Unsupported extractor format: " + format);
        }
        return registryExtractor.extract(carrier);
    }

    public void reportSpan(SofaTracerSpan span) {
        if (span == null) {
            return;
        }
        // //sampler is support &  current span is root span
        if (sampler != null && span.getParentSofaTracerSpan() == null) {
            span.getSofaTracerSpanContext().setSampled(sampler.sample(span).isSampled());
        }
        //invoke listener
        this.invokeReportListeners(span);
        //客户端、服务端
        if (span.isClient()) {
            //摘要
            if (this.clientReporter != null) {
                this.clientReporter.report(span);
            }
        } else if (span.isServer()) {
            //摘要
            if (this.serverReporter != null) {
                this.serverReporter.report(span);
            }
        } else {
            //ignore 不统计
            SelfLog.warn("Span reported neither client nor server.Ignore!");
        }
    }

    /**
     * Shuts down the {@link Reporter}  and {@link Sampler}
     */
    public void close() {
        if (this.clientReporter != null) {
            this.clientReporter.close();
        }
        if (this.serverReporter != null) {
            this.serverReporter.close();
        }

        if (sampler != null) {
            this.sampler.close();
        }
    }

    public String getTracerType() {
        return tracerType;
    }

    public Reporter getClientReporter() {
        return clientReporter;
    }

    public Reporter getServerReporter() {
        return serverReporter;
    }

    public Sampler getSampler() {
        return sampler;
    }

    public Map<String, Object> getTracerTags() {
        return tracerTags;
    }

    @Override
    public String toString() {
        return "SofaTracer{" + "tracerType='" + tracerType + '}';
    }

    private void invokeReportListeners(SofaTracerSpan sofaTracerSpan) {
        List<SpanReportListener> listeners = SpanReportListenerHolder
            .getSpanReportListenersHolder();
        if (listeners != null && listeners.size() > 0) {
            for (SpanReportListener listener : listeners) {
                listener.onSpanReport(sofaTracerSpan);
            }
        }
    }

    /***
     * SofaTracerSpanBuilder 用于在 Tracer 内部构建 Span
     */
    public class SofaTracerSpanBuilder implements io.opentracing.Tracer.SpanBuilder {

        private String                                    operationName = StringUtils.EMPTY_STRING;

        /***
         * 默认初始化时间
         */
        private long                                      startTime     = -1;

        /**
         * In 99% situations there is only one parent (childOf), so we do not want to allocate
         * a collection of references.
         */
        private List<SofaTracerSpanReferenceRelationship> references    = Collections.emptyList();

        private final Map<String, Object>                 tags          = new HashMap<String, Object>();

        public SofaTracerSpanBuilder(String operationName) {
            this.operationName = operationName;
        }

        @Override
        public Tracer.SpanBuilder asChildOf(SpanContext parent) {
            return addReference(References.CHILD_OF, parent);
        }

        @Override
        public Tracer.SpanBuilder asChildOf(Span parentSpan) {
            if (parentSpan == null) {
                return this;
            }
            return addReference(References.CHILD_OF, parentSpan.context());
        }

        @Override
        public Tracer.SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
            if (referencedContext == null) {
                return this;
            }
            if (!(referencedContext instanceof SofaTracerSpanContext)) {
                return this;
            }
            if (!References.CHILD_OF.equals(referenceType)
                && !References.FOLLOWS_FROM.equals(referenceType)) {
                return this;
            }
            if (references.isEmpty()) {
                // Optimization for 99% situations, when there is only one parent
                references = Collections.singletonList(new SofaTracerSpanReferenceRelationship(
                    (SofaTracerSpanContext) referencedContext, referenceType));
            } else {
                if (references.size() == 1) {
                    //要保证有顺序
                    references = new ArrayList<SofaTracerSpanReferenceRelationship>(references);
                }
                references.add(new SofaTracerSpanReferenceRelationship(
                    (SofaTracerSpanContext) referencedContext, referenceType));
            }
            return this;
        }

        @Override
        public Tracer.SpanBuilder withTag(String key, String value) {
            this.tags.put(key, value);
            return this;
        }

        @Override
        public Tracer.SpanBuilder withTag(String key, boolean value) {
            this.tags.put(key, value);
            return this;
        }

        @Override
        public Tracer.SpanBuilder withTag(String key, Number value) {
            this.tags.put(key, value);
            return this;
        }

        @Override
        public Tracer.SpanBuilder withStartTimestamp(long microseconds) {
            this.startTime = microseconds;
            return this;
        }

        @Override
        public Span start() {
            SofaTracerSpanContext sofaTracerSpanContext = null;
            if (this.references != null && this.references.size() > 0) {
                //存在父上下文
                sofaTracerSpanContext = this.createChildContext();
            } else {
                //从新开始新的节点
                sofaTracerSpanContext = this.createRootSpanContext();
            }

            long begin = this.startTime > 0 ? this.startTime : System.currentTimeMillis();
            SofaTracerSpan sofaTracerSpan = new SofaTracerSpan(SofaTracer.this, begin,
                this.references, this.operationName, sofaTracerSpanContext, this.tags);

            // calculate isSampled，but do not change parent's sampler behaviour
            boolean isSampled = calculateSampler(sofaTracerSpan);
            sofaTracerSpanContext.setSampled(isSampled);

            return sofaTracerSpan;
        }

        private boolean calculateSampler(SofaTracerSpan sofaTracerSpan) {
            boolean isSampled = false;
            if (this.references != null && this.references.size() > 0) {
                SofaTracerSpanContext preferredReference = preferredReference();
                isSampled = preferredReference.isSampled();
            } else {
                if (sampler != null) {
                    SamplingStatus samplingStatus = sampler.sample(sofaTracerSpan);
                    if (samplingStatus.isSampled()) {
                        isSampled = true;
                        //发生采样后,将相关属性记录
                        this.tags.putAll(samplingStatus.getTags());
                    }
                }
            }

            return isSampled;
        }

        private SofaTracerSpanContext createRootSpanContext() {
            //生成 traceId
            String traceId = TraceIdGenerator.generate();
            return new SofaTracerSpanContext(traceId, ROOT_SPAN_ID, StringUtils.EMPTY_STRING);
        }

        private SofaTracerSpanContext createChildContext() {
            SofaTracerSpanContext preferredReference = preferredReference();

            SofaTracerSpanContext sofaTracerSpanContext = new SofaTracerSpanContext(
                preferredReference.getTraceId(), preferredReference.nextChildContextId(),
                preferredReference.getSpanId(), preferredReference.isSampled());
            sofaTracerSpanContext.addBizBaggage(this.createChildBaggage(true));
            sofaTracerSpanContext.addSysBaggage(this.createChildBaggage(false));
            return sofaTracerSpanContext;
        }

        private Map<String, String> createChildBaggage(boolean isBiz) {
            // optimization for 99% use cases, when there is only one parent
            if (references.size() == 1) {
                if (isBiz) {
                    return references.get(0).getSofaTracerSpanContext().getBizBaggage();
                } else {
                    return references.get(0).getSofaTracerSpanContext().getSysBaggage();
                }
            }
            Map<String, String> baggage = null;
            for (SofaTracerSpanReferenceRelationship reference : references) {
                Map<String, String> referenceBaggage = null;
                if (isBiz) {
                    referenceBaggage = reference.getSofaTracerSpanContext().getBizBaggage();
                } else {
                    referenceBaggage = reference.getSofaTracerSpanContext().getSysBaggage();
                }
                if (referenceBaggage != null && referenceBaggage.size() > 0) {
                    if (baggage == null) {
                        baggage = new HashMap<String, String>();
                    }
                    baggage.putAll(referenceBaggage);
                }
            }
            return baggage;
        }

        private SofaTracerSpanContext preferredReference() {
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

    public static final class Builder {

        private final String        tracerType;

        /***
         * 作为客户端运行时的 Reporter
         */
        private Reporter            clientReporter;

        /***
         * 作为服务端运行时的 Reporter
         */
        private Reporter            serverReporter;

        private Map<String, Object> tracerTags = new HashMap<String, Object>();

        private Sampler             sampler;

        public Builder(String tracerType) {
            AssertUtils.isTrue(StringUtils.isNotBlank(tracerType), "tracerType must be not empty");
            this.tracerType = tracerType;
        }

        /***
         * 客户端日志功能
         * @param clientReporter 日志功能,落地到磁盘或者上报 zipkin
         * @return Builder
         */
        public Builder withClientReporter(Reporter clientReporter) {
            this.clientReporter = clientReporter;
            return this;
        }

        /***
         * 服务端日志功能
         * @param serverReporter 日志功能,落地到磁盘或者上报 zipkin
         * @return Builder
         */
        public Builder withServerReporter(Reporter serverReporter) {
            this.serverReporter = serverReporter;
            return this;
        }

        /***
         * 采样器入口 span 生效
         * @param sampler 采样器
         * @return Builder
         */
        public Builder withSampler(Sampler sampler) {
            this.sampler = sampler;
            return this;
        }

        public Builder withTag(String key, String value) {
            tracerTags.put(key, value);
            return this;
        }

        public Builder withTag(String key, Boolean value) {
            tracerTags.put(key, value);
            return this;
        }

        public Builder withTag(String key, Number value) {
            tracerTags.put(key, value);
            return this;
        }

        public Builder withTags(Map<String, ?> tags) {
            if (tags == null || tags.size() <= 0) {
                return this;
            }
            for (Map.Entry<String, ?> entry : tags.entrySet()) {
                String key = entry.getKey();
                if (StringUtils.isBlank(key)) {
                    continue;
                }
                Object value = entry.getValue();
                if (value == null) {
                    continue;
                }
                if (value instanceof String) {
                    this.withTag(key, (String) value);
                } else if (value instanceof Boolean) {
                    this.withTag(key, (Boolean) value);
                } else if (value instanceof Number) {
                    this.withTag(key, (Number) value);
                } else {
                    SelfLog.error("Tracer tags unsupported type [" + value.getClass() + "]");
                }
            }
            return this;
        }

        public SofaTracer build() {
            try {
                sampler = SamplerFactory.getSampler();
            } catch (Exception e) {
                SelfLog.error("Failed to get tracer sampler strategy;");
            }
            return new SofaTracer(this.tracerType, this.clientReporter, this.serverReporter,
                this.sampler, this.tracerTags);
        }
    }
}
