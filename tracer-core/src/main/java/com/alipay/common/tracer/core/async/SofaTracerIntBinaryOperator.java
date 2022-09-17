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

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import io.opentracing.Scope;

import java.util.function.IntBinaryOperator;

/**
 * @author khotyn
 * @version SofaTracerIntBinaryOperator.java, v 0.1 2021年02月18日 11:58 上午 khotyn
 */
public class SofaTracerIntBinaryOperator implements IntBinaryOperator {
    private final FunctionalAsyncSupport functionalAsyncSupport;
    private final IntBinaryOperator      wrappedIntBinaryOperator;

    public SofaTracerIntBinaryOperator(IntBinaryOperator wrappedIntBinaryOperator, SofaTracer tracer) {
        this.wrappedIntBinaryOperator = wrappedIntBinaryOperator;
        functionalAsyncSupport = new FunctionalAsyncSupport(
                tracer);
    }

    @Override
    public int applyAsInt(int left, int right) {
       Scope scope = functionalAsyncSupport.doBefore();
        try {
            return wrappedIntBinaryOperator.applyAsInt(left, right);
        } finally {
            functionalAsyncSupport.doFinally(scope);
        }
    }
}