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
package com.alipay.sofa.tracer.plugins.httpclient.interceptor;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.registry.ExtendFormat;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractTracer;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.httpclient.HttpClientRequestCarrier;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpRequestWrapper;

/**
 * AbstractHttpRequestInterceptor
 *
 * @author yangguanchao
 * @since 2018/08/12
 */
public abstract class AbstractHttpRequestInterceptor {

    protected static final String CURRENT_ASYNC_HTTP_SPAN_KEY = "httpclient.async.span.key";

    protected AbstractTracer      httpClientTracer;

    protected String              appName;

    protected String              targetAppName;

    public AbstractHttpRequestInterceptor(AbstractTracer httpClientTracer, String appName,
                                          String targetAppName) {
        this.httpClientTracer = httpClientTracer;
        this.appName = appName;
        this.targetAppName = targetAppName;
    }

    public void appendHttpClientRequestSpanTags(HttpRequest httpRequest,
                                                SofaTracerSpan httpClientSpan) {
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
        if (httpRequest instanceof HttpRequestWrapper) {
            HttpRequestWrapper httpRequestWrapper = (HttpRequestWrapper) httpRequest;
            httpClientSpan.setTag(CommonSpanTags.REQUEST_URL, httpRequestWrapper.getOriginal()
                .getRequestLine().getUri());
        } else {
            httpClientSpan.setTag(CommonSpanTags.REQUEST_URL, requestLine.getUri());
        }
        //method
        httpClientSpan.setTag(CommonSpanTags.METHOD, methodName);
        //length
        if (httpRequest instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest httpEntityEnclosingRequest = (HttpEntityEnclosingRequest) httpRequest;
            HttpEntity httpEntity = httpEntityEnclosingRequest.getEntity();
            httpClientSpan.setTag(CommonSpanTags.REQ_SIZE,
                httpEntity == null ? -1 : httpEntity.getContentLength());
        }
        //carrier
        this.processHttpClientRequestCarrier(httpRequest, httpClientSpan);
    }

    public void appendHttpClientResponseSpanTags(HttpResponse httpResponse,
                                                 SofaTracerSpan httpClientSpan) {
        //length
        if (httpClientSpan != null) {
            HttpEntity httpEntity = httpResponse.getEntity();
            long contentLength = httpEntity == null ? -1 : httpEntity.getContentLength();
            httpClientSpan.setTag(CommonSpanTags.RESP_SIZE, contentLength);
            httpClientSpan.setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread()
                .getName());
        }
    }

    public void processHttpClientRequestCarrier(HttpRequest httpRequest, SofaTracerSpan currentSpan) {
        SofaTracer sofaTracer = this.httpClientTracer.getSofaTracer();
        sofaTracer.inject(currentSpan.getSofaTracerSpanContext(),
            ExtendFormat.Builtin.B3_HTTP_HEADERS, new HttpClientRequestCarrier(httpRequest));
    }
}
