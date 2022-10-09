package com.alipay.common.tracer.core.telemetry;

import com.alipay.common.tracer.core.SofaTracer;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;

public class SofaTelemetryTracer implements Tracer {
    private SofaTracer sofaTracer;

    public SofaTelemetryTracer(String name){
        sofaTracer = new SofaTracer.Builder(name).build();
    }

    public SofaTelemetryTracer(SofaTracer sofaTracer){
        this.sofaTracer = sofaTracer;
    }


    @Override
    public SpanBuilder spanBuilder(String name) {
        return new SofaTelemetrySpanBuilder(name, sofaTracer);
    }


}
