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
import com.alipay.common.tracer.core.base.AbstractTestBase;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.reporter.type.TracerSystemLogEnum;
import com.alipay.common.tracer.core.span.CommonLogSpan;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * CommonTracerManager Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>七月 25, 2017</pre>
 */
public class CommonTracerManagerTest extends AbstractTestBase {

    private SofaTracer sofaTracer;

    @Before
    public void before() throws Exception {
        sofaTracer = new SofaTracer.Builder("CommonTracerManagerTest").withTag("tracer",
            "tracertest").build();
    }

    /**
     * Method: register(String logFileName, String rollingPolicy, String logReserveDay)
     * Method: reportCommonSpan(CommonLogSpan commonLogSpan)
     */
    @Test
    public void testRegisterAndReportCommonSpan() throws Exception {
        String logType = "test-register.log";
        CommonTracerManager.register(logType, "", "");
        CommonLogSpan commonLogSpan = new CommonLogSpan(this.sofaTracer,
            System.currentTimeMillis(), "testReportProfile", SofaTracerSpanContext.rootStart(),
            null);
        assertTrue(CommonTracerManager.isAppenderExist(logType));
        //注意：对于 commonSpan 一定要设置日志类型
        commonLogSpan.setLogType(logType);
        CommonTracerManager.reportCommonSpan(commonLogSpan);
        File file = new File(logDirectoryPath + File.separator + logType);
        assertTrue(file.exists());
        Thread.sleep(2000);
        List<String> errorContents = FileUtils.readLines(new File(logDirectoryPath + File.separator
                                                                  + logType));
        assertTrue(errorContents.toString(), errorContents.size() == 1);
    }

    /***
     * com.alipay.common.tracer.core.reporter.common.CommonTracerManager#register(char, java.lang.String, java.lang.String, java.lang.String)
     * @throws Exception
     */
    @Test
    public void testRegisterAndReportCommonSpanChar() throws Exception {
        char logType = '3';
        String logTypeStr = new String(new char[] { logType });
        String fileName = "test.log.char";
        CommonTracerManager.register(logType, fileName, "", "");
        CommonLogSpan commonLogSpan = new CommonLogSpan(this.sofaTracer,
            System.currentTimeMillis(), "testReportProfile", SofaTracerSpanContext.rootStart(),
            null);
        assertTrue(CommonTracerManager.isAppenderExist(logTypeStr));
        //注意：对于 commonSpan 一定要设置日志类型
        commonLogSpan.setLogType(logTypeStr);
        commonLogSpan.addSlot("hello");
        commonLogSpan.addSlot("word");
        CommonTracerManager.reportCommonSpan(commonLogSpan);
        Thread.sleep(2000);
        File file = new File(logDirectoryPath + File.separator + fileName);
        assertTrue("\n" + file, file.exists());
        List<String> contents = FileUtils.readLines(new File(logDirectoryPath + File.separator
                                                             + fileName));
        assertTrue(contents.toString(), contents.size() == 1);
        assertTrue(contents.get(0).contains("hello") && contents.get(0).contains("word"));

    }

    /**
     * Method: reportProfile(CommonLogSpan sofaTracerSpan)
     */
    @Test
    public void testReportProfile() throws Exception {

        String logType = TracerSystemLogEnum.MIDDLEWARE_ERROR.getDefaultLogName();

        int initSize = 0;
        File f = new File(logDirectoryPath + File.separator + logType);
        if (f.exists()) {
            initSize = FileUtils.readLines(f).size();
        }

        CommonTracerManager.reportError(new CommonLogSpan(this.sofaTracer, System
            .currentTimeMillis(), "testReportProfile", SofaTracerSpanContext.rootStart(), null));
        File file = new File(logDirectoryPath + File.separator + logType);
        assertTrue("\n" + file, file.exists());
        Thread.sleep(2000);
        List<String> errorContents = FileUtils.readLines(new File(logDirectoryPath + File.separator
                                                                  + logType));
        assertTrue(errorContents.toString(), errorContents.size() - initSize == 1);
    }

    /**
     * Method: reportError(CommonLogSpan sofaTracerSpan)
     */
    @Test
    public void testReportError() throws Exception {
        CommonTracerManager.reportProfile(new CommonLogSpan(this.sofaTracer, System
            .currentTimeMillis(), "testReportProfile", SofaTracerSpanContext.rootStart(), null));
        String logType = TracerSystemLogEnum.RPC_PROFILE.getDefaultLogName();
        File file = new File(logDirectoryPath + File.separator + logType);
        assertTrue("\n" + file, file.exists());
        Thread.sleep(2000);
        List<String> profileContents = FileUtils.readLines(new File(logDirectoryPath
                                                                    + File.separator + logType));
        assertTrue(profileContents.toString(), profileContents.size() == 1);
    }

}
