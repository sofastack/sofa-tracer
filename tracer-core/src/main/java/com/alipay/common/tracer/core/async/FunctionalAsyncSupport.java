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
import com.alipay.common.tracer.core.span.SofaTracerSpan;

/**
 * @author khotyn
 * @version FunctionalAsyncSupport.java, v 0.1 2021年02月17日 11:02 下午 khotyn
 */
public class FunctionalAsyncSupport {
    private final long               tid = Thread.currentThread().getId();
    protected final SofaTraceContext traceContext;
    private final SofaTracerSpan     currentSpan;

    public FunctionalAsyncSupport(SofaTraceContext traceContext) {
        this.traceContext = traceContext;
        if (!traceContext.isEmpty()) {
            this.currentSpan = traceContext.getCurrentSpan();
        } else {
            this.currentSpan = null;
        }
    }

    public void doBefore() {
        if (Thread.currentThread().getId() != tid) {
            if (currentSpan != null) {
                traceContext.push(currentSpan);
                SpanExtensionFactory.logStartedSpan(currentSpan);
            }
        }
    }

    public void doFinally() {
        if (Thread.currentThread().getId() != tid) {
            if (currentSpan != null) {
                traceContext.pop();
            }
        }
    }
}