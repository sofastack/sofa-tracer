package com.alipay.common.tracer.core.telemetry;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.propagation.ContextPropagators;


import javax.annotation.Nullable;

public class OpenTelemetryBuilder {
    private ContextPropagators propagators = ContextPropagators.noop();
    @Nullable
    private SofaTelemetryTracerProvider tracerProvider;

    OpenTelemetryBuilder() {
    }

    public OpenTelemetryBuilder setTracerProvider(SofaTelemetryTracerProvider tracerProvider) {
        this.tracerProvider = tracerProvider;
        return this;
    }

    public OpenTelemetryBuilder setPropagators(ContextPropagators propagators) {
        this.propagators = propagators;
        return this;
    }




    public SofaTracerTelemetry build() {
        SofaTelemetryTracerProvider tracerProvider = this.tracerProvider;
        if (tracerProvider == null) {
            tracerProvider = SofaTelemetryTracerProvider.builder().build();
        }


        return new SofaTracerTelemetry(tracerProvider,  this.propagators);
    }
}
