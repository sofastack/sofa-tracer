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

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.sofa.tracer.boot.springmvc.properties.OpenTracingSpringMvcProperties;
import com.alipay.sofa.tracer.plugins.springmvc.SpringMvcSofaTracerFilter;
import com.alipay.sofa.tracer.plugins.springmvc.SpringMvcTracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenTracingSpringMvcAutoConfiguration
 *
 * @author yangguanchao
 * @since 2018/05/01
 */
@Configuration
@EnableConfigurationProperties(OpenTracingSpringMvcProperties.class)
@ComponentScan(value = { "com.alipay.sofa.tracer.boot" })
@ConditionalOnWebApplication
public class OpenTracingSpringMvcAutoConfiguration {

    @Autowired
    private OpenTracingSpringMvcProperties openTracingSpringProperties;

    @Bean
    public FilterRegistrationBean springMvcDelegatingFilterProxy() {
        //decide output format  json or digest
        if (openTracingSpringProperties.isJsonOutput()) {
            //config
            SofaTracerConfiguration.setProperty(SpringMvcTracer.SPRING_MVC_JSON_FORMAT_OUTPUT,
                "true");
        }
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
}