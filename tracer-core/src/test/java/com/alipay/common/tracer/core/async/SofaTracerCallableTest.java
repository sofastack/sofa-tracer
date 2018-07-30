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
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import io.opentracing.Span;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @description: [test for SofaTracerCallable]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/7/30
 */
public class SofaTracerCallableTest {

    SofaTraceContext   sofaTraceContext;
    SofaTracerSpan     sofaTracerSpan;
    SofaTracerCallable sofaTracerCallable;

    @Before
    public void setUp() {
        sofaTracerSpan = Mockito.mock(SofaTracerSpan.class);
        sofaTraceContext = Mockito.mock(SofaTraceContext.class);
        sofaTracerCallable = Mockito.mock(SofaTracerCallable.class);
        when(sofaTraceContext.getCurrentSpan()).thenReturn(sofaTracerSpan);
        when(sofaTraceContext.pop()).thenReturn(sofaTracerSpan);
        when(sofaTraceContext.isEmpty()).thenReturn(false);

    }

    @Test
    public void testInstrumentedCallable() throws Exception {
        when(sofaTracerCallable.call()).thenReturn(sofaTracerSpan);
        SofaTracerCallable<Span> spanSofaTracerCallable = new SofaTracerCallable<Span>(
            sofaTracerCallable, sofaTraceContext);
        spanSofaTracerCallable.call();
        verify(sofaTraceContext, times(1)).isEmpty();
        verify(sofaTraceContext, times(1)).getCurrentSpan();
        verify(sofaTracerCallable, times(1)).call();
        verifyNoMoreInteractions(sofaTraceContext, sofaTracerCallable);
    }

    @Test
    public void testInstrumentedCallableNoCurrentSpan() throws Exception {

        when(sofaTracerCallable.call()).thenReturn(sofaTracerSpan);
        when(sofaTraceContext.isEmpty()).thenReturn(true);
        SofaTracerCallable<Span> spanSofaTracerCallable = new SofaTracerCallable<Span>(
            sofaTracerCallable, sofaTraceContext);
        spanSofaTracerCallable.call();
        verify(sofaTraceContext, times(1)).isEmpty();
        verify(sofaTracerCallable, times(1)).call();
        verifyNoMoreInteractions(sofaTraceContext, sofaTracerCallable);
    }
}