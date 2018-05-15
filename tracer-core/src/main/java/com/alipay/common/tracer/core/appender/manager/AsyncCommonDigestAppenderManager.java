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
import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.appender.file.LoadTestAwareAppender;
import com.alipay.common.tracer.core.appender.self.SynchronizingSelfLog;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.TracerUtils;
import com.alipay.disruptor.BlockingWaitStrategy;
import com.alipay.disruptor.EventHandler;
import com.alipay.disruptor.InsufficientCapacityException;
import com.alipay.disruptor.RingBuffer;
import com.alipay.disruptor.dsl.Disruptor;
import com.alipay.disruptor.dsl.ProducerType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author liangen
 * @version $Id: AsyncCommonDigestAppenderManager.java, v 0.1 2017年10月23日 上午9:47 liangen Exp $
 */
public class AsyncCommonDigestAppenderManager {
    private final Map<String, TraceAppender> appenders                     = new ConcurrentHashMap<String, TraceAppender>();
    private final Map<String, SpanEncoder>   contextEncoders               = new ConcurrentHashMap<String, SpanEncoder>();

    private Disruptor<SofaTracerSpanEvent>   disruptor;
    private RingBuffer<SofaTracerSpanEvent>  ringBuffer;
    private final ConsumerThreadFactory      threadFactory                 = new ConsumerThreadFactory();

    private List<Consumer>                   consumers;
    private AtomicInteger                    index                         = new AtomicInteger(0);
    private static final int                 DEFAULT_CONSUMER_NUMBER       = 3;

    private boolean                          allowDiscard;
    private boolean                          isOutDiscardNumber;
    private boolean                          isOutDiscardId;
    private long                             discardOutThreshold;
    private PaddedAtomicLong                 discardCount;

    private static final String              DEFAULT_ALLOW_DISCARD         = "true";
    private static final String              DEFAULT_IS_OUT_DISCARD_NUMBER = "true";
    private static final String              DEFAULT_IS_OUT_DISCARD_ID     = "false";

    private static final String              DEFAULT_DISCARD_OUT_THRESHOLD = "500";

    public AsyncCommonDigestAppenderManager(int queueSize, int consumerNumber) {
        int realQueueSize = 1 << (32 - Integer.numberOfLeadingZeros(queueSize - 1));
        disruptor = new Disruptor<SofaTracerSpanEvent>(new SofaTracerSpanEventFactory(),
            realQueueSize, threadFactory, ProducerType.MULTI, new BlockingWaitStrategy());

        this.consumers = new ArrayList<Consumer>(consumerNumber);

        for (int i = 0; i < consumerNumber; i++) {
            Consumer consumer = new Consumer();
            consumers.add(consumer);
            disruptor.setDefaultExceptionHandler(new ConsumerExceptionHandler());
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
    }

    public AsyncCommonDigestAppenderManager(int queueSize) {
        this(queueSize, DEFAULT_CONSUMER_NUMBER);
    }

    public void start(final String workerName) {
        this.threadFactory.setWorkName(workerName);

        this.ringBuffer = this.disruptor.start();

    }

    public void addAppender(String logType, TraceAppender appender, SpanEncoder encoder) {
        if (isAppenderOrEncoderExist(logType)) {
            SynchronizingSelfLog.error("logType[" + logType
                                       + "] already is added AsyncCommonDigestAppenderManager");
            return;
        }

        appenders.put(logType, appender);
        contextEncoders.put(logType, encoder);

        consumers.get(index.incrementAndGet() % consumers.size()).addLogType(logType);
    }

    public boolean isAppenderOrEncoderExist(String logType) {
        return appenders.containsKey(logType) || contextEncoders.containsKey(logType);
    }

    public boolean isAppenderAndEncoderExist(String logType) {
        return appenders.containsKey(logType) && contextEncoders.containsKey(logType);
    }

    public boolean isAppenderExist(Character logType) {
        return appenders.containsKey(logType);
    }

    public boolean append(SofaTracerSpan sofaTracerSpan) {
        long sequence = 0L;
        if (allowDiscard) {
            try {
                sequence = ringBuffer.tryNext();
            } catch (InsufficientCapacityException e) {

                if (isOutDiscardId) {
                    SofaTracerSpanContext sofaTracerSpanContext = sofaTracerSpan
                        .getSofaTracerSpanContext();
                    if (sofaTracerSpanContext != null) {
                        SynchronizingSelfLog.warn("discarded tracer: traceId["
                                                  + sofaTracerSpanContext.getTraceId()
                                                  + "];spanId[" + sofaTracerSpanContext.getSpanId()
                                                  + "]");
                    }
                }

                if ((isOutDiscardNumber) && discardCount.incrementAndGet() == discardOutThreshold) {
                    discardCount.set(0);
                    if (isOutDiscardNumber) {
                        SynchronizingSelfLog.warn("discarded " + discardOutThreshold + " logs");
                    }
                }

                return false;
            }
        } else {
            sequence = ringBuffer.next();
        }

        try {
            SofaTracerSpanEvent event = ringBuffer.get(sequence);
            event.setSofaTracerSpan(sofaTracerSpan);
        } catch (Exception e) {
            SynchronizingSelfLog.error("fail to add event");
            return false;
        }
        ringBuffer.publish(sequence);
        return true;
    }

    private class Consumer implements EventHandler<SofaTracerSpanEvent> {

        protected Set<String> logTypes = Collections.synchronizedSet(new HashSet<String>());

        @Override
        public void onEvent(SofaTracerSpanEvent event, long sequence, boolean endOfBatch)
                                                                                         throws Exception {

            SofaTracerSpan sofaTracerSpan = event.getSofaTracerSpan();

            if (sofaTracerSpan != null) {
                try {

                    String logType = sofaTracerSpan.getLogType();
                    if (logTypes.contains(logType)) {
                        SpanEncoder encoder = contextEncoders.get(logType);
                        TraceAppender appender = appenders.get(logType);

                        String encodedStr = encoder.encode(sofaTracerSpan);
                        if (appender instanceof LoadTestAwareAppender) {
                            ((LoadTestAwareAppender) appender).append(encodedStr,
                                TracerUtils.isLoadTest(sofaTracerSpan));
                        } else {
                            appender.append(encodedStr);
                        }
                        appender.flush();

                    }
                } catch (Exception e) {
                    SofaTracerSpanContext sofaTracerSpanContext = sofaTracerSpan
                        .getSofaTracerSpanContext();
                    if (sofaTracerSpanContext != null) {
                        SynchronizingSelfLog.error(
                            "fail to async write log,tracerId["
                                    + sofaTracerSpanContext.getTraceId() + "];spanId["
                                    + sofaTracerSpanContext.getSpanId() + "]", e);
                    } else {
                        SynchronizingSelfLog.error(
                            "fail to async write log.And the sofaTracerSpanContext is null", e);
                    }

                }
            }

        }

        public void addLogType(String logType) {
            logTypes.add(logType);
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