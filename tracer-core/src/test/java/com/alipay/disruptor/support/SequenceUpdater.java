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
package com.alipay.disruptor.support;

import com.alipay.disruptor.Sequence;
import com.alipay.disruptor.WaitStrategy;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @description: [test for SequenceUpdater]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/8/1
 */
public class SequenceUpdater implements Runnable {

    public final Sequence       sequence = new Sequence();
    private final CyclicBarrier barrier  = new CyclicBarrier(2);
    private final long          sleepTime;
    private WaitStrategy        waitStrategy;

    SequenceUpdater(long sleepTime, WaitStrategy waitStrategy) {
        this.sleepTime = sleepTime;
        this.waitStrategy = waitStrategy;
    }

    @Override
    public void run() {
        try {
            barrier.await();
            if (0 != sleepTime) {
                Thread.sleep(sleepTime);
            }
            sequence.incrementAndGet();
            waitStrategy.signalAllWhenBlocking();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void waitForStartup() throws InterruptedException, BrokenBarrierException {
        barrier.await();
    }
}
