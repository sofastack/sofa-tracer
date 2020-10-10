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
package com.sofa.alipay.tracer.plugins.kafkamq.carrier;

import io.opentracing.propagation.TextMap;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * KafkaMqExtractCarrier.
 *
 * @author chenchen6  2020/8/23 09:41
 * @since 3.1.0-SNAPSHOT
 */
public class KafkaMqExtractCarrier implements TextMap {

    private final Map<String, String> headerMap = new HashMap<>();

    public KafkaMqExtractCarrier(Headers headers) {
        for (Header head : headers) {
            String headerVal = head.value() == null ? null : new String(head.value(),
                StandardCharsets.UTF_8);
            headerMap.put(head.key(), headerVal);
        }
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return headerMap.entrySet().iterator();
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException(
            "put should never be used with SOFA Tracer.extract()");
    }
}
