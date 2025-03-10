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
import com.alipay.common.tracer.core.constants.ComponentNameConstants;
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
import com.alipay.sofa.common.code.LogCode2Description;
import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.alipay.common.tracer.core.constants.SofaTracerConstant.SPACE_ID;

/**
 * SofaTracer
 *
 * @author yangguanchao
 * @since 2017/06/17
 */
public class SofaTracer implements Tracer {

    /**
     * normal root spanId's default value
     */
    public static final String        ROOT_SPAN_ID = "0";

    /**
     * Mark the type of tracer
     */
    private final String              tracerType;

    /**
     * Reporter as a client runtime
     */
    private final Reporter            clientReporter;

    /**
     * Reporter as a server runtime
     */
    private final Reporter            serverReporter;

    private final Reporter            clientEventReporter;

    private final Reporter            serverEventReporter;

    /**
     * Cache some information related to the tracer globally
     */
    private final Map<String, Object> tracerTags   = new ConcurrentHashMap<>();

    /**
     * Sampler instance
     */
    private final Sampler             sampler;

    protected SofaTracer(String tracerType, Reporter clientReporter, Reporter serverReporter,
                         Sampler sampler, Map<String, Object> tracerTags) {
        this.tracerType = tracerType;
        this.clientReporter = clientReporter;
        this.serverReporter = serverReporter;
        this.clientEventReporter = null;
        this.serverEventReporter = null;
        this.sampler = sampler;
        if (tracerTags != null && tracerTags.size() > 0) {
            this.tracerTags.putAll(tracerTags);
        }
    }

    protected SofaTracer(String tracerType, Sampler sampler) {
        this.tracerType = tracerType;
        this.clientReporter = null;
        this.serverReporter = null;
        this.clientEventReporter = null;
        this.serverEventReporter = null;
        this.sampler = sampler;
    }

    protected SofaTracer(String tracerType, Reporter clientReporter, Reporter serverReporter,
                         Reporter clientEventReporter, Reporter serverEventReporter, Sampler sampler, Map<String, Object> tracerTags) {
        this.tracerType = tracerType;
        this.clientReporter = clientReporter;
        this.serverReporter = serverReporter;
        this.clientEventReporter = clientEventReporter;
        this.serverEventReporter = serverEventReporter;
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
        if (sampler != null && (span.isClient() && span.getParentSofaTracerSpan() == null)) {
            span.getSofaTracerSpanContext().setSampled(sampler.sample(span).isSampled());
        }
        //invoke listener
        this.invokeReportListeners(span);
        if (span.isClient()
            || this.getTracerType().equalsIgnoreCase(ComponentNameConstants.FLEXIBLE)) {
            if (this.clientReporter != null) {
                this.clientReporter.report(span);
            }
        } else if (span.isServer()) {
            if (this.serverReporter != null) {
                this.serverReporter.report(span);
            }
        } else {
            //ignore ,do not statical
            SelfLog.warn("Span reported neither client nor server.Ignore!");
        }
    }

    public void reportEvent(SofaTracerSpan span) {
        if (span == null) {
            return;
        }
        // //sampler is support &  current span is root span
        if (sampler != null && (span.isClient() && span.getParentSofaTracerSpan() == null)) {
            span.getSofaTracerSpanContext().setSampled(sampler.sample(span).isSampled());
        }
        //invoke listener
        this.invokeReportListeners(span);
        if (span.isClient()
                || this.getTracerType().equalsIgnoreCase(ComponentNameConstants.FLEXIBLE)) {
            if (this.clientEventReporter != null) {
                this.clientEventReporter.report(span);
            }
        } else if (span.isServer()) {
            if (this.serverEventReporter != null) {
                this.serverEventReporter.report(span);
            }
        } else {
            //ignore ,do not statical
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

        if (this.clientEventReporter != null) {
            this.clientEventReporter.close();
        }

        if (this.serverEventReporter != null) {
            this.serverEventReporter.close();
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

    public Reporter getClientEventReporter() {
        return clientEventReporter;
    }

    public Reporter getServerEventReporter() {
        return serverEventReporter;
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

    protected void invokeReportListeners(SofaTracerSpan sofaTracerSpan) {
        List<SpanReportListener> listeners = SpanReportListenerHolder
            .getSpanReportListenersHolder();
        if (listeners != null && listeners.size() > 0) {
            for (SpanReportListener listener : listeners) {
                listener.onSpanReport(sofaTracerSpan);
            }
        }
    }

    /**
     * SofaTracerSpanBuilder is used to build Span inside Tracer
     */
    public class SofaTracerSpanBuilder implements io.opentracing.Tracer.SpanBuilder {

        private String                                    operationName;

        /**
         * Default initialization time
         */
        private long                                      startTime  = -1;

        /**
         * In 99% situations there is only one parent (childOf), so we do not want to allocate
         * a collection of references.
         */
        private List<SofaTracerSpanReferenceRelationship> references = Collections.emptyList();

        private final Map<String, Object>                 tags       = new HashMap<>();

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
                    //To ensure order
                    references = new ArrayList<>(references);
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
            SofaTracerSpanContext sofaTracerSpanContext;
            if (this.references != null && this.references.size() > 0) {
                //Parent context exist
                sofaTracerSpanContext = this.createChildContext();
            } else {
                //Start with new root span context
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
                        //After the sampling occurs, the related attribute records
                        this.tags.putAll(samplingStatus.getTags());
                    }
                }
            }

            return isSampled;
        }

        private SofaTracerSpanContext createRootSpanContext() {
            //generate traceId
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
                Map<String, String> referenceBaggage;
                if (isBiz) {
                    referenceBaggage = reference.getSofaTracerSpanContext().getBizBaggage();
                } else {
                    referenceBaggage = reference.getSofaTracerSpanContext().getSysBaggage();
                }
                if (referenceBaggage != null && referenceBaggage.size() > 0) {
                    if (baggage == null) {
                        baggage = new HashMap<>();
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

        private Reporter            clientReporter;

        private Reporter            serverReporter;

        private Reporter            clientEventReporter;

        private Reporter            serverEventReporter;

        private Map<String, Object> tracerTags = new HashMap<String, Object>();

        private Sampler             sampler;

        public Builder(String tracerType) {
            AssertUtils.isTrue(StringUtils.isNotBlank(tracerType), "tracerType must be not empty");
            this.tracerType = tracerType;
        }

        public Builder withClientReporter(Reporter clientReporter) {
            this.clientReporter = clientReporter;
            return this;
        }

        public Builder withServerReporter(Reporter serverReporter) {
            this.serverReporter = serverReporter;
            return this;
        }

        public Builder withClientEventReporter(Reporter clientEventReporter) {
            this.clientEventReporter = clientEventReporter;
            return this;
        }

        public Builder withServerEventReporter(Reporter serverEventReporter) {
            this.serverEventReporter = serverEventReporter;
            return this;
        }

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
                    SelfLog.error(String.format(LogCode2Description.convert(SPACE_ID, "01-00003"),
                        value.getClass().toString()));
                }
            }
            return this;
        }

        public SofaTracer build() {
            try {
                sampler = SamplerFactory.getSampler();
            } catch (Exception e) {
                SelfLog.error(LogCode2Description.convert(SPACE_ID, "01-00002"));
            }
            return new SofaTracer(this.tracerType, this.clientReporter, this.serverReporter, this.clientEventReporter,
                this.serverEventReporter, this.sampler, this.tracerTags);
        }
    }
}
