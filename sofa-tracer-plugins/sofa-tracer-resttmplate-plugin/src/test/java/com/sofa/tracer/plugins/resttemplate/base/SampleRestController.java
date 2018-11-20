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
package com.sofa.tracer.plugins.resttemplate.base;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SampleRestController
 *
 * @author yangguanchao
 * @since v2.3.0
 */
@RestController
public class SampleRestController {
    /**
     * Request http://localhost:8080/
     * @return Map of Result
     */
    @GetMapping("/")
    public Map<String, Object> greeting() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "weiheng");
        return map;
    }

    /**
     * Request http://localhost:8080/
     * @param postBody body
     * @return Result
     */
    @PostMapping("/")
    public PostBody postBody(@RequestBody PostBody postBody) {
        return postBody;
    }
}
