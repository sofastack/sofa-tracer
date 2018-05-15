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
package com.alipay.common.tracer.core.configuration;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author luoguimu123
 * @version $Id: SofaTracerConfigurationTest.java, v 0.1 2017年06月29日 上午10:06 luoguimu123 Exp $
 */
public class SofaTracerConfigurationTest {

    @BeforeClass
    public static void before() throws Exception {

        System.setProperty("testProperty3", "test3");
        System.setProperty("testProperty4", "test4");
        System.setProperty("testIntegerProperty2", "2");

    }

    @Test
    public void testConf() {
        SofaTracerConfiguration.setProperty("testMapkey", "");
        Assert.assertTrue(SofaTracerConfiguration.getMapEmptyIfNull("testMapkey").size() == 0);
    }

    @AfterClass
    public static void after1() throws Exception {

        Thread.sleep(1500);
        File tracerSelfLog = new File(System.getProperty("user.home") + File.separator + "logs"
                                      + File.separator + "tracelog" + File.separator
                                      + "tracer-self.log");

        if (tracerSelfLog.exists()) {
            FileUtils.write(tracerSelfLog, "");
        }

    }

    @Test
    public void testTracerConfiguration() {
        Assert.assertEquals(SofaTracerConfiguration.getInteger("nullkey"), null);
        Integer i = new Integer(1);
        Assert.assertEquals(SofaTracerConfiguration.getIntegerDefaultIfNull("nullkey", i), i);
        Assert.assertEquals(SofaTracerConfiguration.getMapEmptyIfNull("nullkey").size(), 0);
        Assert.assertEquals(SofaTracerConfiguration.getMapEmptyIfNull("testProperty1").size(), 0);
        Assert.assertEquals(SofaTracerConfiguration.getMapEmptyIfNull("testProperty3").size(), 0);
        Assert.assertEquals(SofaTracerConfiguration.getProperty("testProperty1"), "test1");
        Assert.assertEquals(SofaTracerConfiguration.getProperty("testProperty3"), "test3");

        Assert.assertEquals(SofaTracerConfiguration.getProperty("notExist", null), null);

        SofaTracerConfiguration.setProperty("testProperty1", "newkey");
        Assert.assertEquals(SofaTracerConfiguration.getProperty("testProperty1"), "newkey");

        SofaTracerConfiguration.setProperty("testProperty3", "newkey");
        Assert.assertEquals(SofaTracerConfiguration.getProperty("testProperty3"), "newkey");

        Assert.assertEquals(SofaTracerConfiguration.getInteger("testIntegerProperty1").intValue(),
            1);
        Assert.assertEquals(SofaTracerConfiguration.getInteger("testIntegerProperty2").intValue(),
            2);

        HashMap<String, String> map = new HashMap<String, String>();
        SofaTracerConfiguration.setProperty("map", map);
        Assert.assertEquals(SofaTracerConfiguration.getMapEmptyIfNull("map"), map);

        SofaTracerConfiguration.setProperty("integer1", 1);
        Assert.assertEquals(SofaTracerConfiguration.getInteger("integer1").intValue(), 1);
    }

    @Test
    public void testExternalConfiguration() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("externalkey1", "externalvalue1");
        map.put("externalkey2", "externalvalue2");

        SofaTracerConfiguration
            .setSofaTracerExternalConfiguration(new SofaTracerExternalConfiguration() {
                @Override
                public String getValue(String key) {
                    return map.get(key);
                }

                @Override
                public boolean contains(String key) {
                    return map.containsKey(key);
                }
            });

        Assert.assertTrue(SofaTracerConfiguration.getProperty("externalkey1").equals(
            "externalvalue1"));

        SofaTracerConfiguration.setSofaTracerExternalConfiguration(null);

    }

}