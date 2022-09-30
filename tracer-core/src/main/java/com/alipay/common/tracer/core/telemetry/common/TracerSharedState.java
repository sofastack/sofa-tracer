package com.alipay.common.tracer.core.telemetry.common;


import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class TracerSharedState {
    private final Object lock = new Object();

    private final Sampler sampler;
    private final SpanProcessor activeSpanProcessor;
    @Nullable
    private volatile CompletableResultCode shutdownResult = null;

    public TracerSharedState(Sampler sampler, List<SpanProcessor> spanProcessors) {
        this.sampler = sampler;
        this.activeSpanProcessor = SpanProcessor.composite(spanProcessors);
    }


    Sampler getSampler() {
        return this.sampler;
    }

    public SpanProcessor getActiveSpanProcessor() {
        return this.activeSpanProcessor;
    }

    public boolean hasBeenShutdown() {
        return this.shutdownResult != null;
    }

    public CompletableResultCode shutdown() {
        synchronized(this.lock) {
            if (this.shutdownResult != null) {
                return this.shutdownResult;
            } else {
                //this.shutdownResult = this.activeSpanProcessor.shutdown();
                return this.shutdownResult;
            }
        }
    }
}
