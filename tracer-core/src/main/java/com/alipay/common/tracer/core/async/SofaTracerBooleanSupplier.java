/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;

import java.util.function.BooleanSupplier;

/**
 * @author khotyn
 * @version SofaTracerBooleanSupplier.java, v 0.1 2021年02月07日 11:19 下午 khotyn
 */
public class SofaTracerBooleanSupplier implements BooleanSupplier {
    private final FunctionalAsyncSupport functionalAsyncSupport;
    private final BooleanSupplier wrappedBooleanSupplier;

    public SofaTracerBooleanSupplier(BooleanSupplier wrappedBooleanSupplier) {
        this.wrappedBooleanSupplier = wrappedBooleanSupplier;
        functionalAsyncSupport = new FunctionalAsyncSupport(SofaTraceContextHolder.getSofaTraceContext());
    }

    @Override
    public boolean getAsBoolean() {
        functionalAsyncSupport.doBefore();
        try {
            return wrappedBooleanSupplier.getAsBoolean();
        } finally {
            functionalAsyncSupport.doFinally();
        }
    }
}