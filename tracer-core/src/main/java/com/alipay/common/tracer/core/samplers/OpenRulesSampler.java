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

import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.util.*;

/**
 *
 * @description: Ability to customize rules for external users to provide sampling
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/9/11
 */
public class OpenRulesSampler implements Sampler {

    final Rule                      rules;
    static final String             TYPE = "OpenRulesSampler";

    private final SamplerProperties configuration;

    public OpenRulesSampler(SamplerProperties configuration) throws Exception {
        this.configuration = configuration;
        rules = (Rule) Class.forName(configuration.getRuleClassName()).newInstance();
    }

    @Override
    public SamplingStatus sample(SofaTracerSpan sofaTracerSpan) {
        SamplingStatus samplingStatus = new SamplingStatus();
        Map<String, Object> tags = new HashMap<String, Object>();
        tags.put(SofaTracerConstant.SAMPLER_TYPE_TAG_KEY, TYPE);
        tags.put(SofaTracerConstant.SAMPLER_PARAM_TAG_KEY, configuration.getPercentage());
        tags = Collections.unmodifiableMap(tags);
        samplingStatus.setTags(tags);
        boolean result = rules.matches(sofaTracerSpan);
        samplingStatus.setSampled(result);
        return samplingStatus;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void close() {
        //do nothing
    }

    public static abstract class Rule {
        /**
         * Returns true if this rule matches the input parameters
         * @param sofaTracerSpan current span
         * @return
         */
        public abstract boolean matches(SofaTracerSpan sofaTracerSpan);
    }
}
