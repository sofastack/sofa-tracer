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
package com.alipay.sofa.tracer.boot.resttemplate.configuration;

import com.alipay.sofa.tracer.boot.resttemplate.CustomRestTemplateCustomizer;
import com.sofa.alipay.tracer.plugins.rest.interceptor.RestTemplateInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplateAutoConfiguration
 *
 * @version 1.0
 * @author: guolei.sgl 18/11/20 AM 11:02
 * @since: v2.3.0
 **/
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "com.alipay.sofa.tracer.resttemplate", value = "enable", matchIfMissing = true)
public class RestTemplateAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(RestTemplateInterceptor.class)
    public RestTemplate restTemplate() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        RestTemplate restTemplate = restTemplateBuilder.build();
        CustomRestTemplateCustomizer customRestTemplateCustomizer = new CustomRestTemplateCustomizer();
        customRestTemplateCustomizer.customize(restTemplate);
        return restTemplate;
    }
}
