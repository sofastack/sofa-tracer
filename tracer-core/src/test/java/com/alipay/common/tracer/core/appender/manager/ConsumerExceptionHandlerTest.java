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

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.TestUtil;
import com.alipay.common.tracer.core.base.AbstractTestBase;
import com.alipay.common.tracer.core.reporter.digest.DiskReporterImpl;
import com.alipay.common.tracer.core.reporter.facade.Reporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracertest.encoder.ClientSpanEncoder;
import com.alipay.common.tracer.core.tracertest.encoder.ServerSpanEncoder;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author guolei.sgl
 * @description [test unit for ConsumerExceptionHandler]
 * @email <a href="guolei.sgl@antfin.com"></a>
 * @date 18/7/27
 */
public class ConsumerExceptionHandlerTest extends AbstractTestBase {

    private ConsumerExceptionHandler consumerExceptionHandler;
    private SofaTracerSpanEvent      sofaTracerSpanEvent;
    private SofaTracerSpan           sofaTracerSpan;

    @Before
    public void init() {
        consumerExceptionHandler = new ConsumerExceptionHandler();
        sofaTracerSpanEvent = new SofaTracerSpanEvent();
        String clientLogType = "client-log-test.log";
        Reporter clientReporter = new DiskReporterImpl(clientLogType, new ClientSpanEncoder());
        String serverLogType = "server-log-test.log";
        Reporter serverReporter = new DiskReporterImpl(serverLogType, new ServerSpanEncoder());
        String tracerType = "SofaTracerSpanTest";
        SofaTracer sofaTracer = new SofaTracer.Builder(tracerType)
            .withTag("tracer", "SofaTraceContextHolderTest").withClientReporter(clientReporter)
            .withServerReporter(serverReporter).build();
        sofaTracerSpan = (SofaTracerSpan) sofaTracer.buildSpan("SofaTracerSpanTest").start();
    }

    @After
    public void afterClean() throws IOException {
        File log = customFileLog("sync.log");
        if (log.exists()) {
            FileUtils.writeStringToFile(log, "");
        }
    }

    @Test
    public void handleEventExceptionWithEventNull() {
        consumerExceptionHandler.handleEventException(new Throwable(), 1, null);
        TestUtil.periodicallyAssert(() -> {
            try {
                File log = customFileLog("sync.log");
                List<String> logs = FileUtils.readLines(log);
                assertTrue(logs.toString(), logs.get(0).contains("[ERROR]"));
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }, 500);
    }

    @Test
    public void handleEventExceptionWithEventNotNull() {
        sofaTracerSpanEvent.setSofaTracerSpan(sofaTracerSpan);
        consumerExceptionHandler.handleEventException(new Throwable(), 1, sofaTracerSpanEvent);
        TestUtil.periodicallyAssert(() -> {
            try {
                File log = customFileLog("sync.log");
                List<String> logs = FileUtils.readLines(log);
                assertTrue(logs.toString(), logs.get(0).contains("[ERROR]"));
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }, 500);
    }

    @Test
    public void handleOnStartException() {
        consumerExceptionHandler.handleOnStartException(new Throwable());
        TestUtil.periodicallyAssert(() -> {
            try {
                File log = customFileLog("sync.log");
                List<String> logs = FileUtils.readLines(log);
                assertTrue(logs.toString(), logs.get(0).contains("[ERROR]"));
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }, 500);
    }

    @Test
    public void handleOnShutdownException() {
        consumerExceptionHandler.handleOnShutdownException(new Throwable());
        TestUtil.periodicallyAssert(() -> {
            try {
                File log = customFileLog("sync.log");
                List<String> logs = FileUtils.readLines(log);
                assertTrue(logs.toString(), logs.get(0).contains("[ERROR]"));
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }, 500);
    }
}