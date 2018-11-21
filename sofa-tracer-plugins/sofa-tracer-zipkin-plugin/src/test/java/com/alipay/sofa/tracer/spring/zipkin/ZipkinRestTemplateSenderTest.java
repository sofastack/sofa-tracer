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
package com.alipay.sofa.tracer.spring.zipkin;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.sofa.tracer.spring.zipkin.properties.ZipkinProperties;
import com.alipay.sofa.tracer.spring.zipkin.sender.ZipkinRestTemplateSender;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;
import zipkin2.Call;
import zipkin2.codec.Encoding;

import java.util.ArrayList;
import java.util.List;

/**
 * ZipkinRestTemplateSenderTest
 *
 * @author: guolei.sgl
 * @since: v2.3.0
 **/
public class ZipkinRestTemplateSenderTest {

    ZipkinRestTemplateSender zipkinRestTemplateSender;
    RestTemplate             restTemplate;

    @Before
    public void init() {
        restTemplate = new RestTemplate();
        zipkinRestTemplateSender = new ZipkinRestTemplateSender(restTemplate,
            SofaTracerConfiguration.getProperty(ZipkinProperties.ZIPKIN_BASE_URL_KEY));
    }

    @Test
    public void encoding() {
        Encoding encoding = zipkinRestTemplateSender.encoding();
        Assert.assertTrue(encoding.equals(Encoding.JSON));
    }

    @Test
    public void messageMaxBytes() {
        Assert.assertTrue(zipkinRestTemplateSender.messageMaxBytes() == 2 * 1024 * 1024);
    }

    @Test
    public void messageSizeInBytes() {
        byte[] tests = new byte[1];
        tests[0] = 0;
        List<byte[]> spans = new ArrayList<byte[]>();
        spans.add(tests);
        int i = zipkinRestTemplateSender.messageSizeInBytes(spans);
        Assert.assertTrue(i == 3);
    }

    @Test
    public void sendSpans() throws InterruptedException {
        byte[] tests = new byte[1];
        tests[0] = 0;
        List<byte[]> encodedSpans = new ArrayList<byte[]>();
        encodedSpans.add(tests);
        Call<Void> voidCall = zipkinRestTemplateSender.sendSpans(encodedSpans);
        Thread.sleep(500);
        Assert.assertTrue(voidCall == null);
    }
}
