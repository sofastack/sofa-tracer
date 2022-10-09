package com.alipay.sofa.tracer.plugins.httpclient;

import com.alipay.common.tracer.core.tracer.AbstractTracer;
import com.alipay.sofa.tracer.plugins.httpclient.interceptor.SofaTracerAsyncHttpInterceptor;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;

public class SofaTracerAsyncHttpClientBuilder {

    protected static AbstractTracer httpClientTracer = null;

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
