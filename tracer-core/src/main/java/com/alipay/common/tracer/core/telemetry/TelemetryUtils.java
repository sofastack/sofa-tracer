package com.alipay.common.tracer.core.telemetry;

import io.opentelemetry.api.trace.SpanContext;

public class TelemetryUtils {
    private TelemetryUtils() {};
    static SofaTelemetrySpanContext getContextShim(SpanContext context){
        return (SofaTelemetrySpanContext) context;
    }

}
