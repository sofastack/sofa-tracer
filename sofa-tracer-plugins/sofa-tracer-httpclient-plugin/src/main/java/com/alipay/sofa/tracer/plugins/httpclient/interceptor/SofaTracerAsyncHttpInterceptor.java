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

import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractTracer;
import io.opentracing.Scope;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.RequestLine;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * SofaTracerHttpInterceptor
 *
 * @author yangguanchao
 * @since 2018/08/07
 */
public class SofaTracerAsyncHttpInterceptor extends AbstractHttpRequestInterceptor
                                                                                  implements
                                                                                  HttpRequestInterceptor,
                                                                                  HttpResponseInterceptor {

    public SofaTracerAsyncHttpInterceptor(AbstractTracer httpClientTracer, String appName,
                                          String targetAppName) {

        super(httpClientTracer, appName, targetAppName);
    }

    @Override
    public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException,
                                                                         IOException {
        //lazy init
        RequestLine requestLine = httpRequest.getRequestLine();
        String methodName = requestLine.getMethod();
        //span generated
        SofaTracerSpan httpClientSpan = httpClientTracer.clientSend(methodName);
        super.appendHttpClientRequestSpanTags(httpRequest, httpClientSpan);
        //async handle
        httpContext.setAttribute(CURRENT_ASYNC_HTTP_SPAN_KEY, httpClientSpan);
//        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
//        //client span
//        if (httpClientSpan.getParentSofaTracerSpan() != null) {
//            //restore parent
//            sofaTraceContext.push(httpClientSpan.getParentSofaTracerSpan());
//        } else {
//            //pop async span
//            sofaTraceContext.pop();
//        }
    }




    @Override
    public void process(HttpResponse httpResponse, HttpContext httpContext) throws HttpException,
                                                                           IOException {
        SofaTracerSpan httpClientSpan = (SofaTracerSpan) httpContext
            .getAttribute(CURRENT_ASYNC_HTTP_SPAN_KEY);
        //tag append
        super.appendHttpClientResponseSpanTags(httpResponse, httpClientSpan);
        //finish
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        httpClientTracer.clientReceiveTagFinish(httpClientSpan, String.valueOf(statusCode));
    }
}
