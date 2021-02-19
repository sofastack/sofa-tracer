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
import com.alipay.common.tracer.core.context.trace.SofaTracerThreadLocalTraceContext;
import com.alipay.common.tracer.core.reporter.digest.DiskReporterImpl;
import com.alipay.common.tracer.core.reporter.facade.Reporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracertest.encoder.ClientSpanEncoder;
import com.alipay.common.tracer.core.tracertest.encoder.ServerSpanEncoder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author guolei.sgl
 * @description [test for TracerScheduleExecutorService]
 * @email <a href="guolei.sgl@antfin.com"></a>
 * @date 18/7/30
 */
public class TracerScheduleExecutorServiceTest {

    private static final TimeUnit         TIME_UNIT = TimeUnit.MILLISECONDS;
    private TracerScheduleExecutorService tracerScheduleExecutorService;
    private SofaTracerSpan                sofaTracerSpan;

    @Before
    public void setUp() {
        ScheduledExecutorService scheduledExecutorService = Executors
            .newSingleThreadScheduledExecutor();
        tracerScheduleExecutorService = new TracerScheduleExecutorService(scheduledExecutorService);
        String clientLogType = "client-log-test.log";
        Reporter clientReporter = new DiskReporterImpl(clientLogType, new ClientSpanEncoder());
        String serverLogType = "server-log-test.log";
        Reporter serverReporter = new DiskReporterImpl(serverLogType, new ServerSpanEncoder());
        String tracerType = "SofaTracerSpanTest";
        SofaTracer sofaTracer = new SofaTracer.Builder(tracerType)
            .withTag("tracer", "SofaTraceContextHolderTest").withClientReporter(clientReporter)
            .withServerReporter(serverReporter).build();
        sofaTracerSpan = (SofaTracerSpan) sofaTracer.buildSpan("SofaTracerSpanTest").start();
    }

    @Test
    public void scheduleRunnable() throws InterruptedException {
        final Map<String, Integer> taskMap = new HashMap<>();
        taskMap.put("key", 1);
        SofaTracerRunnable sofaTracerRunnable = new SofaTracerRunnable(() -> taskMap.put("key", taskMap.get("key") + 1));
        tracerScheduleExecutorService.schedule(sofaTracerRunnable, 1000, TIME_UNIT);
        Thread.sleep(1100);
        Integer result = taskMap.get("key");
        Assert.assertEquals(2, (int) result);
    }

    @Test
    public void scheduleRunnable_with_tracerContext() throws Exception {
        SofaTraceContext sofaTraceContext = new SofaTracerThreadLocalTraceContext();
        sofaTraceContext.push(sofaTracerSpan);
        final Map<String, Integer> taskMap = new HashMap<>();
        taskMap.put("key", 1);
        SofaTracerRunnable sofaTracerRunnable = new SofaTracerRunnable(() -> taskMap.put("key", taskMap.get("key") + 1), sofaTraceContext);
        tracerScheduleExecutorService.schedule(sofaTracerRunnable, 1000, TIME_UNIT);
        Thread.sleep(1100);
        Integer result = taskMap.get("key");
        Assert.assertEquals(2, (int) result);
        //check currentSpan
        SofaTracerSpan currentSpan = sofaTracerRunnable.functionalAsyncSupport.traceContext.getCurrentSpan();
        Assert.assertEquals("SofaTracerSpanTest", currentSpan.getOperationName());
    }

    @Test
    public void scheduleCallable() throws Exception {
        final Object testObj = new Object();
        ScheduledFuture<Object> schedule = tracerScheduleExecutorService
                .schedule(() -> testObj, 1000, TIME_UNIT);
        Thread.sleep(1100);
        Object o = schedule.get();
        Assert.assertSame(testObj, o);
    }
}