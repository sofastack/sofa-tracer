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
package com.alipay.sofa.tracer.plugins.httpclient.base;

import com.alipay.common.tracer.core.appender.TracerLogRootDaemon;
import com.alipay.common.tracer.core.reporter.digest.manager.SofaTracerDigestReporterAsyncManager;
import com.alipay.common.tracer.core.reporter.stat.manager.SofaTracerStatisticReporterCycleTimesManager;
import com.alipay.sofa.tracer.plugins.httpclient.HttpClientTracer;
import com.alipay.sofa.tracer.plugins.httpclient.SofaTracerHttpClientBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * referenced document: http://docs.spring.io/spring-boot/docs/1.4.2.RELEASE/reference/htmlsingle/#boot-features-testing
 * <p>
 * <p>
 * @author yangguanchao
 * @since 2018/08/07
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBootWebApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.properties")
public abstract class AbstractTestBase {

    protected static String    logDirectoryPath = TracerLogRootDaemon.LOG_FILE_DIR;

    @LocalServerPort
    private int                definedPort;

    @Autowired
    protected TestRestTemplate testRestTemplate;

    protected String           urlHttpPrefix;

    @BeforeClass
    public static void beforeClass() throws Exception {
        cleanLogDirectory();
        reflectHttpClientClear();
    }

    @Before
    public void setUp() throws Exception {
        urlHttpPrefix = "http://localhost:" + definedPort;
    }

    /**
     * clear directory
     *
     * @throws IOException
     */
    public static void cleanLogDirectory() throws IOException {
        File file = new File(logDirectoryPath);
        if (!file.exists()) {
            return;
        }
        FileUtils.cleanDirectory(file);
    }

    protected static File customFileLog(String fileName) {
        return new File(logDirectoryPath + File.separator + fileName);
    }

    private static void reflectHttpClientClear() throws NoSuchFieldException,
                                                IllegalAccessException {
        //builder
        Field field = SofaTracerHttpClientBuilder.class.getDeclaredField("httpClientTracer");
        field.setAccessible(true);
        field.set(null, null);
        //httpClientTracer
        Field fieldTracer = HttpClientTracer.class.getDeclaredField("httpClientTracer");
        fieldTracer.setAccessible(true);
        fieldTracer.set(null, null);
        //stat
        SofaTracerStatisticReporterCycleTimesManager.getCycleTimesManager().clear();
        //clear
        Field fieldAsync = SofaTracerDigestReporterAsyncManager.class
            .getDeclaredField("asyncCommonDigestAppenderManager");
        fieldAsync.setAccessible(true);
        fieldAsync.set(null, null);
    }
}
