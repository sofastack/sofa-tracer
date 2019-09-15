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
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.registry.ExtendFormat;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractTracer;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.sofa.alipay.tracer.plugins.rest.RestTemplateRequestCarrier;
import io.opentracing.tag.Tags;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.List;

/**
 * RestTemplateInterceptor
 * @author: guolei.sgl
 * @since:  v2.3.0
 */
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    protected AbstractTracer restTemplateTracer;

    public RestTemplateInterceptor(AbstractTracer restTemplateTracer) {
        this.restTemplateTracer = restTemplateTracer;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        SofaTracerSpan sofaTracerSpan = restTemplateTracer.clientSend(request.getMethod().name());
        appendRestTemplateRequestSpanTags(request, sofaTracerSpan);
        ClientHttpResponse response = null;
        Throwable t = null;
        try {
            return response = execution.execute(request, body);
        } catch (IOException e) {
            t = e;
            throw e;
        } finally {
            SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
            SofaTracerSpan currentSpan = sofaTraceContext.getCurrentSpan();
            String resultCode = SofaTracerConstant.RESULT_CODE_ERROR;
            // is get error
            if (t != null) {
                currentSpan.setTag(Tags.ERROR.getKey(), t.getMessage());
                // current thread name
                sofaTracerSpan.setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread()
                    .getName());
            }
            if (response != null) {
                //tag append
                appendRestTemplateResponseSpanTags(response, currentSpan);
                //finish
                resultCode = String.valueOf(response.getStatusCode().value());
            }
            restTemplateTracer.clientReceive(resultCode);
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
        if (headers != null && headers.get("Content-Length") != null
            && !headers.get("Content-Length").isEmpty()) {
            List<String> contentLengthList = headers.get("Content-Length");
            String len = contentLengthList.get(0);
            sofaTracerSpan.setTag(CommonSpanTags.RESP_SIZE, Long.valueOf(len));
        }
        // current thread name
        sofaTracerSpan.setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread().getName());
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
        sofaTracerSpan.setTag(CommonSpanTags.REQUEST_URL, request.getURI().toString());
        //method
        sofaTracerSpan.setTag(CommonSpanTags.METHOD, methodName);
        HttpHeaders headers = request.getHeaders();
        //reqSize
        if (headers != null && headers.containsKey("Content-Length")) {
            List<String> contentLengthList = headers.get("Content-Length");
            if (contentLengthList != null && !contentLengthList.isEmpty()) {
                sofaTracerSpan.setTag(CommonSpanTags.REQ_SIZE,
                    Long.valueOf(contentLengthList.get(0)));
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
