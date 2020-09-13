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
package com.sofa.tracer.plugins.kafka.base;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.mock.MockSofaTracer;
import com.sofa.alipay.tracer.plugins.kafkamq.factories.SofaTracerKafkaConsumerFactory;
import com.sofa.alipay.tracer.plugins.kafkamq.factories.SofaTracerKafkaProducerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenchen6  2020/9/13 21:31
 * @since 3.1.0-SNAPSHOT
 */
@EnableKafka
@Configuration
@EnableAutoConfiguration
public class MockSofaTracerConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public SofaTracer mockSofaTracer() {
        return MockSofaTracer.getMockSofaTracer();
    }


    @Bean
    public ConsumerFactory<Integer, String> consumerFactory() {
        final Map<String, Object> consumerProps = new HashMap<>();
//                KafkaTestUtils
//                .consumerProps("sampleRawConsumer", "false", embeddedKafka.getEmbeddedKafka());
//        consumerProps.put("auto.offset.reset", "earliest");

        return new SofaTracerKafkaConsumerFactory<>(new DefaultKafkaConsumerFactory<>(consumerProps));
    }


    @Bean
    public ProducerFactory<Integer, String> producerFactory() {
        Map<String, Object> pro = new HashMap<>();
        return new SofaTracerKafkaProducerFactory<>(new DefaultKafkaProducerFactory<>(pro));
    }

    @Bean
    public KafkaTemplate<Integer, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}