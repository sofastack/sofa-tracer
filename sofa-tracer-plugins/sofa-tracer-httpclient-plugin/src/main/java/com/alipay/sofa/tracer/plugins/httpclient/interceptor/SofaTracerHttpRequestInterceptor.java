package com.alipay.sofa.tracer.plugins.httpclient.interceptor;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.httpclient.HttpClientTracer;
import org.apache.http.*;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * SofaTracerHttpRequestInterceptor
 *
 * @author yangguanchao
 * @since 2018/08/07
 */
public class SofaTracerHttpRequestInterceptor implements HttpRequestInterceptor {

    private String appName = null;

    private String targetAppName = null;

    public SofaTracerHttpRequestInterceptor(String appName, String targetAppName) {
        this.appName = appName;
        this.targetAppName = targetAppName;
    }

    @Override
    public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
        if (this.appName == null) {
            this.appName = SofaTracerConfiguration
                    .getProperty(SofaTracerConfiguration.TRACER_APPNAME_KEY, StringUtils.EMPTY_STRING);
        }
        //lazy init
        HttpClientTracer httpClientTracer = HttpClientTracer.getHttpClientTracerSingleton();
        RequestLine requestLine = httpRequest.getRequestLine();
        String methodName = requestLine.getMethod();
        //span generated
        SofaTracerSpan httpClientSpan = httpClientTracer.clientSend(methodName);
        //appName
        httpClientSpan.setTag(CommonSpanTags.LOCAL_APP, this.appName == null ? StringUtils.EMPTY_STRING : this.appName);
        //targetAppName
        httpClientSpan.setTag(CommonSpanTags.REMOTE_APP, this.targetAppName == null ? StringUtils.EMPTY_STRING : this.targetAppName);
        //url ((HttpRequestWrapper) request).getOriginal().getRequestLine().getUri()
        if (httpRequest instanceof HttpRequestWrapper) {
            HttpRequestWrapper httpRequestWrapper = (HttpRequestWrapper) httpRequest;
            httpClientSpan.setTag(CommonSpanTags.REQUEST_URL, httpRequestWrapper.getOriginal().getRequestLine().getUri());
        } else {
            httpClientSpan.setTag(CommonSpanTags.REQUEST_URL, requestLine.getUri());
        }
        //method
        httpClientSpan.setTag(CommonSpanTags.METHOD, methodName);
        //length
        if (httpRequest instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest httpEntityEnclosingRequest = (HttpEntityEnclosingRequest) httpRequest;
            httpClientSpan.setTag(CommonSpanTags.REQ_SIZE, httpEntityEnclosingRequest.getEntity().getContentLength());
        }
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
