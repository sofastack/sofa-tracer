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
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.tag.Tags;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

/**
 * @author luoguimu123
 * @version $Id: SofaTracerRunnableTest.java, v 0.1 June 22, 2017 3:53 PM luoguimu123 Exp $
 */
public class SofaTracerRunnableTest extends AbstractAsyncTest{

    @Before
    public void setUp() {
        tracer =new SofaTracer.Builder("TestRunnable").build();
        span  = (SofaTracerSpan) this.tracer.buildSpan("parent")
                .start();
    }

    @Test
    public void testTracedRunnable() throws InterruptedException {
        SofaTracerSpan parentSpan = (SofaTracerSpan) tracer.buildSpan("foo").start();
        Scope scope = tracer.scopeManager().activate(parentSpan);

        Thread thread = new Thread(toTraced(new TestRunnable()));
        thread.start();
        thread.join();
        scope.close();

        assertEquals(1, tracer.getFinishedSpans().size());
        SofaTracerSpan child = tracer.getFinishedSpans().get(0);
        assertEquals("childRunnable",child.getOperationName());
        assertEquals(1,child.getSpanReferences().size());
        assertEquals(References.CHILD_OF, child.getSpanReferences().get(0).getReferenceType());
    }

    @Test
    public void testTracedRunnableNoParent() throws InterruptedException {
        Thread thread = new Thread(toTraced(new TestRunnable()));
        thread.start();
        thread.join();
        assertEquals(1, tracer.getFinishedSpans().size());
        SofaTracerSpan child = tracer.getFinishedSpans().get(0);
        assertEquals("childRunnable",child.getOperationName());
        assertEquals(0,child.getSpanReferences().size());
    }

    protected  Runnable toTraced(Runnable runnable) {
        return new com.alipay.common.tracer.core.async.SofaTracerRunnable(runnable, tracer);
    }



}