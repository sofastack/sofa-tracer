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
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import io.opentracing.Scope;

import java.lang.Runnable;

/**
 * Runnable that passes Span between threads. The Span name is
 * taken either from the passed value or from the interface.
 *
 * @author luoguimu123
 * @version $Id: Runnable.java, v 0.1 June 19, 2017 5:54 PM luoguimu123 Exp $
 */
public class SofaTracerRunnable implements Runnable {
    private Runnable                 wrappedRunnable;
    protected FunctionalAsyncSupport functionalAsyncSupport;

    public SofaTracerRunnable(Runnable wrappedRunnable, SofaTracer tracer) {
        this.initRunnable(wrappedRunnable, tracer);
    }

    private void initRunnable(Runnable wrappedRunnable, SofaTracer tracer) {
        this.wrappedRunnable = wrappedRunnable;
        this.functionalAsyncSupport = new FunctionalAsyncSupport(tracer);
    }

    @Override
    public void run() {
       Scope scope =  functionalAsyncSupport.doBefore();
        try {
            wrappedRunnable.run();
        } finally {
            functionalAsyncSupport.doFinally(scope);
        }
    }
}