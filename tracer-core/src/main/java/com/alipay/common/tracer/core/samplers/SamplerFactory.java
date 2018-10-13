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

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;

/**
 * SamplerFactory
 *
 * @author: guolei.sgl
 * @since: 18/9/11
 */
public class SamplerFactory {

    public static SamplerProperties samplerProperties;

    static {
        samplerProperties = new SamplerProperties();
        try {
            float percentage = Float.parseFloat(SofaTracerConfiguration
                .getProperty(SofaTracerConfiguration.SAMPLER_STRATEGY_PERCENTAGE_KEY));
            samplerProperties.setPercentage(percentage);
        } catch (Exception e) {
            samplerProperties.setPercentage(0.1f);
        }
        samplerProperties.setRuleClassName(SofaTracerConfiguration
            .getProperty(SofaTracerConfiguration.SAMPLER_STRATEGY_CUSTOM_RULE_CLASS_NAME));
    }

    /**
     * getSampler by samplerName
     *
     * the samplerName is the user configuration
     *
     * @param samplerName
     * @return Sampler
     * @throws Exception
     */
    public static Sampler getSampler(String samplerName) throws Exception {
        if (samplerName.equals(OpenRulesSampler.TYPE)) {
            return new OpenRulesSampler(samplerProperties);
        }
        if (samplerName.equals(SofaTracerPercentageBasedSampler.TYPE)) {
            return new SofaTracerPercentageBasedSampler(samplerProperties);
        }
        return null;
    }
}
