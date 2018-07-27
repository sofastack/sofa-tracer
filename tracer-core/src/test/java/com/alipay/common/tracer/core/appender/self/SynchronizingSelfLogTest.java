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
package com.alipay.common.tracer.core.appender.self;

import com.alipay.common.tracer.core.appender.TracerLogRootDaemon;
import com.alipay.common.tracer.core.base.AbstractTestBase;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @description: [描述文本]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/7/26
 */
public class SynchronizingSelfLogTest extends AbstractTestBase {

    @Before
    public void before() {

    }

    @After
    public void after() {

    }

    @Test
    public void error() throws IOException {
        SynchronizingSelfLog.error("test for SynchronizingSelfLog error situation");
        SynchronizingSelfLog.flush();
        File log = new File(TracerLogRootDaemon.LOG_FILE_DIR + File.separator
                            + SynchronizingSelfLog.SELF_LOG_FILE);
        List<String> logs = FileUtils.readLines(log);
        assertTrue(logs.toString(), logs.get(0).contains("[ERROR]"));
        assertTrue(logs.toString(),
            logs.get(0).contains("test for SynchronizingSelfLog error situation"));
        FileUtils.writeStringToFile(log, "");
    }

    @Test
    public void errorWithTraceId() throws IOException {
        SynchronizingSelfLog.errorWithTraceId("test error with tracerId", "tracerId:1234567890");
        SynchronizingSelfLog.flush();
        File log = new File(TracerLogRootDaemon.LOG_FILE_DIR + File.separator
                            + SynchronizingSelfLog.SELF_LOG_FILE);
        List<String> logs = FileUtils.readLines(log);
        assertTrue(logs.toString(), logs.get(0).contains("[ERROR]"));
        assertTrue(logs.toString(), logs.get(0).contains("[tracerId:1234567890]"));
        FileUtils.writeStringToFile(log, "");
    }

    @Test
    public void error1() throws IOException {
        SynchronizingSelfLog
            .error("test for SynchronizingSelfLog error situation", new Throwable());
        SynchronizingSelfLog.flush();
        File log = new File(TracerLogRootDaemon.LOG_FILE_DIR + File.separator
                            + SynchronizingSelfLog.SELF_LOG_FILE);
        List<String> logs = FileUtils.readLines(log);
        assertTrue(logs.toString(), logs.get(0).contains("[ERROR]"));
        assertTrue(logs.toString(),
            logs.get(0).contains("test for SynchronizingSelfLog error situation"));
        FileUtils.writeStringToFile(log, "");
    }

    @Test
    public void errorWithTraceId1() throws IOException {
        SynchronizingSelfLog.errorWithTraceId("test error with tracerId");
        SynchronizingSelfLog.flush();
        File log = new File(TracerLogRootDaemon.LOG_FILE_DIR + File.separator
                            + SynchronizingSelfLog.SELF_LOG_FILE);
        List<String> logs = FileUtils.readLines(log);
        assertTrue(logs.toString(), logs.get(0).contains("[ERROR]"));
        assertTrue(logs.toString(), logs.get(0).contains("test error with tracerId"));
        FileUtils.writeStringToFile(log, "");
    }

    @Test
    public void errorWithTraceId2() throws IOException {
        SynchronizingSelfLog.errorWithTraceId("test error with tracerId", new Throwable());
        SynchronizingSelfLog.flush();
        File log = new File(TracerLogRootDaemon.LOG_FILE_DIR + File.separator
                            + SynchronizingSelfLog.SELF_LOG_FILE);
        List<String> logs = FileUtils.readLines(log);
        assertTrue(logs.toString(), logs.get(0).contains("[ERROR]"));
        assertTrue(logs.toString(), logs.get(0).contains("test error with tracerId"));
        FileUtils.writeStringToFile(log, "");
    }

    @Test
    public void warn() throws IOException {
        SynchronizingSelfLog.warn("test warn");
        SynchronizingSelfLog.flush();
        File log = new File(TracerLogRootDaemon.LOG_FILE_DIR + File.separator
                            + SynchronizingSelfLog.SELF_LOG_FILE);
        List<String> logs = FileUtils.readLines(log);
        assertTrue(logs.toString(), logs.get(0).contains("[WARN]"));
        assertTrue(logs.toString(), logs.get(0).contains("test warn"));
        FileUtils.writeStringToFile(log, "");
    }

    @Test
    public void info() throws IOException {
        SynchronizingSelfLog.info("test info");
        SynchronizingSelfLog.flush();
        File log = new File(TracerLogRootDaemon.LOG_FILE_DIR + File.separator
                            + SynchronizingSelfLog.SELF_LOG_FILE);
        List<String> logs = FileUtils.readLines(log);
        assertTrue(logs.toString(), logs.get(0).contains("[INFO]"));
        assertTrue(logs.toString(), logs.get(0).contains("test info"));
        FileUtils.writeStringToFile(log, "");
    }

    @Test
    public void infoWithTraceId() throws IOException {
        SynchronizingSelfLog.infoWithTraceId("test info with tracerId");
        SynchronizingSelfLog.flush();
        File log = new File(TracerLogRootDaemon.LOG_FILE_DIR + File.separator
                            + SynchronizingSelfLog.SELF_LOG_FILE);
        List<String> logs = FileUtils.readLines(log);
        assertTrue(logs.toString(), logs.get(0).contains("[INFO]"));
        assertTrue(logs.toString(), logs.get(0).contains("test info with tracerId"));
        FileUtils.writeStringToFile(log, "");
    }

}