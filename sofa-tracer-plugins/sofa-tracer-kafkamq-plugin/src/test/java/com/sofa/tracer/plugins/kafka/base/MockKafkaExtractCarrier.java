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

import com.sofa.alipay.tracer.plugins.kafkamq.carrier.KafkaMqExtractCarrier;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.Test;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * copy chenchen6's rabbit mq ExtractCarrierMock
 *
 * @author chenchen6 2020/9/10 23:27
 * @since 3.1.0
 */
public class MockKafkaExtractCarrier {

    @Test
    public void testInterator() {
        //put key and value.
        String key = "testSOFATracerId";
        String value = "testSOFATracerValue";
        Headers headers = new RecordHeaders();
        headers.add(key, value.getBytes());
        //put header to carrier.
        KafkaMqExtractCarrier extractCarrier = new KafkaMqExtractCarrier(headers);
        Map.Entry<String, String> headerEntry = extractCarrier.iterator().next();
        String entryKey = headerEntry.getKey();
        String entryValue = headerEntry.getValue();
        //assert.
        Assert.notNull(headerEntry, "header should not be null.");
        Assert.state(key.equals(entryKey), "key and entry key should be same.");
        Assert.state(value.equals(entryValue), "value and entry value should be same.");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPutHeader2HeadersMap() {
        Headers headers = new RecordHeaders();
        KafkaMqExtractCarrier extractCarrier = new KafkaMqExtractCarrier(headers);

        extractCarrier.put("testSofaTracerKey", "testSofaTracerValue");
    }

}
