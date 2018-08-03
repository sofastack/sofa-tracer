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

/**
 *
 * @author luoguimu123
 * @version $Id: SofaTracerCallableTest.java, v 0.1 2017年06月22日 下午3:38 luoguimu123 Exp $
 */
public class SofaTracerCallableTest {
    SofaTraceContext traceContext;
    SofaTracerSpan   span;

    @Before
    public void setUp() {
        span = Mockito.mock(SofaTracerSpan.class);
        traceContext = Mockito.mock(SofaTraceContext.class);
        Mockito.when(traceContext.getCurrentSpan()).thenReturn(span);
        Mockito.when(traceContext.pop()).thenReturn(span);
        Mockito.when(traceContext.isEmpty()).thenReturn(false);
    }

    @Test
    public void testInstrumentedCallable() throws Exception {
        SofaTracerCallable wrappedCallable = Mockito.mock(SofaTracerCallable.class);
        Mockito.when(wrappedCallable.call()).thenReturn(span);

        SofaTracerCallable<Span> spanSofaTracerCallable = new SofaTracerCallable<Span>(
            wrappedCallable, traceContext);
        spanSofaTracerCallable.call();
        Mockito.verify(traceContext, Mockito.times(1)).isEmpty();
        Mockito.verify(traceContext, Mockito.times(1)).getCurrentSpan();

        Mockito.verify(wrappedCallable, Mockito.times(1)).call();
        Mockito.verifyNoMoreInteractions(traceContext, wrappedCallable);
    }

    @Test
    public void testInstrumentedCallableNoCurrentSpan() throws Exception {
        SofaTracerCallable wrappedCallable = Mockito.mock(SofaTracerCallable.class);
        Mockito.when(wrappedCallable.call()).thenReturn(span);
        Mockito.when(traceContext.isEmpty()).thenReturn(true);

        SofaTracerCallable<Span> spanSofaTracerCallable = new SofaTracerCallable<Span>(
            wrappedCallable, traceContext);
        spanSofaTracerCallable.call();
        Mockito.verify(traceContext, Mockito.times(1)).isEmpty();
        Mockito.verify(wrappedCallable, Mockito.times(1)).call();
        Mockito.verifyNoMoreInteractions(traceContext, wrappedCallable);
    }

}