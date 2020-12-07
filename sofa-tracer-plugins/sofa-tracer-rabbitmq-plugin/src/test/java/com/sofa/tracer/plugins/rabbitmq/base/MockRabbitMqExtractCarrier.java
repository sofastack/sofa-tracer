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
package com.sofa.tracer.plugins.rabbitmq.base;

import com.sofa.alipay.tracer.plugins.rabbitmq.carrier.RabbitMqExtractCarrier;
import org.junit.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *  MockRabbitMqExtractCarrier.
 *
 * @author chenchen6 2020/8/11 23:49
 */
public class MockRabbitMqExtractCarrier {

    @Test
    public void testIterator() {
        String key = "testSOFATracerId";
        String value = "testSOFATracerValue";
        Map<String, String> map = Collections.singletonMap(key, value);
        Map<String, Object> headers = Collections.singletonMap(key, value);
        RabbitMqExtractCarrier carrier = new RabbitMqExtractCarrier(headers);
        final Iterator<Map.Entry<String, String>> iterator = carrier.iterator();
        assertThat(iterator).containsAll(map.entrySet());
    }

    @Test
    public void testIterator_whenNullValue() {
        Map<String, Object> headers = Collections.singletonMap("testSOFATracerId", null);
        RabbitMqExtractCarrier carrier = new RabbitMqExtractCarrier(headers);
        final Iterator<Map.Entry<String, String>> iterator = carrier.iterator();
        assertThat(iterator).doesNotContainNull();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPut() {
        String key = "testSOFATracerId";
        String value = "testSOFATracerValue";
        Map<String, Object> headers = Collections.singletonMap(key, value);
        RabbitMqExtractCarrier carrier = new RabbitMqExtractCarrier(headers);
        //catch UnsupportedOperationException.
        carrier.put(key, value);
    }
}
