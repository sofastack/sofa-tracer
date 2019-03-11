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
package com.alipay.common.tracer.core.samplers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @description: [test unit for SamplerProperties]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/7/25
 */
public class SamplerPropertiesTest {

    SamplerProperties samplerProperties;

    @Before
    public void before() {
        samplerProperties = new SamplerProperties();

    }

    @Test
    public void getPercentage() {
        Assert.assertTrue(samplerProperties.getPercentage() == 100);
    }

    @Test
    public void setPercentage() {
        samplerProperties.setPercentage(20);
        Assert.assertTrue(samplerProperties.getPercentage() == 20);
    }
}