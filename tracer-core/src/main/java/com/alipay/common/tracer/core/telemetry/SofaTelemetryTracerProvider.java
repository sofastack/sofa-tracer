package com.alipay.common.tracer.core.telemetry;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.api.trace.TracerProvider;



import java.util.List;

public  class SofaTelemetryTracerProvider implements TracerProvider {
    static final String DEFAULT_TRACER_NAME = "Null-Name";
    public SofaTelemetryTracerProvider(){

    }

    public static SofaTelemetryTracerProviderBuilder builder() {
        return new SofaTelemetryTracerProviderBuilder();
    }


    @Override
    public Tracer get(String name) {
        return tracerBuilder(name).build();
    }

    @Override
    public Tracer get(String name, String version) {
        return tracerBuilder(name)
        .setInstrumentationVersion(version)
        .build();
    }

    @Override
    public TracerBuilder tracerBuilder(String name){
        if (name == null || name.isEmpty()) {
            name = DEFAULT_TRACER_NAME;
        }
        return new SofaTelemetryBuilder(name);
    }


}
