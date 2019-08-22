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

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 *  SampleController
 * @author: guolei.sgl
 * @since : 18/10/16
 */
@RestController
public class SampleController {
    private final AtomicLong counter = new AtomicLong(0);

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
        System.out.println("asyncrest execute finish ...");
        return map;
    }
}
