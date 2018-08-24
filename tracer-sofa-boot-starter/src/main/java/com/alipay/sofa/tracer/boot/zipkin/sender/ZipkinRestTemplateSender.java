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
package com.alipay.sofa.tracer.boot.zipkin.sender;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;
import zipkin2.Call;
import zipkin2.codec.Encoding;
import zipkin2.reporter.BytesMessageEncoder;
import zipkin2.reporter.Sender;
import java.net.URI;
import java.util.List;

/**
 * ZipkinRestTemplateSender
 *
 * @author yangguanchao
 * @since 2018/05/01
 */
public class ZipkinRestTemplateSender extends Sender {

    private RestTemplate restTemplate;

    private String       url;

    public ZipkinRestTemplateSender(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.url = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "api/v2/spans";
    }

    @Override
    public Encoding encoding() {
        return Encoding.JSON;
    }

    @Override
    public int messageMaxBytes() {
        // Max span size is 2MB
        return 2 * 1024 * 1024;
    }

    @Override
    public int messageSizeInBytes(List<byte[]> spans) {
        return encoding().listSizeInBytes(spans);
    }

    @Override
    public Call<Void> sendSpans(List<byte[]> encodedSpans) {
        try {
            byte[] message = BytesMessageEncoder.JSON.encode(encodedSpans);
            post(message);
        } catch (Throwable e) {
            if (e instanceof Error) {
                throw (Error) e;
            }
        }
        return null;
    }

    private void post(byte[] json) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        RequestEntity<byte[]> requestEntity = new RequestEntity<>(json, httpHeaders,
            HttpMethod.POST, URI.create(this.url));
        this.restTemplate.exchange(requestEntity, String.class);
    }
}
