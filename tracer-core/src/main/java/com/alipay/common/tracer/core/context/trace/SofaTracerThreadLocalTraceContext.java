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
package com.alipay.common.tracer.core.context.trace;

import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.util.EmptyStackException;

/**
 * SofaTracerThreadLocalTraceContext
 *
 * @author yangguanchao
 * @since 2018/05/01
 */
public class SofaTracerThreadLocalTraceContext implements SofaTraceContext {

    private final ThreadLocal<SofaTracerSpan> threadLocal = new ThreadLocal<SofaTracerSpan>();

    @Override
    public void push(SofaTracerSpan span) {
        if (span == null) {
            return;
        }
        threadLocal.set(span);
    }

    @Override
    public SofaTracerSpan getCurrentSpan() throws EmptyStackException {
        if (this.isEmpty()) {
            return null;
        }
        return threadLocal.get();
    }

    @Override
    public SofaTracerSpan pop() throws EmptyStackException {
        if (this.isEmpty()) {
            return null;
        }
        SofaTracerSpan sofaTracerSpan = threadLocal.get();
        //remove
        this.clear();
        return sofaTracerSpan;
    }

    @Override
    public int getThreadLocalSpanSize() {
        SofaTracerSpan sofaTracerSpan = threadLocal.get();
        return sofaTracerSpan == null ? 0 : 1;
    }

    @Override
    public boolean isEmpty() {
        SofaTracerSpan sofaTracerSpan = threadLocal.get();
        return sofaTracerSpan == null;
    }

    @Override
    public void clear() {
        threadLocal.remove();
    }
}
