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
 * 封装一个定时重置的计数器用于降低某些非必要操作的频率(比如某些预期内的错误日志).
 * 具有如下特征:
 * <ul>
 *     <li>计数器的值永远不超过times</li>
 *     <li>当计数器达到times并且尝试增加计数, 如果此时已经到了重置时间点, 那么重置计数器并重试, 否则本次操作不成功</li>
 *     <li>只有计数器满才会尝试重置, 其他时候不重置, 这意味着并不是很严格地遵循"每intervalMills周期最多允许times次计数", 不过也够用了.</li>
 * </ul>
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
                    // wip起到一个自旋锁的功能, 锁内操作非常简单
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
