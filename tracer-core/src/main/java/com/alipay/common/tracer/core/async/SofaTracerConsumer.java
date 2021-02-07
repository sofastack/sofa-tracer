/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;

import java.util.function.Consumer;

/**
 * @author khotyn
 * @version SofaTracerConsumer.java, v 0.1 2021年02月07日 10:57 下午 khotyn
 */
public class SofaTracerConsumer<T> implements Consumer<T> {
    private final Consumer<T> wrappedConsumer;
    private final FunctionalAsyncSupport functionalAsyncSupport;

    public SofaTracerConsumer(Consumer<T> wrappedConsumer) {
        this.wrappedConsumer = wrappedConsumer;
        this.functionalAsyncSupport = new FunctionalAsyncSupport(SofaTraceContextHolder.getSofaTraceContext());
    }

    @Override
    public void accept(T t) {
        functionalAsyncSupport.doBefore();
        try {
            wrappedConsumer.accept(t);
        } finally {
            functionalAsyncSupport.doFinally();
        }
    }
}