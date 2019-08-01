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
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.appender.self.SynchronizingSelfLog;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author liangen
 * @version $Id: ConcurrentDiscardTest.java, v 0.1 October 23, 2017 8:22 PM liangen Exp $
 */
public class ConcurrentDiscardTest {
    static final String fileNameRoot    = TracerLogRootDaemon.LOG_FILE_DIR + File.separator;
    static final String fileName1       = "log1.log";
    static final String fileName2       = "log2.log";
    static final String fileName3       = "log3.log";
    static final String fileName4       = "log4.log";
    static final String fileName5       = "log5.log";

    int                 traceId1Discard = 0;
    int                 traceId2Discard = 0;
    int                 traceId3Discard = 0;
    int                 traceId4Discard = 0;
    int                 traceId5Discard = 0;

    File                f               = new File(fileNameRoot + "sync.log");
    File                f1              = new File(fileNameRoot + fileName1);
    File                f2              = new File(fileNameRoot + fileName2);
    File                f3              = new File(fileNameRoot + fileName3);
    File                f4              = new File(fileNameRoot + fileName4);
    File                f5              = new File(fileNameRoot + fileName5);

    @Before
    public void beforeClean() throws Exception {
        cleanDir();
    }

    @After
    public void afterClean() throws Exception {
        cleanDir();
    }

    private void cleanDir() throws Exception {
        FileUtils.writeStringToFile(f, "");
        FileUtils.deleteQuietly(f1);
        FileUtils.deleteQuietly(f2);
        FileUtils.deleteQuietly(f3);
        FileUtils.deleteQuietly(f4);
        FileUtils.deleteQuietly(f5);
    }

    @Test
    public void testConcurrentDiscard() throws InterruptedException, IOException {
        SofaTracerConfiguration.setProperty(
            SofaTracerConfiguration.TRACER_ASYNC_APPENDER_ALLOW_DISCARD, "true");
        SofaTracerConfiguration.setProperty(
            SofaTracerConfiguration.TRACER_ASYNC_APPENDER_IS_OUT_DISCARD_NUMBER, "true");
        SofaTracerConfiguration.setProperty(
            SofaTracerConfiguration.TRACER_ASYNC_APPENDER_IS_OUT_DISCARD_ID, "true");
        SofaTracerConfiguration.setProperty(
            SofaTracerConfiguration.TRACER_ASYNC_APPENDER_DISCARD_OUT_THRESHOLD, "500");

        final AsyncCommonDigestAppenderManager asyncCommonDigestAppenderManager = new AsyncCommonDigestAppenderManager(
            1024);
        asyncCommonDigestAppenderManager.start("ConcurrentDiscardTest");

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

        final AtomicInteger discardNum = new AtomicInteger(0);

        final CountDownLatch countDownLatch = new CountDownLatch(50);
        for (int i = 0; i < 50; i++) {
            new Thread(()-> {
                SofaTracerSpan span1 = ManagerTestUtil.createSofaTracerSpan(1);
                SofaTracerSpan span2 = ManagerTestUtil.createSofaTracerSpan(2);
                SofaTracerSpan span3 = ManagerTestUtil.createSofaTracerSpan(3);
                SofaTracerSpan span4 = ManagerTestUtil.createSofaTracerSpan(4);
                SofaTracerSpan span5 = ManagerTestUtil.createSofaTracerSpan(5);
                for (int j = 0; j < 100; j++) {
                    if (!asyncCommonDigestAppenderManager.append(span1)) {
                        discardNum.incrementAndGet();
                    }
                    if (!asyncCommonDigestAppenderManager.append(span2)) {
                        discardNum.incrementAndGet();
                    }
                    if (!asyncCommonDigestAppenderManager.append(span3)) {
                        discardNum.incrementAndGet();
                    }
                    if (!asyncCommonDigestAppenderManager.append(span4)) {
                        discardNum.incrementAndGet();
                    }
                    if (!asyncCommonDigestAppenderManager.append(span5)) {
                        discardNum.incrementAndGet();
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                countDownLatch.countDown();
            }).start();
        }

        countDownLatch.await();
        Thread.sleep(3000);
        SynchronizingSelfLog.flush();

        /**check*/
        int log1Num = getLineNum(fileName1);
        int log2Num = getLineNum(fileName2);
        int log3Num = getLineNum(fileName3);
        int log4Num = getLineNum(fileName4);
        int log5Num = getLineNum(fileName5);
        int allNum = log1Num + log2Num + log3Num + log4Num + log5Num;

        /**Landing log + Lost log = Print log*/
        SelfLog.info("Landing log：" + allNum);
        SelfLog.info("Lost log：" + discardNum.get());
        Assert.assertEquals(25000, allNum + discardNum.get());
        /** Sync.log lost log data is less than the actual number of lost */
        int logDiscard = getDiscardNumFromTracerSelfLog();
        SelfLog.info("Sync.log records the number of lost logs：" + logDiscard);
        Assert.assertTrue(logDiscard <= discardNum.get());
        /** Accuracy of the specific lost log data recorded by sync.log: the difference from the true lost number should be less than 500 */
        int allTraceIdDiscard = traceId1Discard + traceId2Discard + traceId3Discard
                                + traceId4Discard + traceId5Discard;
        SelfLog.info("The number of specific lost log data with traceId recorded by sync.log:" + allTraceIdDiscard);
        Assert.assertTrue((discardNum.get() == allTraceIdDiscard)
                          || (discardNum.get() - allTraceIdDiscard) < 500);

    }

    public int getLineNum(String fileName) throws IOException {
        int num = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
            fileNameRoot + fileName)));
        while (reader.readLine() != null) {
            num++;
        }
        return num;
    }

    public int getDiscardNumFromTracerSelfLog() throws IOException {
        int num = 0;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
            fileNameRoot + "sync.log")));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("discarded 500 logs")) {
                num += 500;
            }
            if (line.contains("traceId[traceID1")) {
                traceId1Discard++;
            }
            if (line.contains("traceId[traceID2")) {
                traceId2Discard++;
            }
            if (line.contains("traceId[traceID2")) {
                traceId3Discard++;
            }
            if (line.contains("traceId[traceID3")) {
                traceId4Discard++;
            }
            if (line.contains("traceId[traceID4")) {
                traceId5Discard++;
            }
        }
        return num;
    }

}