/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;

import java.util.function.BiConsumer;

/**
 * @author khotyn
 * @version SofaTracerBiConsumer.java, v 0.1 2021年02月07日 11:12 下午 khotyn
 */
public class SofaTracerBiConsumer<T, U> implements BiConsumer<T, U> {
    private final BiConsumer<T, U> wrappedBiConsumer;
    private final FunctionalAsyncSupport functionalAsyncSupport;

    public SofaTracerBiConsumer(BiConsumer<T, U> wrappedBiConsumer) {
        this.wrappedBiConsumer = wrappedBiConsumer;
        this.functionalAsyncSupport = new FunctionalAsyncSupport(SofaTraceContextHolder.getSofaTraceContext());
    }

    @Override
    public void accept(T t, U u) {
        functionalAsyncSupport.doBefore();
        try {
            wrappedBiConsumer.accept(t, u);
        } finally {
            functionalAsyncSupport.doFinally();
        }
    }
}
