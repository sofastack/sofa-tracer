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
package com.sofa.alipay.tracer.plugins.kafkamq.factories;

import com.sofa.alipay.tracer.plugins.kafkamq.consumer.SofaTracerKafkaConsumer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.core.ConsumerFactory;

import java.util.Map;

/**
 *  SofaTracerKafkaConsumerFactory.
 *
 * @author chenchen6  2020/9/2 22:33
 * @since 3.1.0-SNAPSHOT
 */
public class SofaTracerKafkaConsumerFactory<K, V> implements ConsumerFactory<K, V> {

    private final ConsumerFactory<K, V> consumerFactory;

    public SofaTracerKafkaConsumerFactory(ConsumerFactory<K, V> consumerFactory) {
        this.consumerFactory = consumerFactory;
    }

    @Override
    public Consumer<K, V> createConsumer() {
        return new SofaTracerKafkaConsumer<>(consumerFactory.createConsumer());
    }

    @Override
    public Consumer<K, V> createConsumer(String clientIdSuffix) {
        return new SofaTracerKafkaConsumer<>(consumerFactory.createConsumer(clientIdSuffix));
    }

    @Override
    public Consumer<K, V> createConsumer(String groupId, String clientIdSuffix) {
        return new SofaTracerKafkaConsumer<>(
            consumerFactory.createConsumer(groupId, clientIdSuffix));
    }

    @Override
    public Consumer<K, V> createConsumer(String groupId, String clientIdPrefix,
                                         String clientIdSuffix) {
        return new SofaTracerKafkaConsumer<>(consumerFactory.createConsumer(groupId,
            clientIdSuffix, clientIdSuffix));
    }

    @Override
    public boolean isAutoCommit() {
        return consumerFactory.isAutoCommit();
    }

    @Override
    public Map<String, Object> getConfigurationProperties() {
        return consumerFactory.getConfigurationProperties();
    }

    @Override
    public Deserializer<K> getKeyDeserializer() {
        return consumerFactory.getKeyDeserializer();
    }

    @Override
    public Deserializer<V> getValueDeserializer() {
        return consumerFactory.getValueDeserializer();
    }

}
