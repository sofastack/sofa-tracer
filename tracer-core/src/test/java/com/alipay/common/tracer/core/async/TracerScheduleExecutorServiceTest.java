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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.*;
import static org.mockito.Mockito.*;

/**
 * @description: [test for TracerScheduleExecutorService]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/7/30
 */
public class TracerScheduleExecutorServiceTest {

    private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
    TracerScheduleExecutorService tracerScheduleExecutorService;
    ScheduledExecutorService      scheduledExecutorService;

    @Before
    public void setUp() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        tracerScheduleExecutorService = new TracerScheduleExecutorService(scheduledExecutorService);
    }

    @Test
    public void scheduleRunnable() throws InterruptedException, ExecutionException {
        Runnable runnable = mock(Runnable.class);
        ScheduledFuture<?> schedule = tracerScheduleExecutorService.schedule(runnable, 1000,
            TIME_UNIT);
        Thread.sleep(1100);
        Assert.assertTrue(schedule.get() == null);
    }

    @Test
    public void scheduleCallable() throws InterruptedException, ExecutionException {
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