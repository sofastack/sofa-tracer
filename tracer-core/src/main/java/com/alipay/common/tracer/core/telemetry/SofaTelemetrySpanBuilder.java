package com.alipay.common.tracer.core.telemetry;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentracing.tag.Tags;

import java.util.concurrent.TimeUnit;

public class SofaTelemetrySpanBuilder implements SpanBuilder {
    private final String name;
    private SofaTelemetrySpanContext sofaTelemetrySpanContext;
    private SofaTelemetrySpan parentSpan;
    SofaTracer sofaTracer;
    SofaTracer.SofaTracerSpanBuilder builder;
    SofaTracer.SofaTracerSpanBuilder sofaTracerSpanBuilder;

    public SofaTelemetrySpanBuilder(String name, SofaTracer sofaTracer){
        this.name = name;
        this.sofaTracer = sofaTracer;
        this.builder = (SofaTracer.SofaTracerSpanBuilder) this.sofaTracer.buildSpan(name);
    }

    @Override
    public SpanBuilder setParent(Context context) {
        SofaTelemetrySpan span = (SofaTelemetrySpan) Span.fromContext(context);
        this.builder.asChildOf(span.getSpan());
        return this;
    }

    @Override
    public SpanBuilder setNoParent() {
        builder.ignoreActiveSpan();
        return this;
    }

    @Override
    public SpanBuilder addLink(SpanContext spanContext) {
        //添加关系link相当于所有的父亲span
        SofaTelemetrySpanContext context = TelemetryUtils.getContextShim(spanContext);
        builder.addReference("follows_from", context.getSpanContext());
        return this;
    }

    @Override
    public SpanBuilder addLink(SpanContext spanContext, Attributes attributes) {
        return addLink(spanContext);
    }

    @Override
    public SpanBuilder setAttribute(String s, String s1) {
        //attributes对应的tag
        builder.withTag(s,s1);
        return null;
    }

    @Override
    public SpanBuilder setAttribute(String s, long l) {
        builder.withTag(s,l);
        return null;
    }

    @Override
    public SpanBuilder setAttribute(String s, double v) {
        builder.withTag(s,v);
        return null;
    }

    @Override
    public SpanBuilder setAttribute(String s, boolean b) {
        builder.withTag(s,b);
        return null;
    }

    @Override
    public <T> SpanBuilder setAttribute(AttributeKey<T> attributeKey, T value) {
        if (attributeKey.getKey().isEmpty()) {
            return this;
        }
        AttributeType type = attributeKey.getType();
        if(type == AttributeType.BOOLEAN){
            builder.withTag(attributeKey.getKey(), (Boolean) value);
        }else if(type == AttributeType.DOUBLE || type == AttributeType.LONG){
            builder.withTag(attributeKey.getKey(), (Number) value);
        }else{
            builder.withTag(attributeKey.getKey(), value.toString());
        }
        return this;
    }

    @Override
    public SpanBuilder setSpanKind(SpanKind spanKind) {
        if(spanKind == SpanKind.CLIENT || spanKind == SpanKind.CONSUMER){
            builder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
        }else if(spanKind == SpanKind.SERVER || spanKind == SpanKind.PRODUCER){
            builder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
        }else{
        }
        return this;
    }

    @Override
    public SpanBuilder setStartTimestamp(long timestamp, TimeUnit timeUnit) {
        timestamp =  timeUnit.convert(timestamp, TimeUnit.MILLISECONDS);
        sofaTracerSpanBuilder.withStartTimestamp(timestamp);
        return this;
    }

    @Override
    public Span startSpan() {
        SofaTracerSpan sofaTracerSpan = (SofaTracerSpan) builder.start();
        return new SofaTelemetrySpan(sofaTracerSpan, sofaTracer);
    }
}
