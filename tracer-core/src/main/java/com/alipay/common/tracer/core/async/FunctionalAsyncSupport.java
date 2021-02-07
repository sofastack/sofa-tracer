/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.extensions.SpanExtensionFactory;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

/**
 * @author khotyn
 * @version FunctionAsyncSupport.java, v 0.1 2021年02月07日 10:42 下午 khotyn
 */
public class FunctionalAsyncSupport {
    private final long tid = Thread.currentThread().getId();
    private final SofaTraceContext traceContext;
    private final SofaTracerSpan currentSpan;

    public FunctionalAsyncSupport(SofaTraceContext traceContext) {
        this.traceContext = traceContext;
        if (!traceContext.isEmpty()) {
            this.currentSpan = traceContext.getCurrentSpan();
        } else {
            this.currentSpan = null;
        }
    }

    public void doBefore() {
        if (Thread.currentThread().getId() != tid) {
            if (currentSpan != null) {
                traceContext.push(currentSpan);
                SpanExtensionFactory.logStartedSpan(currentSpan);
            }
        }
    }

    public void doFinally() {
        if (Thread.currentThread().getId() != tid) {
            if (currentSpan != null) {
                traceContext.pop();
            }
        }
    }
}