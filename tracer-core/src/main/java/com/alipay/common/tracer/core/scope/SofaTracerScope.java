package com.alipay.common.tracer.core.scope;

import com.alipay.common.tracer.core.SofaTracer;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

public class SofaTracerScope implements Scope {
    private final SofaTracerScopeManager scopeManager;
    private final Span wrapped;
    private final SofaTracerScope toRestore;

    SofaTracerScope(SofaTracerScopeManager scopeManager, Span wrapped) {
        this.scopeManager = scopeManager;
        this.wrapped = wrapped;
        this.toRestore = scopeManager.tlsScope.get();
        scopeManager.tlsScope.set(this);
    }

    @Override
    public void close() {
        if (scopeManager.tlsScope.get() != this) {
            // This shouldn't happen if users call methods in the expected order. Bail out.
            return;
        }

        scopeManager.tlsScope.set(toRestore);
    }

    Span span() {
        return wrapped;
    }
}
