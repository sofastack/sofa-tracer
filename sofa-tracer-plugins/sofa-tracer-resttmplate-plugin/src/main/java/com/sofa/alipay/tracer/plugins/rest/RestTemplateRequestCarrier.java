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

import io.opentracing.propagation.TextMap;
import org.springframework.http.HttpRequest;

import java.util.Iterator;
import java.util.Map;

/**
 * RestTemplateRequestCarrier
 * @author: guolei.sgl
 * @since: 18/10/15
 */
public class RestTemplateRequestCarrier implements TextMap {

    private final HttpRequest request;

    public RestTemplateRequestCarrier(HttpRequest request) {
        this.request = request;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(String key, String value) {
        request.getHeaders().add(key, value);
    }
}
