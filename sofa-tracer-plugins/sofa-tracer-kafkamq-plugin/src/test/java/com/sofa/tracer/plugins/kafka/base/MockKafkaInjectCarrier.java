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

import com.sofa.alipay.tracer.plugins.kafkamq.carrier.KafkaMqInjectCarrier;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.Test;

/**
 * copy chenchen6's rabbit mq InjectCarrierMock
 *
 * @author chenchen6  2020/9/12 16:28
 * @since 3.1.0-SNAPSHOT
 */
public class MockKafkaInjectCarrier {

    @Test
    public void testPut2Headers() {
        String key = "testSOFATracerId";
        String value = "testSOFATracerValue";
        //
        Headers headers = new RecordHeaders();

        KafkaMqInjectCarrier injectCarrier = new KafkaMqInjectCarrier(headers);
        //
        injectCarrier.put(key, value);

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testInteator() {
        Headers headers = new RecordHeaders();

        KafkaMqInjectCarrier injectCarrier = new KafkaMqInjectCarrier(headers);

        injectCarrier.iterator();
    }
}
