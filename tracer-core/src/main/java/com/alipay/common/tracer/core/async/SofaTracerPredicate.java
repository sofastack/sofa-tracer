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

import java.util.function.Predicate;

/**
 * @author khotyn
 * @version SofaTracerPredicate.java, v 0.1 2021年02月07日 10:54 下午 khotyn
 */
public class SofaTracerPredicate<T> implements Predicate<T> {
    private final Predicate<T>           wrappedPredicate;
    private final FunctionalAsyncSupport functionalAsyncSupport;

    public SofaTracerPredicate(Predicate<T> wrappedPredicate) {
        this.wrappedPredicate = wrappedPredicate;
        this.functionalAsyncSupport = new FunctionalAsyncSupport(
            SofaTraceContextHolder.getSofaTraceContext());
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