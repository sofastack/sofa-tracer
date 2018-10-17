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
package com.sofa.tracer.plugins.resttemplate.base;

import com.alipay.common.tracer.core.reporter.digest.manager.SofaTracerDigestReporterAsyncManager;
import com.alipay.common.tracer.core.reporter.stat.manager.SofaTracerStatisticReporterCycleTimesManager;
import com.sofa.alipay.tracer.plugins.rest.SofaTracerRestTemplateBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
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
    protected static String logDirectoryPath = System.getProperty("user.home") + File.separator
                                               + "logs" + File.separator + "tracelog";

    private static File     logDirectory     = new File(logDirectoryPath);
    @LocalServerPort
    private int             definedPort;

    protected String        urlHttpPrefix;

    @BeforeClass
    public static void beforeClass() throws Exception {
        cleanLogDirectory();
        resetCondition();
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
        if (!logDirectory.exists()) {
            return;
        }
        FileUtils.cleanDirectory(logDirectory);
    }

    private static void resetCondition() throws NoSuchFieldException, IllegalAccessException {
        // set restTemplateTracer as null
        Field field = SofaTracerRestTemplateBuilder.class.getDeclaredField("restTemplateTracer");
        field.setAccessible(true);
        field.set(null, null);
        //stat
        SofaTracerStatisticReporterCycleTimesManager.getCycleTimesManager().clear();
        //clear
        Field fieldAsync = SofaTracerDigestReporterAsyncManager.class
            .getDeclaredField("asyncCommonDigestAppenderManager");
        fieldAsync.setAccessible(true);
        fieldAsync.set(null, null);
    }
}
