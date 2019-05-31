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
package com.alipay.sofa.tracer.examples.springwebflux.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author sx
 */
@RestController
public class SampleRestController {

    private static Logger       logger   = LoggerFactory.getLogger(SampleRestController.class);
    private static final String TEMPLATE = "Hello, %s!";

    private final AtomicLong    counter  = new AtomicLong();

    /***
     * http://localhost:8080/springwebflux
     * @param name name
     * @return map
     */
    @RequestMapping("/springwebflux")
    public Mono<Map<String, Object>> springwebflux(@RequestParam(value = "name", defaultValue = "SOFATracer SpringWebFlux DEMO") String name) {
        Map<String, Object> resultMap = new HashMap<>(3);
        resultMap.put("success", true);
        resultMap.put("id", counter.incrementAndGet());
        resultMap.put("content", String.format(TEMPLATE, name));
        logger.info("result: {}", resultMap);
        return Mono.delay(Duration.ofSeconds(3)).map(i -> resultMap);
    }
}
