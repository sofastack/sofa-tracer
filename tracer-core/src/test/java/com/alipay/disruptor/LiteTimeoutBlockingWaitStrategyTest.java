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
package com.alipay.disruptor;

import com.alipay.disruptor.support.DummySequenceBarrier;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @description: [test for  LiteTimeoutBlockingWaitStrategy]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/8/1
 */
public class LiteTimeoutBlockingWaitStrategyTest {
    @Test
    public void shouldTimeoutWaitFor() throws Exception {
        final SequenceBarrier sequenceBarrier = new DummySequenceBarrier();
        long theTimeout = 500;
        LiteTimeoutBlockingWaitStrategy waitStrategy = new LiteTimeoutBlockingWaitStrategy(
            theTimeout, TimeUnit.MILLISECONDS);
        Sequence cursor = new Sequence(5);
        Sequence dependent = cursor;
        long t0 = System.currentTimeMillis();
        try {
            waitStrategy.waitFor(6, cursor, dependent, sequenceBarrier);
            fail("TimeoutException should have been thrown");
        } catch (TimeoutException e) {
        }
        long t1 = System.currentTimeMillis();
        long timeWaiting = t1 - t0;
        assertTrue(timeWaiting >= theTimeout);
    }
}