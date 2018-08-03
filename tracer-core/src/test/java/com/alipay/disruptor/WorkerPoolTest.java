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

import com.alipay.disruptor.util.DaemonThreadFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import static org.hamcrest.CoreMatchers.is;

/**
 * @description: [test for WorkerPool]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/7/31
 */
public class WorkerPoolTest {

    @Test
    public void test() throws InterruptedException {
        Executor executor = Executors.newCachedThreadPool(DaemonThreadFactory.INSTANCE);
        WorkerPool<AtomicLong> pool = new WorkerPool<AtomicLong>(new AtomicLongEventFactory(),
            new FatalExceptionHandler(), new AtomicLongWorkHandler(), new AtomicLongWorkHandler());
        RingBuffer<AtomicLong> ringBuffer = pool.start(executor);
        ringBuffer.next();
        ringBuffer.next();
        ringBuffer.publish(0);
        ringBuffer.publish(1);
        Thread.sleep(500);
        Assert.assertThat(ringBuffer.get(0).get(), is(1L));
        Assert.assertThat(ringBuffer.get(1).get(), is(1L));
    }

    private static class AtomicLongWorkHandler implements WorkHandler<AtomicLong> {
        @Override
        public void onEvent(AtomicLong event) throws Exception {
            event.incrementAndGet();
        }
    }

    private static class AtomicLongEventFactory implements EventFactory<AtomicLong> {
        @Override
        public AtomicLong newInstance() {
            return new AtomicLong(0);
        }
    }
}