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

import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.utils.StringUtils;

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
            float percentage = 100;

            String perStr = SofaTracerConfiguration
                .getProperty(SofaTracerConfiguration.SAMPLER_STRATEGY_PERCENTAGE_KEY);
            if (StringUtils.isNotBlank(perStr)) {
                percentage = Float.parseFloat(perStr);
            }
            samplerProperties.setPercentage(percentage);
        } catch (Exception e) {
            SelfLog.error("It will be use default percentage value :100;", e);
            samplerProperties.setPercentage(100);
        }
        samplerProperties.setRuleClassName(SofaTracerConfiguration
            .getProperty(SofaTracerConfiguration.SAMPLER_STRATEGY_CUSTOM_RULE_CLASS_NAME));
    }

    /**
     * getSampler by samplerName
     *
     * the samplerName is the user configuration
     * @return Sampler
     * @throws Exception
     */
    public static Sampler getSampler() throws Exception {
        // User-defined rules have high priority
        if (StringUtils.isNotBlank(samplerProperties.getRuleClassName())) {
            return (Sampler) Class.forName(samplerProperties.getRuleClassName()).newInstance();
        }
        // default instance
        return new SofaTracerPercentageBasedSampler(samplerProperties);
    }
}
