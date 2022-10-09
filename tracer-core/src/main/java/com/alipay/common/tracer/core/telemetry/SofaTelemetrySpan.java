package com.alipay.common.tracer.core.telemetry;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.AssertUtils;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ImplicitContextKeyed;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;
import io.opentracing.util.ThreadLocalScope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class SofaTelemetrySpan implements Span {
    private String name;

    private final SofaTracerSpan sofaTracerSpan;
    private final SofaTracer sofaTracer;
    //private final SofaTracerSpanContext sofaTracerParentSpanContext;
    private StatusData status = StatusData.unset();

    //private final List<LinkData> links;
    private final SofaTelemetrySpanContext spanContext;
    boolean hasEnded;

    public SofaTelemetrySpan(SofaTracerSpan sofaTracerSpan, SofaTracer sofaTracer){
        this.sofaTracerSpan = sofaTracerSpan;
        this.sofaTracer = sofaTracer;
        this.spanContext = new SofaTelemetrySpanContext( sofaTracerSpan.getSofaTracerSpanContext());
    }
    public SofaTracerSpan getSpan(){
        return this.sofaTracerSpan;
    }


    private static final Logger logger = Logger.getLogger(SofaTelemetrySpan.class.getName());


    @Override
    public <T> Span setAttribute(AttributeKey<T> key, T value) {
        if (key.getKey().isEmpty()) {
            return this;
        }
        AssertUtils.isTrue(hasEnded, "Calling setAttribute() on an ended Span.");
        AttributeType type = key.getType();
        if(type == AttributeType.BOOLEAN){
            sofaTracerSpan.setTag(key.getKey(), (Boolean) value);
        }else if(type == AttributeType.DOUBLE || type == AttributeType.LONG){
            sofaTracerSpan.setTag(key.getKey(), (Number) value);
        }else{
            sofaTracerSpan.setTag(key.getKey(), value.toString());
        }
        return this;
    }



    @Override
    public Span addEvent(String name, long timestamp, TimeUnit unit) {
        //convert time
        timestamp =  unit.convert(timestamp, TimeUnit.MILLISECONDS);
        sofaTracerSpan.log(timestamp, name);
        return this;
    }

    @Override
    public Span addEvent(String name, Instant timestamp) {
        sofaTracerSpan.log(timestamp.toEpochMilli(), name);
        return this;
    }



    @Override
    public Span addEvent(String s, Attributes attributes) {
        //s: Map<String,?> Fields.
        //store the event name;
        sofaTracerSpan.log(s);
        //store the attributes.
        attributes.forEach((attributeKey, value) -> {
            AttributeType attributeKey1 =  attributeKey.getType();
            if(attributeKey1 == AttributeType.BOOLEAN){
                Map<String, Boolean> mp = new HashMap<>();
                mp.put(attributeKey.getKey(), (Boolean) value);
                sofaTracerSpan.log(mp);
            }else if(attributeKey1 == AttributeType.DOUBLE || attributeKey1 == AttributeType.LONG){
                Map<String, Number> mp = new HashMap<>();
                mp.put(attributeKey.getKey(), (Number) value);
                sofaTracerSpan.log(mp);
            }else{
                Map<String, String> mp = new HashMap<>();
                mp.put(attributeKey.getKey(), (String) value);
                sofaTracerSpan.log(mp);
            }
        });
        return this;
    }


    @Override
    public Span addEvent(String s, Attributes attributes, long timestamp, TimeUnit timeUnit) {
        long finalTimestamp =  timeUnit.convert(timestamp, TimeUnit.MILLISECONDS);
        //s: Map<String,?> Fields.
        //store the event name;
        sofaTracerSpan.log(s);
        //store the attributes.
        attributes.forEach((attributeKey, value) -> {
            AttributeType attributeKey1 =  attributeKey.getType();
            if(attributeKey1 == AttributeType.BOOLEAN){
                Map<String, Boolean> mp = new HashMap<>();
                mp.put(attributeKey.getKey(), (Boolean) value);
                sofaTracerSpan.log(finalTimestamp,mp);
            }else if(attributeKey1 == AttributeType.DOUBLE || attributeKey1 == AttributeType.LONG){
                Map<String, Number> mp = new HashMap<>();
                mp.put(attributeKey.getKey(), (Number) value);
                sofaTracerSpan.log(finalTimestamp,mp);
            }else{
                Map<String, String> mp = new HashMap<>();
                mp.put(attributeKey.getKey(), (String) value);
                sofaTracerSpan.log(finalTimestamp,mp);
            }
        });
        return this;
    }

    @Override
    public Span setStatus(StatusCode statusCode) {
        if(!sofaTracerSpan.getTagsWithBool().get("OK")){
            if(statusCode == StatusCode.ERROR){
                sofaTracerSpan.setTag(Tags.ERROR.getKey(), true);
            }else if(statusCode == StatusCode.OK){
                sofaTracerSpan.setTag(Tags.ERROR.getKey(), false);
                sofaTracerSpan.setTag("OK",true);
            }
        }
        return this;
    }

    @Override
    public Span setStatus(StatusCode statusCode, String s) {
        if(statusCode == StatusCode.ERROR){
            sofaTracerSpan.setTag("error-info", s);
        }
        return this;
    }


    @Override
    public Span recordException(Throwable throwable, Attributes attributes) {
        String exceptionInfo =  throwable.toString();
        addEvent(exceptionInfo, attributes);
        return this;
    }

    @Override
    public Span updateName(String s) {
        sofaTracerSpan.setOperationName(s);
        return this;
    }

    @Override
    public void end() {
        hasEnded = true;
        sofaTracerSpan.finish();
    }

    @Override
    public void end(long l, TimeUnit timeUnit) {
        l =  timeUnit.convert(l, TimeUnit.MILLISECONDS);
        sofaTracerSpan.finish(l);
    }

    @Override
    public void end(Instant timestamp) {
        sofaTracerSpan.finish(timestamp.toEpochMilli());
    }

    @Override
    public SpanContext getSpanContext() {
        return this.spanContext;
    }

    @Override
    public boolean isRecording() {
        return hasEnded;
    }


    @Override
    public Scope makeCurrent() {
        io.opentracing.Scope scope =  sofaTracer.activateSpan(sofaTracerSpan);
        return new SofaTelemetryScope(scope);
    }
}
