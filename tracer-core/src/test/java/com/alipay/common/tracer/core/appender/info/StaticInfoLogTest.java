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
package com.alipay.common.tracer.core.appender.info;

import com.alipay.common.tracer.core.TestUtil;
import com.alipay.common.tracer.core.appender.TraceAppender;
import com.alipay.common.tracer.core.appender.file.TimedRollingFileAppender;
import com.alipay.common.tracer.core.base.AbstractTestBase;
import com.alipay.common.tracer.core.utils.TracerUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * StaticInfoLog Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>July 2, 2017</pre>
 */
public class StaticInfoLogTest extends AbstractTestBase {

    @Test
    public void testLogStaticInfo() throws IOException, InterruptedException, NoSuchFieldException,
                                   IllegalAccessException {

        //record
        reflect();
        StaticInfoLog.logStaticInfo();

        TestUtil.waitForAsyncLog();

        List<String> params = new ArrayList<String>();
        params.add(TracerUtils.getPID());
        params.add(TracerUtils.getInetAddress());
        params.add(TracerUtils.getCurrentZone());
        params.add(TracerUtils.getDefaultTimeZone());
        List<String> contents = FileUtils.readLines(customFileLog("static-info.log"));
        Assert.assertFalse("Static information log has no content", contents.isEmpty());
        assertTrue(checkResult(params, contents.get(0)));
    }

    private static void reflect() throws NoSuchFieldException, IllegalAccessException {
        Field field = StaticInfoLog.class.getDeclaredField("appender");
        field.setAccessible(true);
        TraceAppender appender = (TraceAppender) field.get(null);
        appender = new TimedRollingFileAppender("static-info.log", true);
        field.set(null, appender);
    }

}
