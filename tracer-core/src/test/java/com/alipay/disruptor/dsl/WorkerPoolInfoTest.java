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
package com.alipay.disruptor.dsl;

import com.alipay.common.tracer.core.appender.manager.ConsumerThreadFactory;
import com.alipay.common.tracer.core.appender.manager.StringEvent;
import com.alipay.common.tracer.core.appender.manager.StringEventFactory;
import com.alipay.disruptor.*;
import com.alipay.disruptor.util.DaemonThreadFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @description: [test WorkerPoolInfo]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/8/1
 */
public class WorkerPoolInfoTest {
    private WorkerPool<StringEvent>     workerPool;
    private SequenceBarrier             sequenceBarrier;
    private RingBuffer<StringEvent>     ringBuffer;
    private Disruptor<StringEvent>      disruptor;
    private final ConsumerThreadFactory threadFactory = new ConsumerThreadFactory();
    private WorkerPoolInfo              workerPoolInfo;
    Executor                            executor;

    @Before
    public void init() {
        executor = Executors.newCachedThreadPool(DaemonThreadFactory.INSTANCE);
        workerPool = new WorkerPool<StringEvent>(new WorkerPoolInfoTest.StringEventEventFactory(),
            new FatalExceptionHandler(), new WorkerPoolInfoTest.StringEventWorkHandler());
        disruptor = new Disruptor<StringEvent>(new StringEventFactory(), 64, threadFactory,
            ProducerType.MULTI, new BlockingWaitStrategy());
        ringBuffer = disruptor.start();
        sequenceBarrier = ringBuffer.newBarrier();
        workerPoolInfo = new WorkerPoolInfo(workerPool, sequenceBarrier);
    }

    @Test
    public void getSequences() {
        //WorkProcessor size +1  -> StringEventWorkHandler ; there is 2
        Sequence[] sequences = workerPoolInfo.getSequences();
        Assert.assertTrue(sequences.length == 2);
    }

    @Test
    public void isEndOfChain() {
        Assert.assertTrue(workerPoolInfo.isEndOfChain());
    }

    private static class StringEventWorkHandler implements WorkHandler<StringEvent> {
        @Override
        public void onEvent(StringEvent event) throws Exception {
            event.setString("log-test");
        }
    }

    private static class StringEventEventFactory implements EventFactory<StringEvent> {
        @Override
        public StringEvent newInstance() {
            return new StringEvent();
        }
    }

}