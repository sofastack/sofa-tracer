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
package com.alipay.common.tracer.util;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * <p>created at 2021/4/12
 *
 * @author xiangfeng.xzc
 */
public class DesensitizationHelperTest {
    @Before
    public void before() {
        SofaTracerConfiguration.setProperty(DesensitizationHelper.ENABLED_KEY, "true");
    }

    @After
    public void after() {
        SofaTracerConfiguration.removeProperty(DesensitizationHelper.ENABLED_KEY);
        DesensitizationHelper.setDesensitizer(null);
    }

    @Test
    public void test() {
        DesensitizationHelper.setDesensitizer(new MockDesensitizer());
        assertEquals("mocked", DesensitizationHelper.desensitize("aa"));
    }

    @Test
    public void test_exception() {
        DesensitizationHelper.setDesensitizer(new MockDesensitizer(true));
        assertEquals("aa", DesensitizationHelper.desensitize("aa"));
    }
}