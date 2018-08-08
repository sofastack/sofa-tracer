package com.alipay.sofa.tracer.plugins.httpclient;

import com.alipay.sofa.tracer.plugins.httpclient.interceptor.SofaTracerHttpRequestInterceptor;
import com.alipay.sofa.tracer.plugins.httpclient.interceptor.SofaTracerHttpResponseInterceptor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;

/**
 * SofaTracerHttpClientBuilder
 *
 * @author yangguanchao
 * @since 2018/08/07
 */
public class SofaTracerHttpClientBuilder {

    public static HttpAsyncClientBuilder asyncClientBuilder(HttpAsyncClientBuilder httpAsyncClientBuilder) {
        return asyncClientBuilder(httpAsyncClientBuilder, null, null);
    }

    public static HttpAsyncClientBuilder asyncClientBuilder(HttpAsyncClientBuilder httpAsyncClientBuilder, String currentApp, String targetApp) {
        return httpAsyncClientBuilder
                .addInterceptorFirst(new SofaTracerHttpRequestInterceptor(currentApp, targetApp))
                .addInterceptorFirst(new SofaTracerHttpResponseInterceptor());
    }

    public static HttpClientBuilder clientBuilder(HttpClientBuilder clientBuilder) {
        return clientBuilder(clientBuilder, null, null);
    }

    public static HttpClientBuilder clientBuilder(HttpClientBuilder clientBuilder, String currentApp, String targetApp) {
        return clientBuilder
                .addInterceptorFirst(new SofaTracerHttpRequestInterceptor(currentApp, targetApp))
                .addInterceptorFirst(new SofaTracerHttpResponseInterceptor());
    }
}
