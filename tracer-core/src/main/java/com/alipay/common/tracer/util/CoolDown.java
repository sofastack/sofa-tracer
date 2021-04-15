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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A tool to reduce execution frequency of some unimportant operation(such as log duplicated error logs).
 *
 * <p>created at 2021/4/7
 *
 * @author xiangfeng.xzc
 */
class CoolDown {
    final long          intervalMills;
    final int           times;
    final AtomicInteger counter = new AtomicInteger(0);
    final AtomicInteger wip     = new AtomicInteger(0);
    volatile long       nextResetTime;

    CoolDown(long resetIntervalMills, int times) {
        this.intervalMills = resetIntervalMills;
        this.times = times;
        this.nextResetTime = System.currentTimeMillis() + resetIntervalMills;
    }

    boolean tryAcquire() {
        while (true) {
            int c = counter.get();
            if (c < times) {
                if (counter.compareAndSet(c, c + 1)) {
                    return true;
                }
                // CAS fail: retry loop
            } else {
                // Counter full in this period:
                long now = System.currentTimeMillis();
                if (now > nextResetTime) {
                    // wip acts like a SpinLock: only one thread can update 'nextResetTime' and 'counter'
                    if (wip.compareAndSet(0, 1)) {
                        if (now > nextResetTime) {
                            nextResetTime = now + intervalMills;
                            counter.set(0);
                        }
                        wip.set(0);
                    }
                } else {
                    return false;
                }
            }
        }
    }
}
