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

import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;

import java.util.function.DoubleToIntFunction;

/**
 * @author khotyn
 * @version SofaTracerDoubleToIntFunction.java, v 0.1 2021年02月18日 11:53 上午 khotyn
 */
public class SofaTracerDoubleToIntFunction implements DoubleToIntFunction {
    private final FunctionalAsyncSupport functionalAsyncSupport;
    private final DoubleToIntFunction    wrappedDoubleToIntFunction;

    public SofaTracerDoubleToIntFunction(DoubleToIntFunction wrappedDoubleToIntFunction) {
        this.wrappedDoubleToIntFunction = wrappedDoubleToIntFunction;
        functionalAsyncSupport = new FunctionalAsyncSupport(
            SofaTraceContextHolder.getSofaTraceContext());
    }

    @Override
    public int applyAsInt(double value) {
        functionalAsyncSupport.doBefore();
        try {
            return wrappedDoubleToIntFunction.applyAsInt(value);
        } finally {
            functionalAsyncSupport.doFinally();
        }
    }
}