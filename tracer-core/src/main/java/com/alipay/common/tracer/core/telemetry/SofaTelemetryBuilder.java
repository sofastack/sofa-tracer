package com.alipay.common.tracer.core.telemetry;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.reporter.facade.Reporter;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;

import java.util.HashMap;

public class SofaTelemetryBuilder implements TracerBuilder {

    SofaTracer.Builder builder;

    private HashMap<String ,String>  info = new HashMap<>();

    public SofaTelemetryBuilder(String name){
         this.builder = new SofaTracer.Builder(name);
    }


    @Override
    public TracerBuilder setSchemaUrl(String s) {
        info.put("schemeUrl",s);
        return this;
    }

    @Override
    public TracerBuilder setInstrumentationVersion(String s) {
        info.put("version",s);
        return this;
    }

    public TracerBuilder withClientReporter(Reporter clientReporter) {
        this.builder.withClientReporter(clientReporter);
        return this;
    }

    public TracerBuilder withServerReporter(Reporter serverReporter) {
        this.builder.withServerReporter(serverReporter);
        return this;
    }


    @Override
    public Tracer build() {
        return new SofaTelemetryTracer(this.builder.build());
    }
}
