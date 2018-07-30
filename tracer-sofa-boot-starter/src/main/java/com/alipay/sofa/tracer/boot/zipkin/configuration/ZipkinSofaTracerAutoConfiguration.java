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
package com.alipay.sofa.tracer.boot.zipkin.configuration;

import com.alipay.sofa.tracer.boot.zipkin.ZipkinSofaTracerSpanRemoteReporter;
import com.alipay.sofa.tracer.boot.zipkin.properties.ZipkinSofaTracerProperties;
import com.alipay.sofa.tracer.boot.zipkin.properties.ZipkinSofaTracerSamplerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * ZipkinSofaTracerAutoConfiguration
 *
 * @author yangguanchao
 * @since 2018/05/01
 */
@Configuration
@EnableConfigurationProperties({ ZipkinSofaTracerProperties.class,
                                ZipkinSofaTracerSamplerProperties.class })
@ConditionalOnProperty(value = "com.alipay.sofa.tracer.zipkin.enabled", matchIfMissing = true)
@ConditionalOnClass({ zipkin.Span.class, zipkin.reporter.AsyncReporter.class })
public class ZipkinSofaTracerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ZipkinSofaTracerRestTemplateCustomizer zipkinSofaTracerRestTemplateCustomizer(ZipkinSofaTracerProperties zipkinProperties) {
        return new ZipkinSofaTracerRestTemplateCustomizer(zipkinProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ZipkinSofaTracerSpanRemoteReporter zpkinSofaTracerSpanReporter(ZipkinSofaTracerProperties zipkinSofaTracerProperties,
                                                                          ZipkinSofaTracerRestTemplateCustomizer zipkinSofaTracerRestTemplateCustomizer) {
        RestTemplate restTemplate = new RestTemplate();
        zipkinSofaTracerRestTemplateCustomizer.customize(restTemplate);
        return new ZipkinSofaTracerSpanRemoteReporter(restTemplate,
            zipkinSofaTracerProperties.getBaseUrl(), zipkinSofaTracerProperties.getFlushInterval());
    }
}
