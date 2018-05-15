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
package com.alipay.common.tracer.test;

import com.alipay.common.tracer.core.appender.self.SelfLog;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author luoguimu123
 * @version $Id: MDCTest.java, v 0.1 2017年06月19日 下午6:14 luoguimu123 Exp $
 */
public class MDCTest {

    //private static final Logger logger = LoggerFactory.getLogger("traceLog");

    @Test
    public void test() throws IOException {

        Logger logger = LoggerFactory.getLogger("traceLog");

        MDC.clear();
        MDC.put("sessionId", "f9e287fad9e84cff8b2c2f2ed92adbe6");
        MDC.put("cityId", "1");
        MDC.put("siteName", "北京");
        MDC.put("userName", "userwyh");
        logger.info("测试MDC打印一");

        MDC.put("mobile", "110");
        logger.info("测试MDC打印二");

        MDC.put("mchId", "12");
        MDC.put("mchName", "商户名称");
        logger.info("测试MDC打印三");
        BufferedReader reader = new BufferedReader(new FileReader("../logs/trace.log"));
        List<String> content = new ArrayList<String>();
        while (true) {
            String s = reader.readLine();
            if (s == null || s.equals("")) {
                break;
            }
            content.add(s);
        }
        String line1 = content.get(content.size() - 3);
        line1 = line1.substring(line1.indexOf(",") + 1);
        for (String s : line1.split(",", -1)) {
            SelfLog.info(s);
        }
        String[] array = line1.split(",");
        Assert.assertEquals(array[0], "北京");
        Assert.assertEquals(array[3], "f9e287fad9e84cff8b2c2f2ed92adbe6");
        Assert.assertEquals(array[4], "1");
        Assert.assertEquals(array[5], "userwyh");
        Assert.assertEquals(array[7], "测试MDC打印一");

        String line2 = content.get(content.size() - 2);
        line2 = line2.substring(line2.indexOf(",") + 1);
        String[] array2 = line2.split(",");
        Assert.assertEquals(array2[0], "北京");
        Assert.assertEquals(array2[3], "f9e287fad9e84cff8b2c2f2ed92adbe6");
        Assert.assertEquals(array2[4], "1");
        Assert.assertEquals(array2[5], "userwyh");
        Assert.assertEquals(array2[6], "110");
        Assert.assertEquals(array2[7], "测试MDC打印二");

        String line3 = content.get(content.size() - 1);
        line3 = line3.substring(line3.indexOf(",") + 1);
        String[] array3 = line3.split(",");
        Assert.assertEquals(array3[0], "北京");
        Assert.assertEquals(array3[1], "12");
        Assert.assertEquals(array3[2], "商户名称");
        Assert.assertEquals(array3[3], "f9e287fad9e84cff8b2c2f2ed92adbe6");
        Assert.assertEquals(array3[4], "1");
        Assert.assertEquals(array3[5], "userwyh");
        Assert.assertEquals(array3[6], "110");
        Assert.assertEquals(array3[7], "测试MDC打印三");

    }

}
