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
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @description: [test for TracerScheduleExecutorService]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/7/30
 */
public class TracerScheduleExecutorServiceTest {

    private static final TimeUnit TIME_UNIT     = TimeUnit.MILLISECONDS;
    TracerScheduleExecutorService tracerScheduleExecutorService;
    ScheduledExecutorService      scheduledExecutorService;

    private final String          tracerType    = "SofaTracerSpanTest";
    private final String          clientLogType = "client-log-test.log";
    private final String          serverLogType = "server-log-test.log";
    private SofaTracer            sofaTracer;
    private SofaTracerSpan        sofaTracerSpan;

    @Before
    public void setUp() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        tracerScheduleExecutorService = new TracerScheduleExecutorService(scheduledExecutorService);
        Reporter clientReporter = new DiskReporterImpl(clientLogType, new ClientSpanEncoder());
        Reporter serverReporter = new DiskReporterImpl(serverLogType, new ServerSpanEncoder());
        sofaTracer = new SofaTracer.Builder(tracerType)
            .withTag("tracer", "SofaTraceContextHolderTest").withClientReporter(clientReporter)
            .withServerReporter(serverReporter).build();
        sofaTracerSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("SofaTracerSpanTest").start();
    }

    @Test
    public void scheduleRunnable() throws InterruptedException {
        final Map<String, Integer> taskMap = new HashMap<String, Integer>();
        taskMap.put("key", 1);
        SofaTracerRunnable sofaTracerRunnable = new SofaTracerRunnable(new Runnable() {
            @Override
            public void run() {
                taskMap.put("key", taskMap.get("key") + 1);
            }
        });
        tracerScheduleExecutorService.schedule(sofaTracerRunnable, 1000, TIME_UNIT);
        Thread.sleep(1100);
        Integer result = taskMap.get("key");
        Assert.assertTrue(result == 2);
    }

    @Test
    public void scheduleRunnable_with_tracerContext() throws Exception {
        SofaTraceContext sofaTraceContext = new SofaTracerThreadLocalTraceContext();
        sofaTraceContext.push(sofaTracerSpan);
        final Map<String, Integer> taskMap = new HashMap<String, Integer>();
        taskMap.put("key", 1);
        SofaTracerRunnable sofaTracerRunnable = new SofaTracerRunnable(new Runnable() {
            @Override
            public void run() {
                taskMap.put("key", taskMap.get("key") + 1);
            }
        }, sofaTraceContext);
        tracerScheduleExecutorService.schedule(sofaTracerRunnable, 1000, TIME_UNIT);
        Thread.sleep(1100);
        Integer result = taskMap.get("key");
        Assert.assertTrue(result == 2);
        //check currentSpan
        Field field = sofaTracerRunnable.getClass().getDeclaredField("traceContext");
        field.setAccessible(true);
        SofaTraceContext resultSofaTraceContext = (SofaTraceContext) field.get(sofaTracerRunnable);
        SofaTracerSpan currentSpan = resultSofaTraceContext.getCurrentSpan();
        Assert.assertTrue(currentSpan.getOperationName().equals("SofaTracerSpanTest"));
    }

    @Test
    public void scheduleCallable() throws Exception {
        final Object testObj = new Object();
        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                return testObj;
            }
        };
        ScheduledFuture schedule = tracerScheduleExecutorService
            .schedule(callable, 1000, TIME_UNIT);
        Thread.sleep(1100);
        Object o = schedule.get();
        Assert.assertTrue(testObj == o);
    }

}