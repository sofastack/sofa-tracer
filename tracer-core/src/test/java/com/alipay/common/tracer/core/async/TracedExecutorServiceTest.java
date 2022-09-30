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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author luoguimu123
 * @version $Id: TracedExecutorServiceTest.java, v 0.1 June 22, 2017 3:59 PM luoguimu123 Exp $
 */
public class TracedExecutorServiceTest   extends AbstractAsyncTest{
    private static final int NUMBER_OF_THREADS = 4;

    protected ExecutorService toTraced(ExecutorService executorService) {
        return new TracedExecutorService(executorService, tracer);
    }

    protected ExecutorService toTracedCreatingParent(ExecutorService executorService) {
        return new TracedExecutorService(executorService, tracer, false);
    }

    @Before
    public void setUp() {
        countDownLatch = new CountDownLatch(1);
        tracer =new SofaTracer.Builder("TestExecutorService").build();
        span  = (SofaTracerSpan) this.tracer.buildSpan("parent")
                .start();
    }

    @Test
    public void testExecuteRunnable() throws InterruptedException {
        ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

        SofaTracerSpan parentSpan = (SofaTracerSpan) tracer.buildSpan("foo")
                .start();
        Scope scope = tracer.scopeManager().activate(parentSpan);
        executorService.execute(new TestRunnable());
        scope.close();
        countDownLatch.await();
        assertEquals(1, this.tracer.getFinishedSpans().size());
        SofaTracerSpan span  = this.tracer.getFinishedSpans().get(0);
        assertEquals("childRunnable",span.getOperationName());
        assertEquals(1,span.getSpanReferences().size());
        assertEquals(References.CHILD_OF, span.getSpanReferences().get(0).getReferenceType());
    }

    @Test
    public void testExecuteRunnableNoParent() throws InterruptedException {
        ExecutorService executorService = toTracedCreatingParent(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
        executorService.execute(new TestRunnable());
        countDownLatch.await();
        assertEquals(2, this.tracer.getFinishedSpans().size());
    }

    @Test
    public void testSubmitRunnable() throws InterruptedException {
        ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

        SofaTracerSpan parentSpan = (SofaTracerSpan) tracer.buildSpan("foo").start();
        Scope scope = tracer.scopeManager().activate(parentSpan);
        executorService.submit(new TestRunnable());
        scope.close();

        countDownLatch.await();
        assertEquals(1, this.tracer.getFinishedSpans().size());
        SofaTracerSpan span  = this.tracer.getFinishedSpans().get(0);
        System.out.println(span.getOperationName());
//        assertEquals("childRunnable",span.getOperationName());
//        assertEquals(1,span.getSpanReferences().size());
//        assertEquals(References.CHILD_OF, span.getSpanReferences().get(0).getReferenceType());
    }
    //
    @Test
    public void testSubmitRunnableNoParent() throws InterruptedException {
        ExecutorService executorService = toTracedCreatingParent(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
        executorService.submit(new TestRunnable());
        countDownLatch.await();
        assertEquals(2, this.tracer.getFinishedSpans().size());
    }
    //
    @Test
    public void testSubmitRunnableTyped() throws InterruptedException {
        ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

        SofaTracerSpan parentSpan = (SofaTracerSpan) tracer.buildSpan("foo").start();
        Scope scope = tracer.scopeManager().activate(parentSpan);
        executorService.submit(new TestRunnable(), new Object());
        scope.close();

        countDownLatch.await();
        //assertParentSpan(parentSpan);
        assertEquals(1, this.tracer.getFinishedSpans().size());
    }
    //
    @Test
    public void testSubmitRunnableTypedNoParent() throws InterruptedException {
        ExecutorService executorService = toTracedCreatingParent(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
        executorService.submit(new TestRunnable(), new Object());
        countDownLatch.await();
        assertEquals(2, tracer.getFinishedSpans().size());
    }
    //
    @Test
    public void testSubmitCallable() throws InterruptedException {
        ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

        SofaTracerSpan parentSpan = (SofaTracerSpan) tracer.buildSpan("foo").start();
        Scope scope = tracer.scopeManager().activate(parentSpan);
        executorService.submit(new TestCallable());
        scope.close();

        countDownLatch.await();

        assertEquals(1, tracer.getFinishedSpans().size());
    }
    //
    @Test
    public void testSubmitCallableNoParent() throws InterruptedException {
        ExecutorService executorService = toTracedCreatingParent(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
        executorService.submit(new TestCallable());
        countDownLatch.await();
        assertEquals(2, tracer.getFinishedSpans().size());
    }

    @Test
    public void testInvokeAll() throws InterruptedException {
        ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
        SofaTracerSpan parentSpan = (SofaTracerSpan) tracer.buildSpan("foo").start();
        Scope scope = tracer.scopeManager().activate(parentSpan);
        countDownLatch = new CountDownLatch(2);
        executorService.invokeAll(Arrays.asList(new TestCallable(), new TestCallable()));
        scope.close();
        countDownLatch.await();
        // assertParentSpan(parentSpan);
        assertEquals(2, tracer.getFinishedSpans().size());
    }
    //
    @Test
    public void testInvokeAllNoParent() throws InterruptedException {
        ExecutorService executorService = toTracedCreatingParent(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
        countDownLatch = new CountDownLatch(2);
        executorService.invokeAll(Arrays.asList(new TestCallable(), new TestCallable()));
        countDownLatch.await();
        assertEquals(3,tracer.getFinishedSpans().size());
    }
    //
    @Test
    public void testInvokeAllTimeUnit() throws InterruptedException {
        ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

        SofaTracerSpan parentSpan = (SofaTracerSpan) tracer.buildSpan("foo").start();
        Scope scope = tracer.scopeManager().activate(parentSpan);
        countDownLatch = new CountDownLatch(2);
        executorService.invokeAll(Arrays.asList(new TestCallable(), new TestCallable()), 1, TimeUnit.SECONDS);
        scope.close();
        countDownLatch.await();
        // assertParentSpan(parentSpan);
        assertEquals(2, tracer.getFinishedSpans().size());
    }
    //
    @Test
    public void testInvokeAllTimeUnitNoParent() throws InterruptedException {
        ExecutorService executorService = toTracedCreatingParent(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
        countDownLatch = new CountDownLatch(2);
        executorService.invokeAll(Arrays.asList(new TestCallable(), new TestCallable()), 1, TimeUnit.SECONDS);
        countDownLatch.await();
        assertEquals(3, tracer.getFinishedSpans().size());
    }

    @Test
    public void testInvokeAnyTimeUnit() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

        SofaTracerSpan parentSpan = (SofaTracerSpan) tracer.buildSpan("foo").start();
        Scope scope = tracer.scopeManager().activate(parentSpan);
        executorService.invokeAny(Arrays.asList(new TestCallable()), 1, TimeUnit.SECONDS);
        scope.close();

        countDownLatch.await();
        //assertParentSpan(parentSpan);
        assertEquals(1, tracer.getFinishedSpans().size());
    }

    @Test
    public void testInvokeAnyTimeUnitNoParent() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executorService = toTracedCreatingParent(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
        executorService.invokeAny(Arrays.asList(new TestCallable()), 1, TimeUnit.SECONDS);
        countDownLatch.await();
        assertEquals(2, tracer.getFinishedSpans().size());
    }

    @Test
    public void testInvokeAny() throws InterruptedException, ExecutionException {
        ExecutorService executorService = toTraced(Executors.newFixedThreadPool(NUMBER_OF_THREADS));

        SofaTracerSpan parentSpan = (SofaTracerSpan) tracer.buildSpan("foo").start();
        Scope scope = tracer.scopeManager().activate(parentSpan);
        executorService.invokeAny(Arrays.asList(new TestCallable()));
        scope.close();

        countDownLatch.await();
        //assertParentSpan(parentSpan);
        assertEquals(1, tracer.getFinishedSpans().size());
    }
    //
    @Test
    public void testInvokeAnyNoParent() throws InterruptedException, ExecutionException {
        ExecutorService executorService = toTracedCreatingParent(Executors.newFixedThreadPool(NUMBER_OF_THREADS));
        executorService.invokeAny(Arrays.asList(new TestCallable()));
        countDownLatch.await();
        assertEquals(2, tracer.getFinishedSpans().size());
    }


}
