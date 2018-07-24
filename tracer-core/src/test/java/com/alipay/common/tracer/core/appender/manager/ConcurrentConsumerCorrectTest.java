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
import com.alipay.common.tracer.core.appender.TracerLogRootDaemon;
import com.alipay.common.tracer.core.appender.file.LoadTestAwareAppender;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author liangen
 * @version $Id: ConcurrentConsumerCorrectTest.java, v 0.1 2017年10月23日 下午3:01 liangen Exp $
 */
public class ConcurrentConsumerCorrectTest {

    static final String fileNameRoot = TracerLogRootDaemon.LOG_FILE_DIR + File.separator;
    static final String fileName1    = "log1.log";
    static final String fileName2    = "log2.log";
    static final String fileName3    = "log3.log";
    static final String fileName4    = "log4.log";
    static final String fileName5    = "log5.log";

    @Before
    public void before() throws Exception {

        cleanDir();
    }

    @After
    public void clean() throws Exception {
        cleanDir();
    }

    @Test
    public void testConcurrentConsumerCorrect() throws InterruptedException, IOException {
        /**不允许丢失日志，避免日志丢失影响结果校验的正确性*/
        SofaTracerConfiguration.setProperty(
            SofaTracerConfiguration.TRACER_ASYNC_APPENDER_ALLOW_DISCARD, "false");

        final AsyncCommonDigestAppenderManager asyncCommonDigestAppenderManager = new AsyncCommonDigestAppenderManager(
            1024);
        asyncCommonDigestAppenderManager.start("ConcurrentConsumerCorrectTest");

        ClientSpanEncoder encoder = new ClientSpanEncoder();

        TraceAppender traceAppender1 = LoadTestAwareAppender
            .createLoadTestAwareTimedRollingFileAppender(fileName1, "", "");
        TraceAppender traceAppender2 = LoadTestAwareAppender
            .createLoadTestAwareTimedRollingFileAppender(fileName2, "", "");
        TraceAppender traceAppender3 = LoadTestAwareAppender
            .createLoadTestAwareTimedRollingFileAppender(fileName3, "", "");
        TraceAppender traceAppender4 = LoadTestAwareAppender
            .createLoadTestAwareTimedRollingFileAppender(fileName4, "", "");
        TraceAppender traceAppender5 = LoadTestAwareAppender
            .createLoadTestAwareTimedRollingFileAppender(fileName5, "", "");

        asyncCommonDigestAppenderManager.addAppender("logType1", traceAppender1, encoder);
        asyncCommonDigestAppenderManager.addAppender("logType2", traceAppender2, encoder);
        asyncCommonDigestAppenderManager.addAppender("logType3", traceAppender3, encoder);
        asyncCommonDigestAppenderManager.addAppender("logType4", traceAppender4, encoder);
        asyncCommonDigestAppenderManager.addAppender("logType5", traceAppender5, encoder);

        final CountDownLatch countDownLatch = new CountDownLatch(30);
        for (int i = 0; i < 20; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    SofaTracerSpan span1 = ManagerTestUtil.createSofaTracerSpan(1);
                    for (int j = 0; j < 30; j++) {
                        asyncCommonDigestAppenderManager.append(span1);
                    }

                    SofaTracerSpan span2 = ManagerTestUtil.createSofaTracerSpan(2);
                    for (int j = 0; j < 40; j++) {
                        asyncCommonDigestAppenderManager.append(span2);
                    }

                    SofaTracerSpan span3 = ManagerTestUtil.createSofaTracerSpan(3);
                    for (int j = 0; j < 50; j++) {
                        asyncCommonDigestAppenderManager.append(span3);
                    }

                    SofaTracerSpan span4 = ManagerTestUtil.createSofaTracerSpan(4);
                    for (int j = 0; j < 60; j++) {
                        asyncCommonDigestAppenderManager.append(span4);
                    }

                    SofaTracerSpan span5 = ManagerTestUtil.createSofaTracerSpan(5);
                    for (int j = 0; j < 70; j++) {
                        asyncCommonDigestAppenderManager.append(span5);
                    }

                    countDownLatch.countDown();
                }
            }).start();
        }

        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    SofaTracerSpan span1 = ManagerTestUtil.createSofaTracerSpan(1);
                    SofaTracerSpan span2 = ManagerTestUtil.createSofaTracerSpan(2);
                    SofaTracerSpan span3 = ManagerTestUtil.createSofaTracerSpan(3);
                    SofaTracerSpan span4 = ManagerTestUtil.createSofaTracerSpan(4);
                    SofaTracerSpan span5 = ManagerTestUtil.createSofaTracerSpan(5);

                    for (int j = 0; j < 40; j++) {
                        asyncCommonDigestAppenderManager.append(span1);
                        asyncCommonDigestAppenderManager.append(span2);
                        asyncCommonDigestAppenderManager.append(span3);
                        asyncCommonDigestAppenderManager.append(span4);
                        asyncCommonDigestAppenderManager.append(span5);
                    }

                    countDownLatch.countDown();
                }
            }).start();
        }

        /**校验*/
        countDownLatch.await();
        Thread.sleep(3000);

        assertFile(fileName1, 1000, "traceID1");
        assertFile(fileName2, 1200, "traceID2");
        assertFile(fileName3, 1400, "traceID3");
        assertFile(fileName4, 1600, "traceID4");
        assertFile(fileName5, 1800, "traceID5");

    }

    public void assertFile(String fileName, int expectedNum, String expectedContent)
                                                                                    throws IOException {
        int actualNum = 0;

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
            fileNameRoot + fileName)));

        String line = null;
        while ((line = reader.readLine()) != null) {
            Assert.assertTrue(line.contains(expectedContent));
            actualNum++;
        }

        Assert.assertEquals(expectedNum, actualNum);
    }

    private void cleanDir() throws Exception {
        Thread.sleep(2000);
        File file = new File(System.getProperty("user.home") + File.separator + "logs/tracelog"
                             + File.separator + "append-manager.log");
        if (file.exists()) {
            FileUtils.writeStringToFile(file, "");
        }
        File f1 = new File(fileNameRoot + fileName1);
        if (f1.exists()) {
            FileUtils.writeStringToFile(f1, "");
        }
        File f2 = new File(fileNameRoot + fileName2);
        if (f2.exists()) {
            FileUtils.writeStringToFile(f2, "");
        }
        File f3 = new File(fileNameRoot + fileName3);
        if (f3.exists()) {
            FileUtils.writeStringToFile(f3, "");
        }
        File f4 = new File(fileNameRoot + fileName4);
        if (f4.exists()) {
            FileUtils.writeStringToFile(f4, "");
        }
        File f5 = new File(fileNameRoot + fileName5);
        if (f5.exists()) {
            FileUtils.writeStringToFile(f5, "");
        }
    }

}