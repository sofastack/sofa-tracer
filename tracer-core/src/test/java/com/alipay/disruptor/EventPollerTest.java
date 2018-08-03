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

import org.junit.Test;
import java.util.ArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @description: [test for EventPoller]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/8/1
 */
public class EventPollerTest {

    @Test
    @SuppressWarnings("unchecked")
    public void shouldPollForEvents() throws Exception {
        final Sequence gatingSequence = new Sequence();
        final SingleProducerSequencer sequencer = new SingleProducerSequencer(16,
            new BusySpinWaitStrategy());
        final EventPoller.Handler<Object> handler = new EventPoller.Handler<Object>() {
            public boolean onEvent(Object event, long sequence, boolean endOfBatch)
                                                                                   throws Exception {
                return false;
            }
        };

        final Object[] data = new Object[16];
        final DataProvider<Object> provider = new DataProvider<Object>() {
            public Object get(long sequence) {
                return data[(int) sequence];
            }
        };

        final EventPoller<Object> poller = sequencer.newPoller(provider, gatingSequence);
        final Object event = new Object();
        data[0] = event;
        assertThat(poller.poll(handler), is(EventPoller.PollState.IDLE));
        // Publish Event.
        sequencer.publish(sequencer.next());
        assertThat(poller.poll(handler), is(EventPoller.PollState.GATING));
        gatingSequence.incrementAndGet();
        assertThat(poller.poll(handler), is(EventPoller.PollState.PROCESSING));
    }

    @Test
    public void shouldSuccessfullyPollWhenBufferIsFull() throws Exception {
        final ArrayList<byte[]> events = new ArrayList<byte[]>();

        final EventPoller.Handler<byte[]> handler = new EventPoller.Handler<byte[]>() {
            public boolean onEvent(byte[] event, long sequence, boolean endOfBatch)
                                                                                   throws Exception {
                events.add(event);
                return !endOfBatch;
            }
        };

        EventFactory<byte[]> factory = new EventFactory<byte[]>() {
            @Override
            public byte[] newInstance() {
                return new byte[1];
            }
        };

        final RingBuffer<byte[]> ringBuffer = RingBuffer.createMultiProducer(factory, 4,
            new SleepingWaitStrategy());
        final EventPoller<byte[]> poller = ringBuffer.newPoller();
        ringBuffer.addGatingSequences(poller.getSequence());
        int count = 4;
        for (byte i = 1; i <= count; ++i) {
            long next = ringBuffer.next();
            ringBuffer.get(next)[0] = i;
            ringBuffer.publish(next);
        }
        // think of another thread
        poller.poll(handler);
        assertThat(events.size(), is(4));
    }
}