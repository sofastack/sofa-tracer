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

import com.alipay.sofa.tracer.spring.zipkin.properties.ZipkinProperties;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * ZipkinSofaTracerRestTemplateCustomizer
 * @author guolei.sgl
 */
public class ZipkinSofaTracerRestTemplateCustomizer {

    private ZipkinProperties.Compression compression;

    public ZipkinSofaTracerRestTemplateCustomizer(ZipkinProperties.Compression compression) {
        this.compression = compression;
    }

    public void customize(RestTemplate restTemplate) {
        if (this.compression == null || restTemplate == null) {
            return;
        }
        if (this.compression.isEnabled()) {
            restTemplate.getInterceptors().add(0, new GzipInterceptor());
        }
    }

    private class GzipInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                            ClientHttpRequestExecution execution)
                                                                                 throws IOException {
            request.getHeaders().add("Content-Encoding", "gzip");
            ByteArrayOutputStream gzipped = new ByteArrayOutputStream();
            GZIPOutputStream compressor = null;
            try {
                compressor = new GZIPOutputStream(gzipped);
                compressor.write(body);
            } finally {
                if (compressor != null) {
                    compressor.close();
                }
            }
            return execution.execute(request, gzipped.toByteArray());
        }
    }
}
