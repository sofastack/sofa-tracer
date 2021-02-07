/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;

import java.util.function.DoubleFunction;

/**
 * @author khotyn
 * @version SofaTracerDoubleFunction.java, v 0.1 2021年02月07日 11:24 下午 khotyn
 */
public class SofaTracerDoubleFunction<R> implements DoubleFunction<R> {
    private final DoubleFunction<R> wrappedDoubleFunction;
    private final FunctionalAsyncSupport functionalAsyncSupport;

    public SofaTracerDoubleFunction(DoubleFunction<R> wrappedDoubleFunction) {
        this.wrappedDoubleFunction = wrappedDoubleFunction;
        this.functionalAsyncSupport = new FunctionalAsyncSupport(SofaTraceContextHolder.getSofaTraceContext());
    }

    @Override
    public R apply(double value) {
        functionalAsyncSupport.doBefore();
        try {
            return wrappedDoubleFunction.apply(value);
        } finally {
            functionalAsyncSupport.doFinally();
        }
    }
}