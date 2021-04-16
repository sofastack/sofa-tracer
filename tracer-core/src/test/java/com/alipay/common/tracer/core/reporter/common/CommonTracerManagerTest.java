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
package com.alipay.common.tracer.core.reporter.common;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.TestUtil;
import com.alipay.common.tracer.core.base.AbstractTestBase;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.reporter.type.TracerSystemLogEnum;
import com.alipay.common.tracer.core.span.CommonLogSpan;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * CommonTracerManager Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>July 25, 2017</pre>
 */
public class CommonTracerManagerTest extends AbstractTestBase {

    private SofaTracer sofaTracer;

    @Before
    public void before() {
        sofaTracer = new SofaTracer.Builder("CommonTracerManagerTest").withTag("tracer",
            "tracerTest").build();
    }

    /**
     * Method: register(String logFileName, String rollingPolicy, String logReserveDay)
     * Method: reportCommonSpan(CommonLogSpan commonLogSpan)
     */
    @Test
    public void testRegisterAndReportCommonSpan() {
        String logType = "test-register.log";
        CommonTracerManager.register(logType, "", "");
        CommonLogSpan commonLogSpan = new CommonLogSpan(this.sofaTracer,
                System.currentTimeMillis(), "testReportProfile", SofaTracerSpanContext.rootStart(),
                null);
        assertTrue(CommonTracerManager.isAppenderExist(logType));
        //Note: Be sure to set the log type for commonSpan
        commonLogSpan.setLogType(logType);
        CommonTracerManager.reportCommonSpan(commonLogSpan);

        TestUtil.periodicallyAssert(() -> {
            try {
                File file = customFileLog(logType);
                assertTrue(file.exists());

                List<String> errorContents = FileUtils.readLines(file);
                assertEquals(errorContents.toString(), 1, errorContents.size());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }, 500);
    }

    /**
     * com.alipay.common.tracer.core.reporter.common.CommonTracerManager#register(char, java.lang.String, java.lang.String, java.lang.String)
     */
    @Test
    public void testRegisterAndReportCommonSpanChar() {
        char logType = '3';
        String logTypeStr = String.valueOf(logType);
        String fileName = "test.log.char";
        //noinspection deprecation
        CommonTracerManager.register(logType, fileName, "", "");
        CommonLogSpan commonLogSpan = new CommonLogSpan(this.sofaTracer,
                System.currentTimeMillis(), "testReportProfile", SofaTracerSpanContext.rootStart(),
                null);
        assertTrue(CommonTracerManager.isAppenderExist(logTypeStr));
        //Note: Be sure to set the log type for commonSpan
        commonLogSpan.setLogType(logTypeStr);
        commonLogSpan.addSlot("hello");
        commonLogSpan.addSlot("word");
        CommonTracerManager.reportCommonSpan(commonLogSpan);

        TestUtil.periodicallyAssert(() -> {
            try {
                File file = customFileLog(fileName);
                assertTrue(file.exists());

                List<String> contents = FileUtils.readLines(file);
                assertEquals(contents.toString(), 1, contents.size());
                assertTrue(contents.get(0).contains("hello") && contents.get(0).contains("word"));
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }, 500);
    }

    /**
     * Method: reportProfile(CommonLogSpan sofaTracerSpan)
     */
    @Test
    public void testReportProfile() {
        String logType = TracerSystemLogEnum.MIDDLEWARE_ERROR.getDefaultLogName();

        CommonTracerManager.reportError(new CommonLogSpan(this.sofaTracer, System
                .currentTimeMillis(), "testReportProfile", SofaTracerSpanContext.rootStart(), null));

        TestUtil.periodicallyAssert(() -> {
            try {
                File file = customFileLog(logType);
                assertTrue(file.exists());
                List<String> errorContents = FileUtils.readLines(file);
                assertEquals(errorContents.toString(), 1, errorContents.size());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }, 500);
    }

    /**
     * Method: reportError(CommonLogSpan sofaTracerSpan)
     */
    @Test
    public void testReportError() {
        CommonTracerManager.reportProfile(new CommonLogSpan(this.sofaTracer, System
                .currentTimeMillis(), "testReportProfile", SofaTracerSpanContext.rootStart(), null));
        String logType = TracerSystemLogEnum.RPC_PROFILE.getDefaultLogName();

        TestUtil.periodicallyAssert(() -> {
            try {
                File file = customFileLog(logType);
                assertTrue(file.exists());

                List<String> profileContents = FileUtils.readLines(file);
                assertEquals(profileContents.toString(), 1, profileContents.size());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }, 500);
    }
}
