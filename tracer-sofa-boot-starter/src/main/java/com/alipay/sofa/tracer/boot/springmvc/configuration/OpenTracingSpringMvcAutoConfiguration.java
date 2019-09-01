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
package com.alipay.sofa.tracer.boot.springmvc.configuration;

import com.alipay.sofa.tracer.boot.configuration.SofaTracerAutoConfiguration;
import com.alipay.sofa.tracer.boot.properties.SofaTracerProperties;
import com.alipay.sofa.tracer.boot.springmvc.properties.OpenTracingSpringMvcProperties;
import com.alipay.sofa.tracer.plugins.springmvc.SpringMvcSofaTracerFilter;
import com.alipay.sofa.tracer.plugins.webflux.WebfluxSofaTracerFilter;
import com.sofa.alipay.tracer.plugins.rest.SofaTracerRestTemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.WebFilter;

import java.util.List;

/**
 * OpenTracingSpringMvcAutoConfiguration
 *
 * @author yangguanchao
 * @since 2018/05/01
 */
@Configuration
@EnableConfigurationProperties({ OpenTracingSpringMvcProperties.class, SofaTracerProperties.class })
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "com.alipay.sofa.tracer.springmvc", value = "enable", matchIfMissing = true)
@AutoConfigureAfter(SofaTracerAutoConfiguration.class)
public class OpenTracingSpringMvcAutoConfiguration {

    @Autowired
    private OpenTracingSpringMvcProperties openTracingSpringProperties;

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public class SpringMvcDelegatingFilterProxyConfiguration {
        @Bean
        public FilterRegistrationBean springMvcDelegatingFilterProxy() {
            FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
            SpringMvcSofaTracerFilter filter = new SpringMvcSofaTracerFilter();
            filterRegistrationBean.setFilter(filter);
            List<String> urlPatterns = openTracingSpringProperties.getUrlPatterns();
            if (urlPatterns == null || urlPatterns.size() <= 0) {
                filterRegistrationBean.addUrlPatterns("/*");
            } else {
                filterRegistrationBean.setUrlPatterns(urlPatterns);
            }
            filterRegistrationBean.setName(filter.getFilterName());
            filterRegistrationBean.setAsyncSupported(true);
            filterRegistrationBean.setOrder(openTracingSpringProperties.getFilterOrder());
            return filterRegistrationBean;
        }

        @Bean
        @ConditionalOnClass(RestTemplate.class)
        @ConditionalOnMissingBean
        public RestTemplate restTemplate() {
            return SofaTracerRestTemplateBuilder.buildRestTemplate();
        }

        @Bean
        @ConditionalOnClass(AsyncRestTemplate.class)
        @ConditionalOnMissingBean
        public AsyncRestTemplate asyncRestTemplate() {
            return SofaTracerRestTemplateBuilder.buildAsyncRestTemplate();
        }
    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public class WebfluxSofaTracerFilterConfiguration {
        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE + 10)
        public WebFilter webfluxSofaTracerFilter() {
            return new WebfluxSofaTracerFilter();
        }
    }
}