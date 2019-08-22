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
package com.alipay.sofa.tracer.examples.rest;

import com.sofa.alipay.tracer.plugins.rest.SofaTracerRestTemplateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

/**
 *  RestTemplateDemoApplication
 * @author: guolei.sgl
 */
@SpringBootApplication
public class RestTemplateDemoApplication {
    private static Logger logger = LoggerFactory.getLogger(RestTemplateDemoApplication.class);

    public static void main(String[] args) throws Exception {
        SpringApplication.run(RestTemplateDemoApplication.class, args);
        RestTemplate restTemplate = SofaTracerRestTemplateBuilder.buildRestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
            "http://localhost:8801/rest", String.class);
        logger.info("Response is {}", responseEntity.getBody());

        AsyncRestTemplate asyncRestTemplate = SofaTracerRestTemplateBuilder
            .buildAsyncRestTemplate();
        ListenableFuture<ResponseEntity<String>> forEntity = asyncRestTemplate.getForEntity(
            "http://localhost:8801/asyncrest", String.class);
        //async
        logger.info("Async Response is {}", forEntity.get().getBody());

        logger.info("test finish .......");
    }

}
