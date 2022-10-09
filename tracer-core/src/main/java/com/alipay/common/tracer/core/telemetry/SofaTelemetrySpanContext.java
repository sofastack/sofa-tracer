package com.alipay.common.tracer.core.telemetry;

import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.telemetry.common.TracerSharedState;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.trace.IdGenerator;

public class SofaTelemetrySpanContext implements SpanContext {

    private final SofaTracerSpanContext sofaTracerSpanContext;


    public SofaTelemetrySpanContext(SofaTracerSpanContext spanContext){
       this.sofaTracerSpanContext = spanContext;
    }

    io.opentracing.SpanContext getSpanContext(){
        return sofaTracerSpanContext;
    }

    //映射两个API的traceId和SpanId

    @Override
    public String getTraceId() {
        return sofaTracerSpanContext.toTraceId();
    }

    @Override
    public String getSpanId() {
        return sofaTracerSpanContext.toSpanId();
    }

    @Override
    public TraceFlags getTraceFlags() {
        if(sofaTracerSpanContext.isSampled()){
            return TraceFlags.getSampled();
        }
        return TraceFlags.getDefault();
    }

    @Override
    public TraceState getTraceState() {
        return TraceState.getDefault();
    }

    @Override
    public boolean isRemote() {
        return false;
    }
}
