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
package com.sofa.alipay.tracer.plugins.rabbitmq.carrier;

import io.opentracing.propagation.TextMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *  RabbitMqExtractCarrier
 *
 * @author  chenchen6  2020/8/22 17:35
 * @since 3.1.0
 */
public class RabbitMqExtractCarrier implements TextMap {
    private final Map<String, String> map = new HashMap<>();

    public RabbitMqExtractCarrier(Map<String, Object> headers) {
        headers.forEach((key, value) -> {
            if (value == null) {
                return;
            }
            map.put(key, value.toString());
        });
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return map.entrySet().iterator();
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException("Should be used only with sofa tracer #extract()");
    }
}
