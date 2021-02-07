/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;

import java.util.function.DoubleSupplier;

/**
 * @author khotyn
 * @version SofaTracerDoubleSupplier.java, v 0.1 2021年02月07日 10:53 下午 khotyn
 */
public class SofaTracerDoubleSupplier implements DoubleSupplier {
    private final FunctionalAsyncSupport functionalAsyncSupport;
    private final DoubleSupplier wrappedDoubleSupplier;

    public SofaTracerDoubleSupplier(DoubleSupplier wrappedDoubleSupplier) {
        this.wrappedDoubleSupplier = wrappedDoubleSupplier;
        functionalAsyncSupport = new FunctionalAsyncSupport(SofaTraceContextHolder.getSofaTraceContext());
    }

    @Override
    public double getAsDouble() {
        functionalAsyncSupport.doBefore();
        try {
            return wrappedDoubleSupplier.getAsDouble();
        } finally {
            functionalAsyncSupport.doFinally();
        }
    }
}