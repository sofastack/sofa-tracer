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

import java.util.function.IntSupplier;

/**
 * @author khotyn
 * @version SofaTracerIntSupplier.java, v 0.1 2021年02月07日 10:41 下午 khotyn
 */
public class SofaTracerIntSupplier implements IntSupplier {
    private final FunctionalAsyncSupport functionalAsyncSupport;
    private final IntSupplier            wrappedSupplier;

    public SofaTracerIntSupplier(IntSupplier wrappedSupplier) {
        this.wrappedSupplier = wrappedSupplier;
        functionalAsyncSupport = new FunctionalAsyncSupport(
            SofaTraceContextHolder.getSofaTraceContext());
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