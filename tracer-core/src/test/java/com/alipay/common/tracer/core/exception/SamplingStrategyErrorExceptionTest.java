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
package com.alipay.common.tracer.core.exception;

import com.alipay.common.tracer.core.samplers.SamplerProperties;
import com.alipay.common.tracer.core.samplers.SofaTracerPercentageBasedSampler;
import org.junit.Assert;
import org.junit.Test;

/**
 * @description: [test for SamplingStrategyErrorException]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/7/30
 */
public class SamplingStrategyErrorExceptionTest {

    @Test
    public void testSamplingStrategyErrorException() {
        try {
            buildSamplingStrategyErrorExceptionWithThrowable();
        } catch (SamplingStrategyErrorException e) {
            Assert.assertEquals("sampler init failed;", e.getMessage());
        }

        try {
            buildSamplingStrategyErrorException();
        } catch (SamplingStrategyErrorException e) {
            Assert.assertEquals("SamplerProperties is null", e.getMessage());
        }
    }

    private void buildSamplingStrategyErrorExceptionWithThrowable() {
        try {
            SamplerProperties configuration = new SamplerProperties();
            configuration.setPercentage(-1);
            //init failed
            new SofaTracerPercentageBasedSampler(configuration);
        } catch (Throwable e) {
            throw new SamplingStrategyErrorException("sampler init failed;", e);
        }
    }

    private void buildSamplingStrategyErrorException() throws SamplingStrategyErrorException {
        try {
            SamplerProperties configuration = null;
            //init failed
            new SofaTracerPercentageBasedSampler(configuration);
        } catch (Throwable e) {
            throw new SamplingStrategyErrorException("SamplerProperties is null");
        }
    }
}