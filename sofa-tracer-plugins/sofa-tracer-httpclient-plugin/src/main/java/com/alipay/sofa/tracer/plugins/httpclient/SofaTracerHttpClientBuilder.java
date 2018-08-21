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
package com.alipay.sofa.tracer.plugins.httpclient;

import com.alipay.common.tracer.core.tracer.AbstractTracer;
import com.alipay.sofa.tracer.plugins.httpclient.interceptor.SofaTracerAsyncHttpInterceptor;
import com.alipay.sofa.tracer.plugins.httpclient.interceptor.SofaTracerHttpInterceptor;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;

/**
 * SofaTracerHttpClientBuilder
 *
 * @author yangguanchao
 * @since 2018/08/07
 */
public class SofaTracerHttpClientBuilder {

    protected static AbstractTracer httpClientTracer = null;

    public static HttpClientBuilder clientBuilder(HttpClientBuilder clientBuilder) {
        return clientBuilder(clientBuilder, null, null);
    }

    public static HttpClientBuilder clientBuilder(HttpClientBuilder clientBuilder,
                                                  String currentApp, String targetApp) {
        SofaTracerHttpInterceptor interceptor = new SofaTracerHttpInterceptor(
            getHttpClientTracer(), currentApp, targetApp);
        return clientBuilder.addInterceptorFirst((HttpRequestInterceptor) interceptor)
            .addInterceptorFirst((HttpResponseInterceptor) interceptor);
    }

    public static HttpAsyncClientBuilder asyncClientBuilder(HttpAsyncClientBuilder httpAsyncClientBuilder) {
        return asyncClientBuilder(httpAsyncClientBuilder, null, null);
    }

    public static HttpAsyncClientBuilder asyncClientBuilder(HttpAsyncClientBuilder httpAsyncClientBuilder,
                                                            String currentApp, String targetApp) {
        SofaTracerAsyncHttpInterceptor interceptor = new SofaTracerAsyncHttpInterceptor(
            getHttpClientTracer(), currentApp, targetApp);
        return httpAsyncClientBuilder.addInterceptorFirst((HttpRequestInterceptor) interceptor)
            .addInterceptorFirst((HttpResponseInterceptor) interceptor);
    }

    public static AbstractTracer getHttpClientTracer() {
        if (httpClientTracer == null) {
            synchronized (SofaTracerHttpClientBuilder.class) {
                if (httpClientTracer == null) {
                    //default json format
                    httpClientTracer = HttpClientTracer.getHttpClientTracerSingleton();
                }
            }
        }
        return httpClientTracer;
    }
}
