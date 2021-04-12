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
package com.alipay.common.tracer.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * <p>created at 2021/4/7
 *
 * @author xiangfeng.xzc
 */
public class CoolDownTest {
    @Test
    public void test() throws Throwable {
        int times = 5;
        int periods = 3;
        final CoolDown coolDown = new CoolDown(1000, times);
        ExecutorService es = Executors.newFixedThreadPool(4);
        List<Future<?>> futures = new ArrayList<Future<?>>();

        final AtomicInteger counter = new AtomicInteger();
        long start = System.currentTimeMillis();
        // 在 1000ms 和 2000ms 时刻 会重置
        // 留下500ms的buffer给线程初次启动造成的延迟
        // 因此预期的计数值为 5*3=15
        final long end = start + (periods - 1) * 1000 + 500;

        for (int i = 0; i < 4; i++) {
            futures.add(es.submit(new Runnable() {
                @Override
                public void run() {
                    while (System.currentTimeMillis() < end) {
                        if (coolDown.tryAcquire()) {
                            counter.incrementAndGet();
                        }
                    }
                }
            }));
        }
        for (Future<?> f : futures) {
            f.get();
        }
        es.shutdown();
        assertEquals(times * periods, counter.get());
    }
}