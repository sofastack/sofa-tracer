/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.extensions.SpanExtensionFactory;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.util.function.Supplier;

/**
 * @author khotyn
 * @version SofaTracerSupplier.java, v 0.1 2021年02月07日 2:02 下午 khotyn
 */
public class SofaTracerSupplier<T> implements Supplier<T> {
    private final long       tid = Thread.currentThread().getId();
    private Supplier<T>      wrappedSupplier;
    private SofaTraceContext traceContext;
    private SofaTracerSpan   currentSpan;

    public SofaTracerSupplier(Supplier<T> wrappedSupplier) {
        this.initCallable(wrappedSupplier, SofaTraceContextHolder.getSofaTraceContext());
    }

    public SofaTracerSupplier(Supplier<T> wrappedSupplier, SofaTraceContext traceContext) {
        this.initCallable(wrappedSupplier, traceContext);
    }

    private void initCallable(Supplier<T> wrappedSupplier, SofaTraceContext traceContext) {
        this.wrappedSupplier = wrappedSupplier;
        this.traceContext = traceContext;
        if (!traceContext.isEmpty()) {
            this.currentSpan = traceContext.getCurrentSpan();
        } else {
            this.currentSpan = null;
        }
    }

    @Override
    public T get() {
        if (Thread.currentThread().getId() != tid) {
            if (currentSpan != null) {
                traceContext.push(currentSpan);
                SpanExtensionFactory.logStartedSpan(currentSpan);
            }
        }
        try {
            return wrappedSupplier.get();
        } finally {
            if (Thread.currentThread().getId() != tid) {
                if (currentSpan != null) {
                    traceContext.pop();
                }
            }
        }
    }
}