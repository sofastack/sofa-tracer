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

import com.alipay.sofa.tracer.boot.base.AbstractTestBase;
import com.alipay.sofa.tracer.boot.base.SpringBootWebApplication;
import com.alipay.sofa.tracer.boot.properties.SofaTracerProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.bind.RelaxedBindingNotWritablePropertyException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * SofaTracerPropertiesTest
 *
 * @author yangguanchao
 * @since 2018/10/24
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("configerror")
public class SofaTracerPropertiesTest {

    private static Map<String, Object> map = null;

    @BeforeClass
    public static void beforeClassRun() throws Exception {
        AbstractTestBase.beforeClass();
        ConfigurationProperties configurationProperties = AnnotationUtils.findAnnotation(
            SofaTracerProperties.class, ConfigurationProperties.class);
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(configurationProperties);
        Field valueCacheField = invocationHandler.getClass().getDeclaredField("valueCache");
        valueCacheField.setAccessible(true);
        map = (Map<String, Object>) valueCacheField.get(invocationHandler);
        map.put("prefix", "com.alipay.sofa.tracer.test");
        map.put("value", "com.alipay.sofa.tracer.test");
        map.put("ignoreUnknownFields", false);
    }

    @Test
    public void testSofaTracerPropertiesAnnotation() throws Exception {
        Boolean isErrorParseProperties = false;
        try {
            System.setProperty("spring.profiles.active", "configerror");
            SpringBootWebApplication.main(new String[] {});
        } catch (RelaxedBindingNotWritablePropertyException exception) {
            isErrorParseProperties = true;
        }
        assertTrue(isErrorParseProperties);
        //recover
        map.put("ignoreUnknownFields", true);
        map.put("prefix", SofaTracerProperties.SOFA_TRACER_CONFIGURATION_PREFIX);
        map.put("value", SofaTracerProperties.SOFA_TRACER_CONFIGURATION_PREFIX);
    }

    @AfterClass
    public static void reset() {
        System.getProperties().remove("spring.profiles.active");
    }
}
