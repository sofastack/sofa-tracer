package com.alipay.common.tracer.core.telemetry.baggage;

import com.alipay.common.tracer.core.span.SofaTracerSpan;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.baggage.BaggageEntryMetadata;
import io.opentracing.Span;

public class SofaTelemetryBaggageBuilder implements BaggageBuilder {

    SofaTracerSpan sofaTracerSpan;
    public SofaTelemetryBaggageBuilder(SofaTracerSpan sofaTracerSpan){
        this.sofaTracerSpan = sofaTracerSpan;
    }


    @Override
    public BaggageBuilder put(String s, String s1, BaggageEntryMetadata baggageEntryMetadata) {
        sofaTracerSpan.setBaggageItem(s,s1);
        return this;
    }

    @Override
    public BaggageBuilder remove(String s) {
        return this;
    }

    @Override
    public Baggage build() {
        return new SofaTelemetryBaggage(sofaTracerSpan);
    }
}
