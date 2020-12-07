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
import org.springframework.amqp.core.MessageProperties;

import java.util.Iterator;
import java.util.Map;

/**
 *  RabbitMqInjectCarrier.
 *
 * @author  chenchen6  2020/8/22 17:36
 * @since 3.1.0-SNAPSHOT
 */
public class RabbitMqInjectCarrier implements TextMap {

    private final MessageProperties messageProperties;

    public RabbitMqInjectCarrier(MessageProperties messageProperties) {
        this.messageProperties = messageProperties;
    }

    @Override
    public void put(String key, String value) {
        messageProperties.getHeaders().put(key, value);
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException(
            "iterator should never be used with sofa tracer #inject()");
    }
}
