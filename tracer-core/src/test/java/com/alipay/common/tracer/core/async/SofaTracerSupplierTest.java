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
import com.alipay.common.tracer.core.reporter.digest.DiskReporterImpl;
import com.alipay.common.tracer.core.reporter.facade.Reporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracertest.encoder.ClientSpanEncoder;
import com.alipay.common.tracer.core.tracertest.encoder.ServerSpanEncoder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author khotyn
 * @version SofaTracerSupplierTest.java, v 0.1 2021年02月07日 2:12 下午 khotyn
 */
public class SofaTracerSupplierTest {
    private final String   tracerType    = "SofaTracerSpanTest";
    private final String   clientLogType = "client-log-test.log";
    private final String   serverLogType = "server-log-test.log";
    private SofaTracer     sofaTracer;
    private SofaTracerSpan sofaTracerSpan;

    @Before
    public void setup() {
        Reporter clientReporter = new DiskReporterImpl(clientLogType, new ClientSpanEncoder());
        Reporter serverReporter = new DiskReporterImpl(serverLogType, new ServerSpanEncoder());
        sofaTracer = new SofaTracer.Builder(tracerType)
            .withTag("tracer", "SofaTraceContextHolderTest").withClientReporter(clientReporter)
            .withServerReporter(serverReporter).build();
        sofaTracerSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("SofaTracerSpanTest").start();
    }

    @Test
    public void testSupplier() throws ExecutionException, InterruptedException {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        sofaTraceContext.push(sofaTracerSpan);
        CompletableFuture<SofaTracerSpan> future = CompletableFuture.supplyAsync(new SofaTracerSupplier<>(() -> SofaTraceContextHolder.getSofaTraceContext().getCurrentSpan()));
        Assert.assertEquals(sofaTracerSpan, future.get());
    }

    @Test
    public void testRawSupplier() throws ExecutionException, InterruptedException {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        sofaTraceContext.push(sofaTracerSpan);
        CompletableFuture<SofaTracerSpan> future = CompletableFuture.supplyAsync(() -> SofaTraceContextHolder.getSofaTraceContext().getCurrentSpan());
        Assert.assertNull(future.get());
    }
}