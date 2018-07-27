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

import com.alipay.common.tracer.core.appender.TracerLogRootDaemon;
import com.alipay.common.tracer.core.base.AbstractTestBase;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @description: [描述文本]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/7/27
 */
public class StringConsumerExceptionHandlerTest extends AbstractTestBase {

    private StringConsumerExceptionHandler stringConsumerExceptionHandler;

    private StringEvent                    stringEvent;

    @Before
    public void setUp() throws Exception {
        stringConsumerExceptionHandler = new StringConsumerExceptionHandler();
        stringEvent = new StringEvent();
        stringEvent.setString("test_StringEvent");
    }

    @Test
    public void handleEventException_with_event_null() throws IOException {
        stringConsumerExceptionHandler.handleEventException(new Throwable(), 2, null);
        File log = new File(TracerLogRootDaemon.LOG_FILE_DIR + File.separator + "sync.log");
        List<String> logs = FileUtils.readLines(log);
        assertTrue(logs.toString(), logs.get(0).contains("[ERROR]"));
        assertTrue(logs.toString(),
            logs.get(0).contains("AsyncConsumer occurs exception during handle StringEvent"));
        FileUtils.writeStringToFile(log, "");
    }

    @Test
    public void handleEventException_with_event_not_null() throws IOException {
        stringConsumerExceptionHandler.handleEventException(new Throwable(), 2, stringEvent);
        File log = new File(TracerLogRootDaemon.LOG_FILE_DIR + File.separator + "sync.log");
        List<String> logs = FileUtils.readLines(log);
        assertTrue(logs.toString(), logs.get(0).contains("[ERROR]"));
        assertTrue(logs.toString(), logs.get(0).contains(stringEvent.getString()));
        FileUtils.writeStringToFile(log, "");
    }

    @Test
    public void handleOnStartException() throws IOException {
        stringConsumerExceptionHandler.handleOnStartException(new Throwable());
        File log = new File(TracerLogRootDaemon.LOG_FILE_DIR + File.separator + "sync.log");
        List<String> logs = FileUtils.readLines(log);
        assertTrue(logs.toString(), logs.get(0).contains("[ERROR]"));
        assertTrue(logs.toString(), logs.get(0).contains("AsyncConsumer occurs exception on start"));
        FileUtils.writeStringToFile(log, "");

    }

    @Test
    public void handleOnShutdownException() throws IOException {
        stringConsumerExceptionHandler.handleOnShutdownException(new Throwable());
        File log = new File(TracerLogRootDaemon.LOG_FILE_DIR + File.separator + "sync.log");
        List<String> logs = FileUtils.readLines(log);
        assertTrue(logs.toString(), logs.get(0).contains("[ERROR]"));
        assertTrue(logs.toString(),
            logs.get(0).contains("Disruptor or AsyncConsumer occurs exception on shutdown"));
        FileUtils.writeStringToFile(log, "");
    }
}