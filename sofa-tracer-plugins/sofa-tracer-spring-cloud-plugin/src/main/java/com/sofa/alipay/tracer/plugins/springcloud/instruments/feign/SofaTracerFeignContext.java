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
package com.sofa.alipay.tracer.plugins.springcloud.instruments.feign;

import feign.Client;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 10:42 AM
 * @since:
 **/
public class SofaTracerFeignContext extends FeignContext {

    private FeignContext delegateContext;
    private BeanFactory  beanFactory;

    public SofaTracerFeignContext(FeignContext delegateContext, BeanFactory beanFactory) {
        this.delegateContext = delegateContext;
        this.beanFactory = beanFactory;
    }

    @Override
    public <T> T getInstance(String name, Class<T> type) {
        T object = this.delegateContext.getInstance(name, type);
        return (T) this.wrapperFeignClient(object);
    }

    @Override
    public <T> Map<String, T> getInstances(String name, Class<T> type) {
        Map<String, T> instances = this.delegateContext.getInstances(name, type);
        Map<String, T> tracerInstances = new HashMap<>();
        if (instances != null) {
            instances.entrySet().forEach( item ->
                    tracerInstances.put(item.getKey(), (T) wrapperFeignClient(item.getValue())));
        }
        return tracerInstances;
    }

    private Object wrapperFeignClient(Object bean) {
        // not need to wrapper
        if (bean instanceof SofaTracerFeignClient
            || bean instanceof SofaTracerLoadBalancedFeignClient) {
            return bean;
        }
        if (bean instanceof Client) {
            // LoadBalancerFeignClient Type Wrapper to SofaTracerLoadBalancedFeignClient
            if (bean instanceof LoadBalancerFeignClient
                && !(bean instanceof SofaTracerLoadBalancedFeignClient)) {
                return new SofaTracerLoadBalancedFeignClient(
                    newSofaTracerFeignClient(((LoadBalancerFeignClient) bean).getDelegate()),
                    beanFactory.getBean(CachingSpringLoadBalancerFactory.class),
                    beanFactory.getBean(SpringClientFactory.class));
            }
            return newSofaTracerFeignClient((Client) bean);
        }
        return bean;
    }

    private SofaTracerFeignClient newSofaTracerFeignClient(Client delegate) {
        return new SofaTracerFeignClient(delegate);
    }
}
