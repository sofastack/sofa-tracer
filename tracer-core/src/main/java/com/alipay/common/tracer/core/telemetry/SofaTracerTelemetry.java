package com.alipay.common.tracer.core.telemetry;

import com.alipay.common.tracer.core.tracer.AbstractTracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.MeterBuilder;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;

import javax.annotation.concurrent.ThreadSafe;

public class SofaTracerTelemetry  implements OpenTelemetry {
    private final SofaTelemetryTracerProvider tracerProvider;
    private final ContextPropagators propagators;



    public SofaTracerTelemetry(SofaTelemetryTracerProvider tracerProvider, ContextPropagators propagators) {
        this.tracerProvider = tracerProvider;
        this.propagators = propagators;
    }



    public static OpenTelemetryBuilder builder() {
        return new OpenTelemetryBuilder();
    }

    public TracerProvider getTracerProvider() {
        return this.tracerProvider;
    }


    public ContextPropagators getPropagators() {
        return this.propagators;
    }

    public String toString() {
        return "OpenTelemetry{tracerProvider=" + this.tracerProvider + "propagators}" + this.propagators;
    }


}
