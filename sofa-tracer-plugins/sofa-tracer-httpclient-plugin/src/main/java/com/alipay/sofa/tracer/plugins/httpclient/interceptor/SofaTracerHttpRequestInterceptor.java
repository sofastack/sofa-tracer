package com.alipay.sofa.tracer.plugins.httpclient.interceptor;

import com.alipay.sofa.tracer.plugins.httpclient.HttpClientTracer;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.RequestLine;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * SofaTracerHttpRequestInterceptor
 *
 * @author yangguanchao
 * @since 2018/08/07
 */
public class SofaTracerHttpRequestInterceptor implements HttpRequestInterceptor {

    @Override
    public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
        //span generated
        HttpClientTracer httpClientTracer = HttpClientTracer.getHttpClientTracerSingleton();
        RequestLine requestLine = httpRequest.getRequestLine();
        String operationName = requestLine.getMethod();
        httpClientTracer.clientSend(operationName);
    }
}
