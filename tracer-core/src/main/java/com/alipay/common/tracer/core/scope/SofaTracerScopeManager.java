package com.alipay.common.tracer.core.scope;


import com.alipay.common.tracer.core.span.SofaTracerSpan;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.util.ThreadLocalScope;

public class SofaTracerScopeManager implements ScopeManager{
    final ThreadLocal<SofaTracerScope> tlsScope = new ThreadLocal<SofaTracerScope>();

    @Override
    public SofaTracerScope activate(Span span) {
        return new SofaTracerScope(this, span);
    }

    @Override
    public Span activeSpan() {
        SofaTracerScope scope = tlsScope.get();
        return scope == null ? null : scope.span();
    }
    
    public SofaTracerScope active(){
        return tlsScope.get();
    }
}


