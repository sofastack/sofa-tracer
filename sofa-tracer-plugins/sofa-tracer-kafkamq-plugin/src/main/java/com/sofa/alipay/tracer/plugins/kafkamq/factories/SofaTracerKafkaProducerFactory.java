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

import com.sofa.alipay.tracer.plugins.kafkamq.producer.SofaTracerKafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.kafka.core.ProducerFactory;

/**
 * SofaTracerKafkaProducerFactory。
 * @author Malffeev
 * @author chenchen6
 * @since 3.1.0-SNAPSHOT
 */
public class SofaTracerKafkaProducerFactory<K, V> implements ProducerFactory<K, V>, DisposableBean {

    private final ProducerFactory<K, V> producerFactory;

    public SofaTracerKafkaProducerFactory(ProducerFactory<K, V> producerFactory) {
        this.producerFactory = producerFactory;
    }

    @Override
    public Producer<K, V> createProducer() {
        return new SofaTracerKafkaProducer<>(producerFactory.createProducer());
    }

    @Override
    public boolean transactionCapable() {
        return producerFactory.transactionCapable();
    }

    @Override
    public void destroy() throws Exception {
        if (producerFactory instanceof DisposableBean) {
            ((DisposableBean) producerFactory).destroy();
        }
    }
}
