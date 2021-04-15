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
package com.alipay.common.tracer.core.reporter.digest;

import com.alipay.common.tracer.core.appender.manager.AsyncCommonDigestAppenderManager;
import com.alipay.common.tracer.core.reporter.digest.manager.SofaTracerDigestReporterAsyncManager;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;

/**
 *
 * @author abby.zh
 * @since 2.5.1
 */
public class SofaTracerDigestReporterAsyncManagerTest {

    private SofaTracerSpan sofaTracerSpan;

    @Before
    public void before() {
        this.sofaTracerSpan = mock(SofaTracerSpan.class);
    }

    @Test
    public void testGetSofaTracerDigestReporterAsyncManager() throws Exception{
        AtomicInteger npeCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        int testTimes = 1000;
        int threadCount = 100;
        CountDownLatch latch = new CountDownLatch(testTimes);
        for (int times = 0 ; times < testTimes; times ++) {
            Executors.newFixedThreadPool(threadCount).execute(() ->  {
                    try {
                        AsyncCommonDigestAppenderManager sofaTracerDigestReporterAsyncManager =
                                SofaTracerDigestReporterAsyncManager.getSofaTracerDigestReporterAsyncManager();
                        sofaTracerDigestReporterAsyncManager.append(sofaTracerSpan);
                        successCount.getAndIncrement();
                    }catch (NullPointerException e){
                        npeCount.getAndIncrement();
                    }finally {
                        latch.countDown();
                    }
            });
        }
        latch.await();
        Assert.assertEquals(0, npeCount.get());
        Assert.assertEquals(testTimes, successCount.get());
    }
}
