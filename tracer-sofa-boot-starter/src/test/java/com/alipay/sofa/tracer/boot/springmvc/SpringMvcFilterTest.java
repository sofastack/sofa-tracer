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
package com.alipay.sofa.tracer.boot.springmvc;

import com.alipay.common.tracer.core.appender.TracerLogRootDaemon;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.sofa.tracer.boot.TestUtil;
import com.alipay.sofa.tracer.boot.base.AbstractTestBase;
import com.alipay.sofa.tracer.boot.base.SpringBootWebApplication;
import com.alipay.sofa.tracer.boot.base.controller.SampleRestController;
import com.alipay.sofa.tracer.plugins.springmvc.SpringMvcLogEnum;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * SpringMvcFilterTest
 *
 * @author yangguanchao
 * @since 2018/05/01
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBootWebApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-non-json.properties")
public class SpringMvcFilterTest {

    protected static String    logDirectoryPath = TracerLogRootDaemon.LOG_FILE_DIR;

    @LocalServerPort
    private int                definedPort;

    @Autowired
    protected TestRestTemplate testRestTemplate;

    protected String           urlHttpPrefix;

    @BeforeClass
    public static void beforeClass() throws IOException {
        cleanLogDirectory();
    }

    @Before
    public void setUp() throws Exception {
        urlHttpPrefix = "http://localhost:" + definedPort;
        AbstractTestBase.reflectSpringMVCClear();
    }

    @Value("${spring.application.name}")
    private String appName;

    @Before
    public void before() throws Exception {
        // wait for resources clean
        TestUtil.waitForAsyncLog();
    }

    @Test
    public void testSofaRestGet() throws Exception {

        File file = customFileLog(SpringMvcLogEnum.SPRING_MVC_DIGEST.getDefaultLogName());
        if (file.exists()) {
            file.delete();
        }
        assertNotNull(testRestTemplate);
        String restUrl = urlHttpPrefix + "/greeting";

        ResponseEntity<SampleRestController.Greeting> response = testRestTemplate.getForEntity(
            restUrl, SampleRestController.Greeting.class);
        SampleRestController.Greeting greetingResponse = response.getBody();
        assertTrue(greetingResponse.isSuccess());
        // http://docs.spring.io/spring-boot/docs/1.4.2.RELEASE/reference/htmlsingle/#boot-features-testing
        assertTrue(greetingResponse.getId() >= 0);

        TestUtil.waitForAsyncLog();

        //wait for async output
        List<String> contents = FileUtils
            .readLines(customFileLog(SpringMvcLogEnum.SPRING_MVC_DIGEST.getDefaultLogName()));
        assertTrue(contents.size() == 1);

        String logAppName = contents.get(0).split(",")[1];
        assertEquals(appName, logAppName);
    }

    /**
     * clear directory
     *
     * @throws java.io.IOException
     */
    public static void cleanLogDirectory() throws IOException {
        File file = new File(logDirectoryPath);
        if (file.exists()) {
            FileUtils.cleanDirectory(file);
        }
    }

    protected static File customFileLog(String fileName) {
        return new File(logDirectoryPath + File.separator + fileName);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        clearTracerProperties();
    }

    private static void clearTracerProperties() throws Exception {
        Field propertiesField = SofaTracerConfiguration.class.getDeclaredField("properties");
        propertiesField.setAccessible(true);
        propertiesField.set(null, new ConcurrentHashMap<>());
    }
}
