package com.alipay.sofa.tracer.plugins.httpclient;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.utils.StringUtils;
import io.opentracing.Span;
import io.opentracing.tag.Tags;
import org.apache.http.*;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.protocol.HttpContext;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public interface SofaTracerClientSpanDecorator {

    /**
     * Decorate span before request is fired.
     *
     * @param request request
     * @param httpContext context
     * @param span span to decorate
     */
    void onRequest(HttpRequestWrapper request, HttpContext httpContext, Span span);

    /**
     * Decorate span after response is received.
     *
     * @param response response
     * @param httpContext context
     * @param span span to decorate
     */
    void onResponse(HttpResponse response, HttpContext httpContext, Span span);

    /**
     *  Decorate span span on error e.g. {@link java.net.UnknownHostException}/
     *
     * @param request request
     * @param httpContext context
     * @param ex exception
     * @param span span to decorate
     */
    void onError(HttpRequest request, HttpContext httpContext, Exception ex, Span span);

    /**
     * Decorator which adds standard set of tags and logs.
     */
    class StandardTags implements SofaTracerClientSpanDecorator {
        private static final Logger log = Logger.getLogger(StandardTags.class.getName());
        protected String              appName;

        protected String              targetAppName;

        @Override
        public void onRequest(HttpRequestWrapper httpRequest, HttpContext httpContext, Span httpClientSpan) {
            if (httpClientSpan == null) {
                return;
            }
            if (this.appName == null) {
                this.appName = SofaTracerConfiguration.getProperty(
                        SofaTracerConfiguration.TRACER_APPNAME_KEY, StringUtils.EMPTY_STRING);
            }
            //lazy init
            RequestLine requestLine = httpRequest.getRequestLine();
            String methodName = requestLine.getMethod();
            //appName
            httpClientSpan.setTag(CommonSpanTags.LOCAL_APP,
                    this.appName == null ? StringUtils.EMPTY_STRING : this.appName);
            //targetAppName
            httpClientSpan.setTag(CommonSpanTags.REMOTE_APP,
                    this.targetAppName == null ? StringUtils.EMPTY_STRING : this.targetAppName);
            //URL
            HttpRequest request = httpRequest.getOriginal();
            httpClientSpan.setTag(CommonSpanTags.REQUEST_URL, request
                    .getRequestLine().getUri());

            //method
            httpClientSpan.setTag(CommonSpanTags.METHOD, methodName);

            //length
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntityEnclosingRequest httpEntityEnclosingRequest = (HttpEntityEnclosingRequest) request;
                HttpEntity httpEntity = httpEntityEnclosingRequest.getEntity();

                httpClientSpan.setTag(CommonSpanTags.REQ_SIZE,
                        httpEntity == null ? -1 : httpEntity.getContentLength());
                long contentL = httpEntity == null ? -1 : httpEntity.getContentLength();
                System.out.println("=-=-=--=-==-=-=--=-=-="+contentL);
            }
//            HttpEntityEnclosingRequest httpEntityEnclosingRequest = (HttpEntityEnclosingRequest) request;
//            HttpEntity httpEntity = httpEntityEnclosingRequest.getEntity();
//            httpClientSpan.setTag(CommonSpanTags.REQ_SIZE,
//                    httpEntity == null ? -1 : httpEntity.getContentLength());

        }


        @Override
        public void onResponse(HttpResponse response, HttpContext httpContext, Span span) {
            // log event
            span.log(LogData.CLIENT_RECV_EVENT_VALUE);
            // set resultCode
            span.setTag(CommonSpanTags.RESULT_CODE, response.getStatusLine().getStatusCode());
            HttpEntity httpEntity = response.getEntity();
            long contentLength = httpEntity == null ? -1 : httpEntity.getContentLength();
            span.setTag(CommonSpanTags.RESP_SIZE, contentLength);
            span.setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread()
                    .getName());

        }

        @Override
        public void onError(HttpRequest request, HttpContext httpContext, Exception ex, Span span) {
            Tags.ERROR.set(span, Boolean.TRUE);

            Map<String, Object> errorLogs = new HashMap<>(2);
            errorLogs.put("event", Tags.ERROR.getKey());
            errorLogs.put("error.object", ex);
            span.log(errorLogs);
        }
    }




}
