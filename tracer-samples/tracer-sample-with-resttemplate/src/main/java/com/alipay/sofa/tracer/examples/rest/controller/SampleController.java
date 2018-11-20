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
package com.alipay.sofa.tracer.examples.rest.controller;

import com.alipay.sofa.tracer.examples.rest.RestTemplateDemoApplication;
import com.sofa.alipay.tracer.plugins.rest.SofaTracerRestTemplateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 *  SampleController
 * @author: guolei.sgl
 * @since : v2.3.0
 */
@RestController
public class SampleController {
    private final AtomicLong counter = new AtomicLong(0);
    private static Logger    logger  = LoggerFactory.getLogger(RestTemplateDemoApplication.class);

    @Autowired
    RestTemplate             restTemplate;

    /**
     * Request http://localhost:8080/rest?name=
     * @return Map of Result
     */
    @RequestMapping("/rest")
    public Map<String, Object> rest() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("count", counter.incrementAndGet());
        return map;
    }

    @RequestMapping("/asyncrest")
    public Map<String, Object> asyncrest() throws InterruptedException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("count", counter.incrementAndGet());
        Thread.sleep(5000);
        return map;
    }

    @RequestMapping("/api")
    public Map<String, Object> test() throws ExecutionException, InterruptedException {
        RestTemplate restTemplateApi = SofaTracerRestTemplateBuilder.buildRestTemplate();
        ResponseEntity<String> responseEntity = restTemplateApi.getForEntity(
            "http://sac.alipay.net:8080/rest", String.class);
        logger.info("Response is {}", responseEntity.getBody());

        AsyncRestTemplate asyncRestTemplate = SofaTracerRestTemplateBuilder
            .buildAsyncRestTemplate();
        ListenableFuture<ResponseEntity<String>> forEntity = asyncRestTemplate.getForEntity(
            "http://sac.alipay.net:8080/asyncrest", String.class);
        //async
        logger.info("Async Response is {}", forEntity.get().getBody());

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("count", counter.incrementAndGet());
        return map;
    }

    @RequestMapping("/auto")
    public Map<String, Object> testAuto() {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
            "http://sac.alipay.net:8080/rest", String.class);
        logger.info("Response is {}", responseEntity.getBody());

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("count", counter.incrementAndGet());
        return map;
    }

}
