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
package com.alipay.common.tracer.core.reporter.digest.event;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.base.AbstractTestBase;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracertest.encoder.ClientSpanEventEncoder;
import com.alipay.common.tracer.core.tracertest.type.TracerTestLogEnum;
import com.alipay.common.tracer.core.utils.StringUtils;
import io.opentracing.tag.Tags;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author yuqian
 * @version : SpanEventDiskReporterTest.java, v 0.1 2025-03-10 16:59 yuqian Exp $$
 */
public class SpanEventDiskReporterTest extends AbstractTestBase {

    private final String                 eventLogType           = TracerTestLogEnum.RPC_CLIENT_EVENT
                                                                    .getDefaultLogName();

    private final String                 expectRollingPolicy    = SofaTracerConfiguration
                                                                    .getRollingPolicy(TracerTestLogEnum.RPC_CLIENT_EVENT
                                                                        .getRollingKey());

    private final String                 expectLogReserveConfig = SofaTracerConfiguration
                                                                    .getLogReserveConfig(TracerTestLogEnum.RPC_CLIENT_EVENT
                                                                        .getLogReverseKey());

    private final ClientSpanEventEncoder clientSpanEventEncoder = new ClientSpanEventEncoder();

    private SpanEventDiskReporter        spanEventDiskReporter;

    private SofaTracerSpan               sofaTracerSpan;

    @Before
    public void before() {
        this.spanEventDiskReporter = new SpanEventDiskReporter(eventLogType, expectRollingPolicy,
            expectLogReserveConfig, clientSpanEventEncoder, null);
        this.sofaTracerSpan = mock(SofaTracerSpan.class);
    }

    @Test
    public void testGetDigestReporterType() {
        assertEquals(eventLogType, this.spanEventDiskReporter.getEventLogType());
    }

    @Test
    public void testDigestReport() throws InterruptedException {
        this.spanEventDiskReporter.digestReport(this.sofaTracerSpan);
        assertTrue(this.spanEventDiskReporter.getIsEventFileInited().get());
    }

    @Test
    public void testGetDigestLogType() {
        assertEquals(this.eventLogType, this.spanEventDiskReporter.getEventLogType());
    }

    @Test
    public void testGetDigestRollingPolicy() {
        String rollingPolicy = this.spanEventDiskReporter.getEventRollingPolicy();
        assertEquals(expectRollingPolicy, rollingPolicy);
    }

    @Test
    public void testGetDigestLogReserveConfig() {
        String logReserveConfig = this.spanEventDiskReporter.getEventLogReserveConfig();
        assertEquals(expectLogReserveConfig, logReserveConfig);
    }

    @Test
    public void testGetContextEncoder() {
        assertEquals(clientSpanEventEncoder, this.spanEventDiskReporter.getContextEncoder());
        String logNameKey = this.spanEventDiskReporter.getLogNameKey();
        assertTrue(StringUtils.isBlank(logNameKey));
    }

    @Test
    public void testFixInitDigestFile() throws Exception {
        //should be only one item log
        SelfLog.warn("SelfLog init success!!!");
        int nThreads = 30;
        ExecutorService executor = new ThreadPoolExecutor(nThreads, nThreads, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        CountDownLatch countDownLatch = new CountDownLatch(nThreads);
        for (int i = 0; i < nThreads; i++) {
            Runnable worker = new WorkerInitThread(this.spanEventDiskReporter, "" + i,
                countDownLatch);
            executor.execute(worker);
        }
        //noinspection ResultOfMethodCallIgnored
        countDownLatch.await(3, TimeUnit.SECONDS);
        // When there is no control for concurrent initialization, report span will get an error;
        // when the repair method is initialized,other threads need to wait for initialization to complete.
        List<String> contents = FileUtils.readLines(tracerSelfLog());
        assertEquals("Actual concurrent init file size = " + contents.size(), 1, contents.size());
    }

    static class WorkerInitThread implements Runnable {
        private final SpanEventDiskReporter reporter;
        private final String                command;
        private final CountDownLatch        countDownLatch;

        public WorkerInitThread(SpanEventDiskReporter reporter, String s,
                                CountDownLatch countDownLatch) {
            this.command = s;
            this.reporter = reporter;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            processCommand();
            countDownLatch.countDown();
        }

        private void processCommand() {
            SofaTracerSpan span = new SofaTracerSpan(mock(SofaTracer.class),
                System.currentTimeMillis(), "open", SofaTracerSpanContext.rootStart(), null);
            span.setTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
            this.reporter.digestReport(span);
        }

        @Override
        public String toString() {
            return this.command;
        }
    }
}