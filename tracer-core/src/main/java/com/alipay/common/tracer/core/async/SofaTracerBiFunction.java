/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;

import java.util.function.BiFunction;

/**
 * @author khotyn
 * @version SofaTracerBiFunction.java, v 0.1 2021年02月07日 11:14 下午 khotyn
 */
public class SofaTracerBiFunction<T, U, R> implements BiFunction<T, U, R> {
    private final BiFunction<T, U, R> wrappedBiFunction;
    private final FunctionalAsyncSupport functionalAsyncSupport;

    public SofaTracerBiFunction(BiFunction<T, U, R> wrappedBiFunction) {
        this.wrappedBiFunction = wrappedBiFunction;
        this.functionalAsyncSupport = new FunctionalAsyncSupport(SofaTraceContextHolder.getSofaTraceContext());
    }

    @Override
    public R apply(T t, U u) {
        functionalAsyncSupport.doBefore();
        try {
            return wrappedBiFunction.apply(t, u);
        } finally {
            functionalAsyncSupport.doFinally();
        }
    }
}