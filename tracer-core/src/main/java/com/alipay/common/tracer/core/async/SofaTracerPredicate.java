/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;

import java.util.function.Predicate;

/**
 * @author khotyn
 * @version SofaTracerPredicate.java, v 0.1 2021年02月07日 10:54 下午 khotyn
 */
public class SofaTracerPredicate<T> implements Predicate<T> {
    private final Predicate<T> wrappedPredicate;
    private final FunctionalAsyncSupport functionalAsyncSupport;

    public SofaTracerPredicate(Predicate<T> wrappedPredicate) {
        this.wrappedPredicate = wrappedPredicate;
        this.functionalAsyncSupport = new FunctionalAsyncSupport(SofaTraceContextHolder.getSofaTraceContext());
    }

    @Override
    public boolean test(T t) {
        functionalAsyncSupport.doBefore();
        try {
            return wrappedPredicate.test(t);
        } finally {
            functionalAsyncSupport.doFinally();
        }
    }
}