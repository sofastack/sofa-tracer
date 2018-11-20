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
package com.sofa.alipay.tracer.plugins.rest.interceptor;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.registry.ExtendFormat;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractTracer;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.sofa.alipay.tracer.plugins.rest.RestTemplateRequestCarrier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.AsyncClientHttpRequestExecution;
import org.springframework.http.client.AsyncClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import java.io.IOException;
import java.util.List;

/**
 * AsyncRestTemplateRequestInterceptor
 * @author: guolei.sgl
 * @since : v2.3.0
 */
public class AsyncRestTemplateRequestInterceptor implements AsyncClientHttpRequestInterceptor {

    protected AbstractTracer restTemplateTracer;

    public AsyncRestTemplateRequestInterceptor(AbstractTracer restTemplateTracer) {
        this.restTemplateTracer = restTemplateTracer;
    }

    @Override
    public ListenableFuture<ClientHttpResponse> intercept(HttpRequest request, byte[] body,
                                                          AsyncClientHttpRequestExecution execution)
                                                                                                    throws IOException {
        SofaTracerSpan sofaTracerSpan = restTemplateTracer.clientSend(request.getMethod().name());
        appendRestTemplateRequestSpanTags(request, sofaTracerSpan);
        try {
            ListenableFuture<ClientHttpResponse> result = execution.executeAsync(request, body);
            result.addCallback(new SofaTraceListenableFutureCallback(restTemplateTracer,
                sofaTracerSpan));
            return result;
        } catch (RuntimeException e) {
            restTemplateTracer.clientReceiveTagFinish(sofaTracerSpan, String.valueOf(500));
            throw e;
        }
    }

    /**
     * ListenableFutureCallback instance under Async situation
     */
    static final class SofaTraceListenableFutureCallback
                                                        implements
                                                        ListenableFutureCallback<ClientHttpResponse> {

        final AbstractTracer restTemplateTracer;
        final SofaTracerSpan sofaTracerSpan;

        SofaTraceListenableFutureCallback(AbstractTracer restTemplateTracer,
                                          SofaTracerSpan sofaTracerSpan) {
            this.restTemplateTracer = restTemplateTracer;
            this.sofaTracerSpan = sofaTracerSpan;
        }

        @Override
        public void onFailure(Throwable throwable) {
            restTemplateTracer.clientReceiveTagFinish(sofaTracerSpan, String.valueOf(500));
        }

        @Override
        public void onSuccess(ClientHttpResponse response) {
            //finish
            try {
                int statusCode = response.getStatusCode().value();
                appendRestTemplateResponseSpanTags(response, sofaTracerSpan);
                restTemplateTracer.clientReceiveTagFinish(sofaTracerSpan,
                    String.valueOf(statusCode));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * add response tag
         * @param response
         * @param sofaTracerSpan
         */
        private void appendRestTemplateResponseSpanTags(ClientHttpResponse response,
                                                        SofaTracerSpan sofaTracerSpan) {
            if (sofaTracerSpan == null) {
                return;
            }
            HttpHeaders headers = response.getHeaders();
            //content length
            List<String> contentLengthList = headers.get("Content-Length");
            if (headers != null && contentLengthList != null && !contentLengthList.isEmpty()) {
                String len = contentLengthList.get(0);
                sofaTracerSpan.setTag(CommonSpanTags.RESP_SIZE, len);
            }
            // current thread name
            sofaTracerSpan.setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread()
                .getName());
        }
    }

    /**
     * add request tag
     * @param request
     * @param sofaTracerSpan
     */
    private void appendRestTemplateRequestSpanTags(HttpRequest request,
                                                   SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null) {
            return;
        }
        //appName
        String appName = SofaTracerConfiguration.getProperty(
            SofaTracerConfiguration.TRACER_APPNAME_KEY, StringUtils.EMPTY_STRING);
        //methodName
        String methodName = request.getMethod().name();
        //appName
        sofaTracerSpan.setTag(CommonSpanTags.LOCAL_APP, appName == null ? StringUtils.EMPTY_STRING
            : appName);
        //targetAppName
        sofaTracerSpan.setTag(CommonSpanTags.REMOTE_APP, StringUtils.EMPTY_STRING);
        sofaTracerSpan.setTag(CommonSpanTags.REQUEST_URL, request.getURI().toString());
        //method
        sofaTracerSpan.setTag(CommonSpanTags.METHOD, methodName);
        HttpHeaders headers = request.getHeaders();
        //reqSize
        if (headers != null && headers.containsKey("Content-Length")) {
            List<String> contentLengthList = headers.get("Content-Length");
            if (contentLengthList != null && !contentLengthList.isEmpty()) {
                sofaTracerSpan.setTag(CommonSpanTags.REQ_SIZE, contentLengthList.get(0));
            }
        } else {
            sofaTracerSpan.setTag(CommonSpanTags.REQ_SIZE, String.valueOf(-1));
        }
        //carrier
        this.injectCarrier(request, sofaTracerSpan);
    }

    public void injectCarrier(HttpRequest request, SofaTracerSpan currentSpan) {
        SofaTracer sofaTracer = this.restTemplateTracer.getSofaTracer();
        sofaTracer.inject(currentSpan.getSofaTracerSpanContext(),
            ExtendFormat.Builtin.B3_HTTP_HEADERS, new RestTemplateRequestCarrier(request));
    }
}
