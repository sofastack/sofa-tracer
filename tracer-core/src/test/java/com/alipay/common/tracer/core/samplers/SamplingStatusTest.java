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
import java.util.HashMap;
import java.util.Map;

/**
 * @description: [test unit for SamplingStatus]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/7/25
 */
public class SamplingStatusTest {

    SamplingStatus          samplingStatus;
    HashMap<String, Object> tags;

    @Before
    public void setUp() throws Exception {
        samplingStatus = new SamplingStatus();
        tags = new HashMap<String, Object>();
    }

    @Test
    public void isSampled() {
        Assert.assertTrue(!samplingStatus.isSampled());
    }

    @Test
    public void setSampled() {
        samplingStatus.setSampled(true);
        Assert.assertTrue(samplingStatus.isSampled());
    }

    @Test
    public void setTags() {
        samplingStatus.setTags(tags);
        Map<String, Object> tags = samplingStatus.getTags();
        Assert.assertTrue(tags.size() == 0);
    }
}