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

import com.alipay.common.tracer.core.appender.TracerLogRootDaemon;
import com.alipay.common.tracer.core.base.AbstractTestBase;
import com.alipay.common.tracer.core.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author khotyn 4/8/14 3:56 PM
 */
public class TimedRollingFileAppenderTest extends AbstractTestBase {
    private static final String                 ROLLING_TEST_FILE_NAME = "rolling-test.log";
    private TimedRollingFileAppender            timedRollingFileAppender;
    private PathMatchingResourcePatternResolver resolver               = new PathMatchingResourcePatternResolver();

    @Before
    public void init() throws IOException {
        timedRollingFileAppender = new TimedRollingFileAppender(ROLLING_TEST_FILE_NAME,
            AbstractRollingFileAppender.DEFAULT_BUFFER_SIZE, true, "'.'yyyy-MM-dd.HH:mm:ss");
    }

    @Test
    public void test() throws IOException, InterruptedException {
        String content = "adsfadsfadsfd" + StringUtils.NEWLINE;
        timedRollingFileAppender.append(content);
        TimeUnit.SECONDS.sleep(1);
        timedRollingFileAppender.append(content);
        timedRollingFileAppender.flush();
        Resource[] resources = resolver.getResources("file:" + TracerLogRootDaemon.LOG_FILE_DIR
                                                     + File.separator + ROLLING_TEST_FILE_NAME
                                                     + "*");
        Assert.assertTrue("文件的数量不正确，以 " + ROLLING_TEST_FILE_NAME + " 为开头的文件数量应该是 2 个",
            resources.length == 2);

        for (Resource resource : resources) {
            String c = FileUtils.readFileToString(resource.getFile());
            Assert.assertEquals("文件 " + resource.getFilename() + " 中的内容不正确", content, c);
        }
    }
}
