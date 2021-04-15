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
package com.alipay.common.tracer.core.appender;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static junit.framework.TestCase.assertEquals;

/**
 * TracerLogRootDaemonLoggingpathTest
 * @author guolei.sgl
 * @since v3.1.1
 */
public class TracerLogRootDaemonLoggingpathTest {

    @Before
    public void before() throws Exception {
        System.setProperty(TracerLogRootDaemon.TRACER_APPEND_PID_TO_LOG_PATH_KEY, "false");
        System.setProperty("logging.file.path", System.getProperty("user.home") + File.separator
                                                + "logs");
    }

    @After
    public void after() throws Exception {
        System.clearProperty(TracerLogRootDaemon.TRACER_APPEND_PID_TO_LOG_PATH_KEY);
        System.clearProperty("logging.file.path");
    }

    @Test
    public void testLogRoot() {
        assertEquals(TracerLogRootDaemon.LOG_FILE_DIR, System.getProperty("user.home")
                                                       + File.separator + "logs/tracelog");
    }

}
