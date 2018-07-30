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
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

/**
 * @description: [test for SofaTracerRunnable]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/7/30
 */
public class SofaTracerRunnableTest {

    SofaTraceContext sofaTraceContext;
    SofaTracerSpan   sofaTracerSpan;
    Runnable         wrappedRunnable;

    @Before
    public void setUp() {
        sofaTracerSpan = Mockito.mock(SofaTracerSpan.class);
        sofaTraceContext = Mockito.mock(SofaTraceContext.class);
        wrappedRunnable = Mockito.mock(Runnable.class);
        when(sofaTraceContext.getCurrentSpan()).thenReturn(sofaTracerSpan);
        when(sofaTraceContext.pop()).thenReturn(sofaTracerSpan);
        when(sofaTraceContext.isEmpty()).thenReturn(false);
    }

    @Test
    public void testIntrumentedRunnable() {
        Runnable wrappedRunnable = Mockito.mock(Runnable.class);
        SofaTracerRunnable runnable = new SofaTracerRunnable(wrappedRunnable, sofaTraceContext);
        runnable.run();
        verify(sofaTraceContext, times(1)).getCurrentSpan();
        verify(sofaTraceContext, times(1)).isEmpty();
        verify(wrappedRunnable, times(1)).run();
        verifyNoMoreInteractions(sofaTraceContext, wrappedRunnable);
    }

    @Test
    public void testIntrumentedRunnableNoCurrentSpan() {
        when(sofaTraceContext.isEmpty()).thenReturn(true);

        Runnable wrappedRunnable = Mockito.mock(Runnable.class);
        SofaTracerRunnable runnable = new SofaTracerRunnable(wrappedRunnable, sofaTraceContext);
        runnable.run();
        verify(sofaTraceContext, times(1)).isEmpty();
        verify(wrappedRunnable, times(1)).run();
        verifyNoMoreInteractions(sofaTraceContext, wrappedRunnable);
    }

    @Test
    public void sofaTracerRunnableSamples() throws InterruptedException {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan sofaTracerSpan = Mockito.mock((SofaTracerSpan.class));
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
        SofaTracerSpan sofaTracerSpan = Mockito.mock((SofaTracerSpan.class));
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