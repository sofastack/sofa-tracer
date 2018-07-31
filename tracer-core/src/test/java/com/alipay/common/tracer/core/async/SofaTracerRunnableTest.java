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
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

/**
 * @author luoguimu123
 * @version $Id: SofaTracerRunnableTest.java, v 0.1 2017年06月22日 下午3:53 luoguimu123 Exp $
 */
public class SofaTracerRunnableTest {
    SofaTraceContext traceContext;
    SofaTracerSpan   span;

    @Before
    public void setUp() {
        span = mock(SofaTracerSpan.class);
        traceContext = mock(SofaTraceContext.class);
        when(traceContext.getCurrentSpan()).thenReturn(span);
        when(traceContext.pop()).thenReturn(span);
        when(traceContext.isEmpty()).thenReturn(false);
    }

    @Test
    public void testIntrumentedRunnable() {
        Runnable wrappedRunnable = mock(Runnable.class);
        SofaTracerRunnable runnable = new SofaTracerRunnable(wrappedRunnable, traceContext);

        runnable.run();

        verify(traceContext, times(1)).getCurrentSpan();
        verify(traceContext, times(1)).isEmpty();
        verify(wrappedRunnable, times(1)).run();
        verifyNoMoreInteractions(traceContext, wrappedRunnable);
    }

    @Test
    public void testIntrumentedRunnableNoCurrentSpan() {
        when(traceContext.isEmpty()).thenReturn(true);

        Runnable wrappedRunnable = mock(Runnable.class);
        SofaTracerRunnable runnable = new SofaTracerRunnable(wrappedRunnable, traceContext);

        runnable.run();

        verify(traceContext, times(1)).isEmpty();
        verify(wrappedRunnable, times(1)).run();
        verifyNoMoreInteractions(traceContext, wrappedRunnable);
    }

    @Test
    public void sofaTracerRunnableSamples() throws InterruptedException {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan sofaTracerSpan = mock((SofaTracerSpan.class));
        sofaTraceContext.push(sofaTracerSpan);
        RunnableTread runnableTread = new RunnableTread();
        SofaTracerRunnable sofaTracerRunnable = new SofaTracerRunnable(runnableTread,
            sofaTraceContext);
        //run1
        sofaTracerRunnable.run();
        assertEquals(1, sofaTraceContext.getThreadLocalSpanSize());
        assertEquals(Thread.currentThread().getId(), runnableTread.getThreadId());
        assertEquals(Thread.currentThread().getName(), runnableTread.getThreadName());
        //run2
        Thread thread = new Thread(sofaTracerRunnable);
        thread.start();
        Thread.sleep(3 * 1000);
        assertEquals(1, sofaTraceContext.getThreadLocalSpanSize());
        assertEquals(2, runnableTread.atomicLong.get());
        assertNotEquals(Thread.currentThread().getId(), runnableTread.getThreadId());
        assertNotEquals(Thread.currentThread().getName(), runnableTread.getThreadName());
    }

    @Test
    public void samples() throws Exception {

        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan sofaTracerSpan = mock((SofaTracerSpan.class));
        sofaTraceContext.push(sofaTracerSpan);
        Thread thread = new Thread(new SofaTracerRunnable(new Runnable() {
            @Override
            public void run() {
                //do something your business code
            }
        }));
        thread.start();
        assertEquals(1, sofaTraceContext.getThreadLocalSpanSize());
        sofaTraceContext.pop();
        assertEquals(0, sofaTraceContext.getThreadLocalSpanSize());
    }

    static class RunnableTread implements Runnable {

        public static AtomicLong atomicLong = new AtomicLong(0);

        private long             threadId   = 0;

        private String           threadName = "";

        @Override
        public void run() {
            //do something
            this.threadId = Thread.currentThread().getId();
            this.threadName = Thread.currentThread().getName();
            atomicLong.incrementAndGet();
        }

        public long getThreadId() {
            return threadId;
        }

        public String getThreadName() {
            return threadName;
        }
    }
}