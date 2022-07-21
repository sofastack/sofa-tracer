package com.alipay.common.tracer.core.scope;


import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

public class SofaTracerScopeManager implements ScopeManager {

    @Override
    public Scope activate(Span span) {
        return null;
    }

    @Override
    public Span activeSpan() {
        return null;
    }
}


