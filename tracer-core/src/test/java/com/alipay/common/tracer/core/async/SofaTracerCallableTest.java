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
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author luoguimu123
 * @version $Id: SofaTracerCallableTest.java, v 0.1 June 22, 2017 3:38 PM luoguimu123 Exp $
 */
public class SofaTracerCallableTest extends AbstractAsyncTest{


    @Before
    public void setUp() {

        countDownLatch = new CountDownLatch(1);
        tracer =new SofaTracer.Builder("TestCallable").build();
        span  = (SofaTracerSpan) this.tracer.buildSpan("parent")
                .start();
    }
    @Test
    public void testTracedCallable() throws InterruptedException, ExecutionException {
        Scope scope = tracer.scopeManager().activate(span);
        FutureTask<Void> futureTask = new FutureTask<Void>(toTraced(new TestCallable()));
        Thread thread = createThread(futureTask);
        thread.start();
        futureTask.get();
        thread.join();
        scope.close();
        assertEquals(1, tracer.getFinishedSpans().size());
        SofaTracerSpan child = tracer.getFinishedSpans().get(0);
        assertEquals("childCallable",child.getOperationName());
        assertEquals(1,child.getSpanReferences().size());
        assertEquals(References.CHILD_OF, child.getSpanReferences().get(0).getReferenceType());
    }

    @Test
    public void testTracedCallableNoParent() throws Throwable {
        FutureTask<Void> futureTask = new FutureTask<Void>(toTraced(new TestCallable()));
        Thread thread = createThread(futureTask);
        thread.start();
        futureTask.get();
        thread.join();
        assertEquals(1, tracer.getFinishedSpans().size());
        assertTrue((tracer.getFinishedSpans().get(0)).getSpanReferences().isEmpty());
        SofaTracerSpan child = tracer.getFinishedSpans().get(0);
        assertEquals("childCallable",child.getOperationName());

    }

    protected <V> Callable<V> toTraced(Callable<V> callable) {
        return new SofaTracerCallable<>(callable, tracer);
    }

}