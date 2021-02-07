/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;

import java.util.function.BiPredicate;

/**
 * @author khotyn
 * @version SofaTracerBiPredicate.java, v 0.1 2021年02月07日 11:15 下午 khotyn
 */
public class SofaTracerBiPredicate<T, U> implements BiPredicate<T, U> {
    private final BiPredicate<T, U> wrappedBiPredicate;
    private final FunctionalAsyncSupport functionalAsyncSupport;

    public SofaTracerBiPredicate(BiPredicate<T, U> wrappedBiPredicate) {
        this.wrappedBiPredicate = wrappedBiPredicate;
        this.functionalAsyncSupport = new FunctionalAsyncSupport(SofaTraceContextHolder.getSofaTraceContext());
    }

    @Override
    public boolean test(T t, U u) {
        functionalAsyncSupport.doBefore();
        try {
            return wrappedBiPredicate.test(t, u);
        } finally {
            functionalAsyncSupport.doFinally();
        }
    }
}