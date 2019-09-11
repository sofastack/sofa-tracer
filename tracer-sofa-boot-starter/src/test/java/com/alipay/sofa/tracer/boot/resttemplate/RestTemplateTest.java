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
package com.alipay.sofa.tracer.boot.resttemplate;

import com.alipay.common.tracer.core.appender.TracerLogRootDaemon;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.sofa.tracer.boot.TestUtil;
import com.alipay.sofa.tracer.boot.base.SpringBootWebApplication;
import com.sofa.alipay.tracer.plugins.rest.RestTemplateLogEnum;
import com.sofa.alipay.tracer.plugins.rest.SofaTracerRestTemplateBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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
public class RestTemplateTest {
    protected static String logDirectoryPath = TracerLogRootDaemon.LOG_FILE_DIR;

    @BeforeClass
    public static void beforeClass() throws IOException {
        cleanLogDirectory();
    }

    @Value("${spring.application.name}")
    private String appName;

    @Before
    public void before() throws Exception {
        // wait for resources clean
        TestUtil.waitForAsyncLog();
    }

    /**
     * test for 404
     * @throws Exception
     */
    @Test
    public void testRestTemplate404() throws Exception {
        RestTemplate restTemplate = SofaTracerRestTemplateBuilder.buildRestTemplate();
        String restUrl = "http://localhost:8888/greeting";
        try {
            restTemplate.getForEntity(restUrl, String.class);
        } catch (Exception e) {
            Assert.assertTrue(e != null);
        }

        Thread.sleep(1000);
        //wait for async output
        List<String> contents = FileUtils.readLines(new File(
            logDirectoryPath + File.separator
                    + RestTemplateLogEnum.REST_TEMPLATE_DIGEST.getDefaultLogName()));
        assertTrue(contents.size() == 1);
    }

    /**
     * clear directory
     *
     * @throws IOException
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
