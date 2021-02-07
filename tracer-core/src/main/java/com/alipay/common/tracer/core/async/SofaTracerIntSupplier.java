/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;

import java.util.function.IntSupplier;

/**
 * @author khotyn
 * @version SofaTracerIntSupplier.java, v 0.1 2021年02月07日 10:41 下午 khotyn
 */
public class SofaTracerIntSupplier implements IntSupplier {
    private final FunctionalAsyncSupport functionalAsyncSupport;
    private final IntSupplier wrappedSupplier;

    public SofaTracerIntSupplier(IntSupplier wrappedSupplier) {
        this.wrappedSupplier = wrappedSupplier;
        functionalAsyncSupport = new FunctionalAsyncSupport(SofaTraceContextHolder.getSofaTraceContext());
    }

    @Override
    public int getAsInt() {
        functionalAsyncSupport.doBefore();
        try {
            return wrappedSupplier.getAsInt();
        } finally {
            functionalAsyncSupport.doFinally();
        }
    }
}