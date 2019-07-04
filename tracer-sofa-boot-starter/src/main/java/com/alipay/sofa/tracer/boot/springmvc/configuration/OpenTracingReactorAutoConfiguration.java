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

import com.alipay.sofa.tracer.boot.springmvc.processor.HookRegisteringBeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import javax.annotation.PreDestroy;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/7/1 2:19 PM
 * @since:
 **/
@Configuration
@ConditionalOnClass(Mono.class)
@ConditionalOnProperty(name = "com.alipay.sofa.tracer.reactor.enabled", havingValue = "true", matchIfMissing = true)
public class OpenTracingReactorAutoConfiguration {

    @PreDestroy
    public void cleanupHooks() {
        HookRegisteringBeanDefinitionRegistryPostProcessor.resetHooks();
    }

    @Bean
    @ConditionalOnMissingBean
    public HookRegisteringBeanDefinitionRegistryPostProcessor hookRegisteringBeanDefinitionRegistryPostProcessor() {
        return new HookRegisteringBeanDefinitionRegistryPostProcessor();
    }
}
