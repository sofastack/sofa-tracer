package com.alipay.common.tracer.core.telemetry;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import io.opentelemetry.context.Scope;

public class SofaTelemetryScope implements Scope {
    private io.opentracing.Scope scope;
    public SofaTelemetryScope(io.opentracing.Scope scope){
        this.scope = scope;
    }

    @Override
    public void close() {
        this.scope.close();
    }
}
