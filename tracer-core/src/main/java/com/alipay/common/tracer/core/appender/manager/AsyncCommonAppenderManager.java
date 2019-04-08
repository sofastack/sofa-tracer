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
package com.alipay.common.tracer.core.appender.manager;

import com.alipay.common.tracer.core.appender.TraceAppender;
import com.alipay.common.tracer.core.appender.file.TimedRollingFileAppender;
import com.alipay.common.tracer.core.appender.self.SynchronizingSelfLog;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.disruptor.BlockingWaitStrategy;
import com.alipay.disruptor.EventHandler;
import com.alipay.disruptor.InsufficientCapacityException;
import com.alipay.disruptor.RingBuffer;
import com.alipay.disruptor.dsl.Disruptor;
import com.alipay.disruptor.dsl.ProducerType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * For SelfLog
 * @author luoguimu123
 * @version $Id: AsyncCommonAppenderManager.java, v 0.1 November 21, 2017 6:15 PM luoguimu123 Exp $
 */
public class AsyncCommonAppenderManager {

    private final TraceAppender         appender;

    private Disruptor<StringEvent>      disruptor;
    private RingBuffer<StringEvent>     ringBuffer;
    private final ConsumerThreadFactory threadFactory                 = new ConsumerThreadFactory();

    private List<Consumer>              consumers;

    /** Dedicated for SelfLog, no need to open three Consumers */
    private static final int            DEFAULT_CONSUMER_NUMBER       = 1;

    private boolean                     allowDiscard;
    private boolean                     isOutDiscardNumber;
    private boolean                     isOutDiscardId;
    private long                        discardOutThreshold;
    private PaddedAtomicLong            discardCount;

    private static final String         DEFAULT_ALLOW_DISCARD         = "true";
    private static final String         DEFAULT_IS_OUT_DISCARD_NUMBER = "true";
    private static final String         DEFAULT_IS_OUT_DISCARD_ID     = "false";

    private static final String         DEFAULT_DISCARD_OUT_THRESHOLD = "500";

    public AsyncCommonAppenderManager(int queueSize, int consumerNumber, String logName) {
        int realQueueSize = 1 << (32 - Integer.numberOfLeadingZeros(queueSize - 1));
        disruptor = new Disruptor<StringEvent>(new StringEventFactory(), realQueueSize,
            threadFactory, ProducerType.MULTI, new BlockingWaitStrategy());

        this.consumers = new ArrayList<Consumer>(consumerNumber);

        for (int i = 0; i < consumerNumber; i++) {
            Consumer consumer = new Consumer();
            consumers.add(consumer);
            disruptor.setDefaultExceptionHandler(new StringConsumerExceptionHandler());
            disruptor.handleEventsWith(consumer);
        }

        this.allowDiscard = Boolean.parseBoolean(SofaTracerConfiguration.getProperty(
            SofaTracerConfiguration.TRACER_ASYNC_APPENDER_ALLOW_DISCARD, DEFAULT_ALLOW_DISCARD));
        if (allowDiscard) {
            this.isOutDiscardNumber = Boolean.parseBoolean(SofaTracerConfiguration.getProperty(
                SofaTracerConfiguration.TRACER_ASYNC_APPENDER_IS_OUT_DISCARD_NUMBER,
                DEFAULT_IS_OUT_DISCARD_NUMBER));
            this.isOutDiscardId = Boolean.parseBoolean(SofaTracerConfiguration.getProperty(
                SofaTracerConfiguration.TRACER_ASYNC_APPENDER_IS_OUT_DISCARD_ID,
                DEFAULT_IS_OUT_DISCARD_ID));
            this.discardOutThreshold = Long.parseLong(SofaTracerConfiguration.getProperty(
                SofaTracerConfiguration.TRACER_ASYNC_APPENDER_DISCARD_OUT_THRESHOLD,
                DEFAULT_DISCARD_OUT_THRESHOLD));

            if (isOutDiscardNumber) {
                this.discardCount = new PaddedAtomicLong(0L);
            }
        }

        String globalLogReserveDay = SofaTracerConfiguration.getProperty(
            SofaTracerConfiguration.TRACER_GLOBAL_LOG_RESERVE_DAY,
            String.valueOf(SofaTracerConfiguration.DEFAULT_LOG_RESERVE_DAY));
        String rollingPolicy = SofaTracerConfiguration
            .getProperty(SofaTracerConfiguration.TRACER_GLOBAL_ROLLING_KEY);

        if (StringUtils.isBlank(rollingPolicy)) {
            rollingPolicy = TimedRollingFileAppender.DAILY_ROLLING_PATTERN;
        }

        this.appender = new TimedRollingFileAppender(logName, rollingPolicy,
            String.valueOf(globalLogReserveDay));
    }

    public AsyncCommonAppenderManager(int queueSize, String logName) {
        this(queueSize, DEFAULT_CONSUMER_NUMBER, logName);
    }

    public void start(final String workerName) {
        this.threadFactory.setWorkName(workerName);

        this.ringBuffer = this.disruptor.start();

    }

    public boolean append(String string) {
        long sequence = 0L;
        if (allowDiscard) {
            try {
                sequence = ringBuffer.tryNext();
            } catch (InsufficientCapacityException e) {

                if (isOutDiscardId) {
                    if (string != null) {
                        SynchronizingSelfLog.warn("discarded selflog ");
                    }
                }

                if ((isOutDiscardNumber) && discardCount.incrementAndGet() == discardOutThreshold) {
                    discardCount.set(0);
                    if (isOutDiscardNumber) {
                        SynchronizingSelfLog.warn("discarded " + discardOutThreshold + " selflogs");
                    }
                }

                return false;
            }
        } else {
            sequence = ringBuffer.next();
        }

        try {
            StringEvent event = ringBuffer.get(sequence);
            event.setString(string);
        } catch (Exception e) {
            SynchronizingSelfLog.error("fail to add event");
            return false;
        }
        ringBuffer.publish(sequence);
        return true;
    }

    private class Consumer implements EventHandler<StringEvent> {

        @Override
        public void onEvent(StringEvent event, long sequence, boolean endOfBatch) throws Exception {
            String string = event.getString();
            if (string != null) {
                try {
                    appender.append(string);
                    appender.flush();
                } catch (Exception e) {
                    //todo Globally keep a synchronized log for synchronizing print logs at some key points
                    if (string != null) {
                        SynchronizingSelfLog.error("fail to async write log", e);
                    } else {
                        SynchronizingSelfLog.error(
                            "fail to async write log.And the sofaTracerSpanContext is null", e);
                    }

                }
            }

        }

    }

    class PaddedAtomicLong extends AtomicLong {
        public volatile long p1, p2, p3, p4, p5, p6 = 7L;

        public PaddedAtomicLong(long initialValue) {
            super(initialValue);
        }

        public PaddedAtomicLong() {
        }
    }
}