package com.alipay.sofa.tracer.plugins.httpclient.interceptor;

import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.sofa.tracer.plugins.httpclient.HttpClientTracer;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * SofaTracerHttpResponseInterceptor
 *
 * @author yangguanchao
 * @since 2018/08/07
 */
public class SofaTracerHttpResponseInterceptor implements HttpResponseInterceptor {

    @Override
    public void process(HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
        HttpClientTracer httpClientTracer = HttpClientTracer.getHttpClientTracerSingleton();
        //sync handled
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan httpClientSpan = sofaTraceContext.getCurrentSpan();
        //length
        httpClientSpan.setTag(CommonSpanTags.RESP_SIZE, httpResponse.getEntity().getContentLength());

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        httpClientTracer.clientReceive(String.valueOf(statusCode));
    }
}
