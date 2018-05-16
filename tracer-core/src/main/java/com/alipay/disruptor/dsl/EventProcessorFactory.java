/*
 * Copyright 2011 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.disruptor.dsl;

import com.alipay.disruptor.EventProcessor;
import com.alipay.disruptor.RingBuffer;
import com.alipay.disruptor.Sequence;

/**
 * A factory interface to make it possible to include custom event processors in a chain:
 *
 * <pre><code>
 * disruptor.handleEventsWith(handler1).then((ringBuffer, barrierSequences) -&gt; new CustomEventProcessor(ringBuffer, barrierSequences));
 * </code></pre>
 */
public interface EventProcessorFactory<T> {

    /**
     * Create a new event processor that gates on <code>barrierSequences</code>.
     * @param ringBuffer ringBuffer
     * @param barrierSequences the sequences to gate on
     * @return a new EventProcessor that gates on <code>barrierSequences</code> before processing events
     */
    EventProcessor createEventProcessor(RingBuffer<T> ringBuffer, Sequence[] barrierSequences);
}
