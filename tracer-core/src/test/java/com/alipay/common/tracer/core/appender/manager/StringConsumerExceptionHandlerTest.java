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

import com.alipay.common.tracer.core.TestUtil;
import com.alipay.common.tracer.core.base.AbstractTestBase;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author guolei.sgl
 * @description [test unit for StringConsumerExceptionHandler]
 * @email <a href="guolei.sgl@antfin.com"></a>
 * @date 18/7/27
 */
public class StringConsumerExceptionHandlerTest extends AbstractTestBase {

    private StringConsumerExceptionHandler stringConsumerExceptionHandler;

    private StringEvent                    stringEvent;

    @Before
    public void setUp() {
        stringConsumerExceptionHandler = new StringConsumerExceptionHandler();
        stringEvent = new StringEvent();
        stringEvent.setString("test_StringEvent");
    }

    @After
    public void clean() throws IOException {
        File log = customFileLog("sync.log");
        if (log.exists()) {
            FileUtils.writeStringToFile(log, "");
        }
    }

    @Test
    public void handleEventExceptionWithEventNull() {
        stringConsumerExceptionHandler.handleEventException(new Throwable(), 2, null);
        TestUtil.periodicallyAssert(() -> {
            try {
                Assert.assertTrue(checkFileContainError());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }, 500);
    }

    @Test
    public void handleEventExceptionWithEventNotNull() {
        stringConsumerExceptionHandler.handleEventException(new Throwable(), 2, stringEvent);
        TestUtil.periodicallyAssert(() -> {
            try {
                Assert.assertTrue(checkFileContainError());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }, 500);
    }

    @Test
    public void handleOnStartException() {
        stringConsumerExceptionHandler.handleOnStartException(new Throwable());
        TestUtil.periodicallyAssert(() -> {
            try {
                Assert.assertTrue(checkFileContainError());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }, 500);
    }

    @Test
    public void handleOnShutdownException() {
        stringConsumerExceptionHandler.handleOnShutdownException(new Throwable());
        TestUtil.periodicallyAssert(() -> {
            try {
                Assert.assertTrue(checkFileContainError());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }, 500);
    }

    private boolean checkFileContainError() throws IOException {
        File log = customFileLog("sync.log");
        List<String> logs = FileUtils.readLines(log);
        return logs.get(0).contains("[ERROR]");
    }
}