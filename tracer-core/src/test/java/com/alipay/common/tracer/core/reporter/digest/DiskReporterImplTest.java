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
package com.alipay.common.tracer.core.reporter.digest;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.base.AbstractTestBase;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.reporter.stat.SofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracertest.encoder.ClientSpanEncoder;
import com.alipay.common.tracer.core.tracertest.type.TracerTestLogEnum;
import com.alipay.common.tracer.core.utils.StringUtils;
import io.opentracing.tag.Tags;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * DiskReporterImpl Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>May 25, 2018</pre>
 */
public class DiskReporterImplTest extends AbstractTestBase {

    private final String            clientLogType             = "client-log-disk-report.log";

    private final String            expectRollingPolicy       = SofaTracerConfiguration
                                                                  .getRollingPolicy(TracerTestLogEnum.RPC_CLIENT
                                                                      .getRollingKey());

    private final String            expectLogReserveConfig    = SofaTracerConfiguration
                                                                  .getLogReserveConfig(TracerTestLogEnum.RPC_CLIENT
                                                                      .getLogReverseKey());

    private final ClientSpanEncoder expectedClientSpanEncoder = new ClientSpanEncoder();

    private DiskReporterImpl        clientReporter;

    private SofaTracerSpan          sofaTracerSpan;

    @Before
    public void before() {
        this.clientReporter = new DiskReporterImpl(clientLogType, expectRollingPolicy,
            expectLogReserveConfig, expectedClientSpanEncoder);
        this.sofaTracerSpan = mock(SofaTracerSpan.class);
    }

    /**
     * Method: getStatReporter()
     */
    @Test
    public void testGetSetStatReporter() {
        SofaTracerStatisticReporter statisticReporter = mock(SofaTracerStatisticReporter.class);
        this.clientReporter.setStatReporter(statisticReporter);
        assertEquals(statisticReporter, this.clientReporter.getStatReporter());
    }

    /**
     * Method: getDigestReporterType()
     */
    @Test
    public void testGetDigestReporterType() {
        assertEquals(clientLogType, this.clientReporter.getDigestLogType());
    }

    /**
     * Method: getStatReporterType()
     */
    @Test
    public void testGetStatReporterType() {
        assertTrue(StringUtils.isBlank(this.clientReporter.getStatReporterType()));
    }

    @Test
    public void testGetStatReporterTypeNotNull() {
        AbstractDiskReporter diskReporter = new DiskReporterImpl(clientLogType,
            expectRollingPolicy, expectLogReserveConfig, expectedClientSpanEncoder,
            new AbstractSofaTracerStatisticReporter("testTracer", "", "") {
                @Override
                public void doReportStat(SofaTracerSpan sofaTracerSpan) {

                }
            });
        Assert.assertNotNull(diskReporter.getStatReporterType());
    }

    @Test
    public void testStatisticReport() {
        final String[] str = { "" };
        AbstractDiskReporter diskReporter = new DiskReporterImpl(clientLogType,
            expectRollingPolicy, expectLogReserveConfig, expectedClientSpanEncoder,
            new AbstractSofaTracerStatisticReporter("testTracer", "", "") {
                @Override
                public void doReportStat(SofaTracerSpan sofaTracerSpan) {
                    str[0] = "hello";
                }
            });
        SofaTracer sofaTracer = mock(SofaTracer.class);
        SofaTracerSpanContext sofaTracerSpanContext = new SofaTracerSpanContext();
        SofaTracerSpan sofaTracerSpan = new SofaTracerSpan(sofaTracer, System.currentTimeMillis(),
            "mock", sofaTracerSpanContext, new HashMap<>());
        diskReporter.statisticReport(sofaTracerSpan);
        Assert.assertEquals("hello", str[0]);
    }

    /**
     * Method: digestReport(SofaTracerSpan span)
     */
    @Test
    public void testDigestReport() {
        this.clientReporter.digestReport(this.sofaTracerSpan);
        assertTrue(this.clientReporter.getIsDigestFileInited().get());
    }

    /**
     * Method: getDigestLogType()
     */
    @Test
    public void testGetDigestLogType() {
        assertEquals(this.clientLogType, this.clientReporter.getDigestLogType());
    }

    /**
     * Method: getDigestRollingPolicy()
     */
    @Test
    public void testGetDigestRollingPolicy() {
        String rollingPolicy = this.clientReporter.getDigestRollingPolicy();
        assertEquals(expectRollingPolicy, rollingPolicy);
    }

    /**
     * Method: getDigestLogReserveConfig()
     */
    @Test
    public void testGetDigestLogReserveConfig() {
        String logReserveConfig = this.clientReporter.getDigestLogReserveConfig();
        assertEquals(expectLogReserveConfig, logReserveConfig);
    }

    /**
     * Method: getContextEncoder()
     * Method: getLogNameKey()
     */
    @Test
    public void testGetContextEncoder() {
        assertEquals(expectedClientSpanEncoder, this.clientReporter.getContextEncoder());
        String logNameKey = this.clientReporter.getLogNameKey();
        assertTrue(StringUtils.isBlank(logNameKey));
    }

    /**
     * Method: initDigestFile()
     * fix Concurrent
     */
    @Test
    public void testFixInitDigestFile() throws Exception {
        //should be only one item log
        SelfLog.warn("SelfLog init success!!!");
        int nThreads = 30;
        ExecutorService executor = new ThreadPoolExecutor(nThreads, nThreads, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        CountDownLatch countDownLatch = new CountDownLatch(nThreads);
        for (int i = 0; i < nThreads; i++) {
            Runnable worker = new WorkerInitThread(this.clientReporter, "" + i, countDownLatch);
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
        private final DiskReporterImpl reporter;
        private final String           command;
        private final CountDownLatch   countDownLatch;

        public WorkerInitThread(DiskReporterImpl reporter, String s, CountDownLatch countDownLatch) {
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
