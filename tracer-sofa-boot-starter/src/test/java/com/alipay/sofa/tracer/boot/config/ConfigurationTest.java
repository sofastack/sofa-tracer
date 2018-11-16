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
package com.alipay.sofa.tracer.boot.config;

import java.io.File;
import java.util.Map;

import com.alipay.common.tracer.core.appender.TracerLogRootDaemon;
import com.alipay.common.tracer.core.appender.info.StaticInfoLog;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.common.tracer.core.utils.TracerUtils;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alipay.common.tracer.core.appender.file.TimedRollingFileAppender;
import com.alipay.sofa.tracer.boot.base.ConfigurationHolder;
import com.alipay.sofa.tracer.boot.base.SpringBootWebApplication;
import com.alipay.sofa.tracer.boot.properties.SofaTracerProperties;

import static com.alipay.common.tracer.core.appender.TracerLogRootDaemon.TRACER_APPEND_PID_TO_LOG_PATH_KEY;

/**
 * @author qilong.zql
 * @since 2.2.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBootWebApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.properties")
@ActiveProfiles("config")
public class ConfigurationTest {

    private String oldPath;

    @BeforeClass
    public static void before() {
        System.getProperties().remove("logging.path");
        File defaultDir = new File(System.getProperty("user.home") + File.separator + "logs"
                                   + File.separator + "tracelog");
        File configDir = new File(System.getProperty("user.dir") + File.separator + "logs"
                                  + File.separator + "tracelog");
        if (defaultDir.exists()) {
            FileUtils.deleteQuietly(defaultDir);
        }
        if (configDir.exists()) {
            FileUtils.deleteQuietly(configDir);
        }
    }

    @Before
    public void beforeMethod() {
        oldPath = TracerLogRootDaemon.LOG_FILE_DIR;
        String loggingRoot = System.getProperty("loggingRoot");
        if (StringUtils.isBlank(loggingRoot)) {
            loggingRoot = System.getProperty("logging.path");
        }

        String appendPidToLogPathString = System.getProperty(TRACER_APPEND_PID_TO_LOG_PATH_KEY);
        boolean appendPidToLogPath = "true".equalsIgnoreCase(appendPidToLogPathString);

        if (StringUtils.isBlank(loggingRoot)) {
            loggingRoot = System.getProperty("user.home") + File.separator + "logs";
        }

        String tempLogFileDir = loggingRoot + File.separator + "tracelog";

        if (appendPidToLogPath) {
            tempLogFileDir = tempLogFileDir + File.separator + TracerUtils.getPID();
        }
        TracerLogRootDaemon.LOG_FILE_DIR = tempLogFileDir;
    }

    @After
    public void afterMethod() {
        TracerLogRootDaemon.LOG_FILE_DIR = oldPath;
    }

    @Test
    public void testAdvanceTracerConfig() {
        SofaTracerProperties sofaTracerProperties = ConfigurationHolder.getSofaTracerProperties();
        Assert.assertEquals("true", sofaTracerProperties.getDisableDigestLog());

        Map<String, String> disableConfiguration = sofaTracerProperties.getDisableConfiguration();
        Assert.assertTrue("v1".equals(disableConfiguration.get("k1")));
        Assert.assertTrue("v2".equals(disableConfiguration.get("k2")));
        Assert.assertEquals(2, disableConfiguration.size());

        Assert.assertEquals(TimedRollingFileAppender.HOURLY_ROLLING_PATTERN,
            sofaTracerProperties.getTracerGlobalRollingPolicy());
        Assert.assertEquals("1", sofaTracerProperties.getTracerGlobalLogReserveDay());
        Assert.assertEquals("1", sofaTracerProperties.getStatLogInterval());
        Assert.assertEquals("1", sofaTracerProperties.getBaggageMaxLength());
    }

    @Test
    public void testTracerLogDir() throws Throwable {
        StaticInfoLog.logStaticInfo();
        File defaultDir = new File(System.getProperty("user.home") + File.separator + "logs"
                                   + File.separator + "tracelog");
        File configDir = new File(System.getProperty("user.dir") + File.separator + "logs"
                                  + File.separator + "tracelog");
        Assert.assertFalse(defaultDir.exists());
        Assert.assertTrue(configDir.exists());
    }

}