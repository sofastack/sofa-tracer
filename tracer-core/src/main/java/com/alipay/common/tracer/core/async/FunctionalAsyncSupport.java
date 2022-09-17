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
import com.alipay.common.tracer.core.extensions.SpanExtensionFactory;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import io.opentracing.Scope;

/**
 * @author khotyn
 * @version FunctionalAsyncSupport.java, v 0.1 2021年02月17日 11:02 下午 khotyn
 */
public class FunctionalAsyncSupport {
    private final long   tid = Thread.currentThread().getId();
    protected final SofaTracer tracer;
    private final SofaTracerSpan currentSpan;

    public FunctionalAsyncSupport(SofaTracer tracer) {
        this.tracer = tracer;
        this.currentSpan = (SofaTracerSpan) tracer.activeSpan();
    }

    public Scope doBefore() {
        if (Thread.currentThread().getId() != tid) {
            if (currentSpan != null) {
                SpanExtensionFactory.logStartedSpan(currentSpan);
                return tracer.scopeManager().activate(currentSpan);
            }
        }
        return null;
    }

    public void doFinally(Scope scope) {
        if (Thread.currentThread().getId() != tid) {
            if (scope != null) {
                scope.close();
            }
        }
    }
}