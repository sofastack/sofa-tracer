/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;

import java.util.function.DoubleConsumer;

/**
 * @author khotyn
 * @version SofaTracerDoubleConsumer.java, v 0.1 2021年02月07日 11:23 下午 khotyn
 */
public class SofaTracerDoubleConsumer implements DoubleConsumer {
    private final DoubleConsumer wrappedDoubleConsumer;
    private final FunctionalAsyncSupport functionalAsyncSupport;

    public SofaTracerDoubleConsumer(DoubleConsumer wrappedDoubleConsumer) {
        this.wrappedDoubleConsumer = wrappedDoubleConsumer;
        this.functionalAsyncSupport = new FunctionalAsyncSupport(SofaTraceContextHolder.getSofaTraceContext());
    }

    @Override
    public void accept(double value) {
        functionalAsyncSupport.doBefore();
        try {
            wrappedDoubleConsumer.accept(value);
        } finally {
            functionalAsyncSupport.doFinally();
        }
    }
}