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
package com.alipay.sofa.tracer.boot.resttemplate;

import com.sofa.alipay.tracer.plugins.rest.SofaTracerRestTemplateBuilder;
import com.sofa.alipay.tracer.plugins.rest.interceptor.RestTemplateInterceptor;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * CustomRestTemplateCustomizer
 *
 * @version 1.0
 * @author: guolei.sgl 18/11/19 下午11:07
 * @since: v2.3.0
 **/
public class CustomRestTemplateCustomizer implements RestTemplateCustomizer {
    @Override
    public void customize(RestTemplate restTemplate) {
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        RestTemplateInterceptor restTemplateInterceptor = new RestTemplateInterceptor(
            SofaTracerRestTemplateBuilder.getRestTemplateTracer());
        interceptors.add(restTemplateInterceptor);
        restTemplate.setInterceptors(interceptors);
    }
}
