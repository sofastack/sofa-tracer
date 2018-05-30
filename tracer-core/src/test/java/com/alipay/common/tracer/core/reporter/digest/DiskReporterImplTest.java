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
import com.alipay.common.tracer.core.reporter.stat.SofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracertest.encoder.ClientSpanEncoder;
import com.alipay.common.tracer.core.tracertest.type.TracerTestLogEnum;
import com.alipay.common.tracer.core.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;
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
 * @since <pre>五月 25, 2018</pre>
 */
public class DiskReporterImplTest extends AbstractTestBase {

    private String            clientLogType             = "client-log-disk-report.log";

    private String            expectRollingPolicy       = SofaTracerConfiguration
                                                            .getRollingPolicy(TracerTestLogEnum.RPC_CLIENT
                                                                .getRollingKey());

    private String            expectLogReserveConfig    = SofaTracerConfiguration
                                                            .getLogReserveConfig(TracerTestLogEnum.RPC_CLIENT
                                                                .getLogReverseKey());

    private ClientSpanEncoder expectedClientSpanEncoder = new ClientSpanEncoder();

    private DiskReporterImpl  clientReporter;

    private SofaTracerSpan    sofaTracerSpan;

    @Before
    public void before() throws Exception {
        this.clientReporter = new DiskReporterImpl(clientLogType, expectRollingPolicy,
            expectLogReserveConfig, expectedClientSpanEncoder);
        this.sofaTracerSpan = mock(SofaTracerSpan.class);
    }

    /**
     * Method: getStatReporter()
     */
    @Test
    public void testGetSetStatReporter() throws Exception {
        SofaTracerStatisticReporter statisticReporter = mock(SofaTracerStatisticReporter.class);
        this.clientReporter.setStatReporter(statisticReporter);
        assertEquals(statisticReporter, this.clientReporter.getStatReporter());
    }

    /**
     * Method: getDigestReporterType()
     */
    @Test
    public void testGetDigestReporterType() throws Exception {
        assertEquals(clientLogType, this.clientReporter.getDigestLogType());
    }

    /**
     * Method: getStatReporterType()
     */
    @Test
    public void testGetStatReporterType() throws Exception {
        assertTrue(StringUtils.isBlank(this.clientReporter.getStatReporterType()));
    }

    /**
     * Method: digestReport(SofaTracerSpan span)
     */
    @Test
    public void testDigestReport() throws Exception {
        this.clientReporter.digestReport(this.sofaTracerSpan);
        assertEquals(true, this.clientReporter.getIsDigestFileInited().get());
    }

    /**
     * Method: getDigestLogType()
     */
    @Test
    public void testGetDigestLogType() throws Exception {
        assertEquals(this.clientLogType, this.clientReporter.getDigestLogType());
    }

    /**
     * Method: getDigestRollingPolicy()
     */
    @Test
    public void testGetDigestRollingPolicy() throws Exception {
        String rollingPolicy = this.clientReporter.getDigestRollingPolicy();
        assertEquals(expectRollingPolicy, rollingPolicy);
    }

    /**
     * Method: getDigestLogReserveConfig()
     */
    @Test
    public void testGetDigestLogReserveConfig() throws Exception {
        String logReserveConfig = this.clientReporter.getDigestLogReserveConfig();
        assertEquals(expectLogReserveConfig, logReserveConfig);
    }

    /**
     * Method: getContextEncoder()
     * Method: getLogNameKey()
     */
    @Test
    public void testGetContextEncoder() throws Exception {
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
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        for (int i = 0; i < nThreads; i++) {
            Runnable worker = new WorkerInitThread(this.clientReporter, "" + i);
            executor.execute(worker);
        }
        Thread.sleep(6 * 1000);
        //未控制并发初始化时,report span 会报错;修复方法即初始化未完成时,其他线程需要等待初始化完成
        List<String> contents = FileUtils.readLines(new File(logDirectoryPath + File.separator
                                                             + "tracer-self.log"));
        assertTrue("Actual concurrent init file size = " + contents.size(), contents.size() == 1);
    }

    class WorkerInitThread implements Runnable {

        private DiskReporterImpl reporter;

        private String           command;

        public WorkerInitThread(DiskReporterImpl reporter, String s) {
            this.command = s;
            this.reporter = reporter;
        }

        @Override
        public void run() {
            processCommand();
        }

        private void processCommand() {
            SofaTracerSpan span = new SofaTracerSpan(mock(SofaTracer.class),
                System.currentTimeMillis(), "open", SofaTracerSpanContext.rootStart(), null);
            this.reporter.digestReport(span);
        }

        @Override
        public String toString() {
            return this.command;
        }
    }
}
