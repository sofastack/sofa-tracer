/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;

import java.util.function.DoubleBinaryOperator;

/**
 * @author khotyn
 * @version SofaTracerDoubleBinaryOperator.java, v 0.1 2021年02月07日 11:21 下午 khotyn
 */
public class SofaTracerDoubleBinaryOperator implements DoubleBinaryOperator {
    private final DoubleBinaryOperator wrappedDoubleBinaryOperator;
    private final FunctionalAsyncSupport functionalAsyncSupport;

    public SofaTracerDoubleBinaryOperator(DoubleBinaryOperator wrappedDoubleBinaryOperator) {
        this.wrappedDoubleBinaryOperator = wrappedDoubleBinaryOperator;
        this.functionalAsyncSupport = new FunctionalAsyncSupport(SofaTraceContextHolder.getSofaTraceContext());
    }

    @Override
    public double applyAsDouble(double left, double right) {
        functionalAsyncSupport.doBefore();
        try {
            return wrappedDoubleBinaryOperator.applyAsDouble(left, right);
        } finally {
            functionalAsyncSupport.doFinally();
        }
    }
}