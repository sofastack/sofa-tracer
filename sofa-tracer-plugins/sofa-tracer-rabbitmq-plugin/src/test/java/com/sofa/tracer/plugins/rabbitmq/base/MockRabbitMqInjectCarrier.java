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

import com.sofa.alipay.tracer.plugins.rabbitmq.carrier.RabbitMqInjectCarrier;
import org.junit.Test;
import org.springframework.amqp.core.MessageProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MockRabbitMqInjectCarrier.
 *
 * @author chenchen6  2020/8/11 23:44
 */
public class MockRabbitMqInjectCarrier {

    @Test
    public void testPut2Properties() {
        MessageProperties messageProperties = new MessageProperties();
        RabbitMqInjectCarrier carrier = new RabbitMqInjectCarrier(messageProperties);
        String key = "testSOFATracerId";
        String value = "testSOFATraceValue";
        carrier.put(key, value);
        assertThat(messageProperties.getHeaders().get(key)).isEqualTo(value);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIterator() {
        MessageProperties messageProperties = new MessageProperties();
        RabbitMqInjectCarrier carrier = new RabbitMqInjectCarrier(messageProperties);
        //catch UnsupportedOperationException.
        carrier.iterator();
    }
}
