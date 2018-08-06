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
package com.alipay.common.tracer.core.appender.file;

import com.alipay.common.tracer.core.appender.TraceAppender;
import com.alipay.common.tracer.core.appender.TracerLogRootDaemon;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.io.File;
import java.io.IOException;

/**
 * @description: [test unit for CompositeTraceAppender]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/7/27
 */
public class CompositeTraceAppenderTest {

    private static final String                 COMPOSITE_TEST_FILE_NAME = "composite-test.log";
    private TimedRollingFileAppender            timedRollingFileAppender;
    private PathMatchingResourcePatternResolver resolver                 = new PathMatchingResourcePatternResolver();

    CompositeTraceAppender                      compositeTraceAppender;
    TraceAppender                               mockTracerAppender;

    @Before
    public void init() throws IOException {
        timedRollingFileAppender = new TimedRollingFileAppender(COMPOSITE_TEST_FILE_NAME,
            AbstractRollingFileAppender.DEFAULT_BUFFER_SIZE, true, "'.'yyyy-MM-dd.HH:mm:ss");
        compositeTraceAppender = new CompositeTraceAppender();
        mockTracerAppender = Mockito.mock(TraceAppender.class);
        compositeTraceAppender.putAppender("timedRollingFileAppender", timedRollingFileAppender);
        compositeTraceAppender.putAppender("mockTracerAppender", mockTracerAppender);
    }

    @Test
    public void getAppender() {
        TraceAppender compositeTimedRollingFileAppender = compositeTraceAppender
            .getAppender("timedRollingFileAppender");
        TraceAppender compositeMockTracerAppender = compositeTraceAppender
            .getAppender("mockTracerAppender");
        Assert.assertEquals(compositeTimedRollingFileAppender.hashCode(),
            timedRollingFileAppender.hashCode());
        Assert.assertEquals(compositeMockTracerAppender.hashCode(), mockTracerAppender.hashCode());
    }

    @Test
    public void append() throws IOException {
        compositeTraceAppender.cleanup();
        compositeTraceAppender.append("test compositeTraceAppender");
        compositeTraceAppender.flush();
        Resource[] resources = resolver.getResources("file:" + TracerLogRootDaemon.LOG_FILE_DIR
                                                     + File.separator + COMPOSITE_TEST_FILE_NAME);
        Assert.assertTrue(resources.length == 1);
        String logText = FileUtils.readFileToString(resources[0].getFile());
        Assert.assertTrue(logText.equals("test compositeTraceAppender"));
    }
}