package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.util.concurrent.Executor;

public class TracedExecutor implements Executor {

    protected final SofaTracer tracer;
    private final Executor delegate;
    private final boolean traceWithActiveSpanOnly;

    public TracedExecutor(Executor executor, SofaTracer tracer) {
        this(executor, tracer, true);
    }

    public TracedExecutor(Executor executor, SofaTracer tracer, boolean traceWithActiveSpanOnly) {
        this.delegate = executor;
        this.tracer = tracer;
        this.traceWithActiveSpanOnly = traceWithActiveSpanOnly;
    }

    @Override
    public void execute(Runnable runnable) {
        SofaTracerSpan span = createSpan("execute");
        try {
            SofaTracerSpan toActivate = span != null ? span : (SofaTracerSpan) tracer.activeSpan();
            delegate.execute(toActivate == null ? runnable : new SofaTracerRunnable(runnable, tracer));
        } finally {
            // close the span if created
            if (span != null) {
                span.finish();
            }
        }
    }

    SofaTracerSpan createSpan(String operationName) {
        if (tracer.activeSpan() == null && !traceWithActiveSpanOnly) {
            return (SofaTracerSpan) tracer.buildSpan(operationName).start();
        }
        return null;
    }
}
