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

/**
 * SofaTraceContext allows an application access and manipulation of the current span state.
 *
 * @author yangguanchao
 * @since  2017/06/17
 */
public interface SofaTraceContext {

    /**
     * Adds the given span to the TraceContext
     *
     * if the span is null ,then ignore pushed
     *
     * @param span The span to be pushed onto the thread local stacked.
     */
    void push(SofaTracerSpan span);

    /**
     * Retrieves the current span without modifying the TraceContext
     *
     * @return returns the current span on the thread local stack without removing it from the stack.
     */
    SofaTracerSpan getCurrentSpan();

    /**
     * Removes a span from the TraceContext
     *
     * @return returns and removes the current span from the top of the stack
     */
    SofaTracerSpan pop();

    /***
     *  Retrieves the current span size stored in current thread local
     *
     * @return the span size of current thread local
     */
    int getThreadLocalSpanSize();

    /***
     * Clear current thread local span
     */
    void clear();

    /**
     * Checks if their is any span set in the current TraceContext
     *
     * @return returns a boolean saying weather or not the thread local is empty
     */
    boolean isEmpty();
}
