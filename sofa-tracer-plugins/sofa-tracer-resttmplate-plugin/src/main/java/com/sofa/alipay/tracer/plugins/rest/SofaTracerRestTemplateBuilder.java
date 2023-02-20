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
package com.sofa.alipay.tracer.plugins.rest;

import com.alipay.common.tracer.core.tracer.AbstractTracer;
import com.sofa.alipay.tracer.plugins.rest.interceptor.RestTemplateInterceptor;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 *  SofaTracerRestTemplateBuilder
 * @author: guolei.sgl
 * @since: v2.3.0
 */
public class SofaTracerRestTemplateBuilder {

    private static volatile AbstractTracer restTemplateTracer = null;

    public static RestTemplate buildRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
        RestTemplateInterceptor restTemplateInterceptor = new RestTemplateInterceptor(
            getRestTemplateTracer());
        interceptors.add(restTemplateInterceptor);
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }

    public static AbstractTracer getRestTemplateTracer() {
        if (restTemplateTracer == null) {
            synchronized (RestTemplateTracer.class) {
                if (restTemplateTracer == null) {
                    restTemplateTracer = new RestTemplateTracer();
                }
            }
        }
        return restTemplateTracer;
    }
}
