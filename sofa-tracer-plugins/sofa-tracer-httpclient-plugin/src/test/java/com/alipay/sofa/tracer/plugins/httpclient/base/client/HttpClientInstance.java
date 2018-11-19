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
package com.alipay.sofa.tracer.plugins.httpclient.base.client;

import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.httpclient.SofaTracerHttpClientBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.util.concurrent.TimeUnit;

/**
 * HttpClientInstance
 *
 * @author yangguanchao
 * @since 2018/08/08
 */
public class HttpClientInstance {

    /***
     * request timeout
     */
    private static final int    REQUEST_TIMEOUT = 10 * 1000;

    private RequestConfig       defaultRequestConfig;

    private CloseableHttpClient httpClient      = null;

    public HttpClientInstance(int soTimeoutMilliseconds) {
        if (soTimeoutMilliseconds < 0) {
            soTimeoutMilliseconds = REQUEST_TIMEOUT;
        }
        //request timeout milliseconds
        int connectionTimeout = 3 * 1000;
        //connect Manager get the Connection timeout milliseconds
        int connectionRequestTimeout = 3 * 1000;
        this.defaultRequestConfig = RequestConfig.custom().setConnectTimeout(connectionTimeout)
            .setSocketTimeout(soTimeoutMilliseconds)
            .setConnectionRequestTimeout(connectionRequestTimeout).build();
        this.httpClient = this.getHttpClient();
    }

    public String executeHead(String url) throws Exception {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        if (!url.startsWith("http://")) {
            url = "http://" + url;
        }
        HttpHead httpHead = new HttpHead(url);
        httpHead.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpHead.setConfig(defaultRequestConfig);
        return this.execute(httpHead);
    }

    public String executeGet(String url) throws Exception {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        if (!url.startsWith("http://")) {
            url = "http://" + url;
        }
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpget.setConfig(defaultRequestConfig);
        return this.execute(httpget);
    }

    public String executePost(String url, String content) throws Exception {
        //https://www.baeldung.com/httpclient-post-http-request
        if (!url.startsWith("http://")) {
            url = "http://" + url;
        }
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        StringEntity body = new StringEntity(content);
        body.setContentType("application/json");
        httpPost.setEntity(body);
        return execute(httpPost);
    }

    private String execute(HttpRequestBase requestBase) throws Exception {
        CloseableHttpResponse response = this.httpClient.execute(requestBase);
        int responseCode = response.getStatusLine().getStatusCode();
        if (HttpStatus.SC_OK != responseCode) {
            return null;
        }
        //https://memorynotfound.com/apache-httpclient-http-get-request-method-example/
        HttpEntity httpEntity = response.getEntity();
        return httpEntity != null ? EntityUtils.toString(httpEntity) : null;
    }

    private CloseableHttpClient getHttpClient() {
        if (this.httpClient != null) {
            return this.httpClient;
        }
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setDefaultMaxPerRoute(6);
        connManager.setMaxTotal(20);
        connManager.closeIdleConnections(120, TimeUnit.SECONDS);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        //SOFATracer
        SofaTracerHttpClientBuilder.clientBuilder(httpClientBuilder);
        httpClient = httpClientBuilder.setConnectionManager(connManager).disableAutomaticRetries()
            .setUserAgent("CLIENT_VERSION").build();
        return httpClient;
    }
}
