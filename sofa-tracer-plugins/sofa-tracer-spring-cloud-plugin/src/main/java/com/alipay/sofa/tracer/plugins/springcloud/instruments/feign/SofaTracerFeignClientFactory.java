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
package com.alipay.sofa.tracer.plugins.springcloud.instruments.feign;

import feign.Client;
import org.springframework.cloud.openfeign.FeignClientFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 10:42 AM
 * @since:
 **/
public class SofaTracerFeignClientFactory extends FeignClientFactory {

    private final FeignClientFactory delegateClientFactory;

    public SofaTracerFeignClientFactory(FeignClientFactory delegateClientFactory) {
        this.delegateClientFactory = delegateClientFactory;
    }

    @Override
    public <T> T getInstance(String name, Class<T> type) {
        T object = this.delegateClientFactory.getInstance(name, type);
        return (T) this.wrapperFeignClient(object);
    }

    @Override
    public <T> Map<String, T> getInstances(String name, Class<T> type) {
        Map<String, T> instances = this.delegateClientFactory.getInstances(name, type);
        Map<String, T> tracerInstances = new HashMap<>();
        if (instances != null) {
            instances.forEach((key, value) -> tracerInstances.put(key, (T) wrapperFeignClient(value)));
        }
        return tracerInstances;
    }

    private Object wrapperFeignClient(Object bean) {
        // not need to wrapper
        if (bean instanceof SofaTracerFeignClient) {
            return bean;
        }
        if (bean instanceof Client) {
            return newSofaTracerFeignClient((Client) bean);
        }
        return bean;
    }

    private SofaTracerFeignClient newSofaTracerFeignClient(Client delegate) {
        return new SofaTracerFeignClient(delegate);
    }
}
