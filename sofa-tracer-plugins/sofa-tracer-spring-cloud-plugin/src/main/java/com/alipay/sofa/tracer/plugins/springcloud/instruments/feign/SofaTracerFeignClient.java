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
package com.alipay.sofa.tracer.plugins.springcloud.instruments.feign;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.registry.ExtendFormat;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.springcloud.carriers.FeignRequestCarrier;
import com.alipay.sofa.tracer.plugins.springcloud.tracers.FeignClientTracer;
import feign.Client;
import feign.Request;
import feign.Response;
import io.opentracing.tag.Tags;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 10:50 AM
 * @since:
 **/
public class SofaTracerFeignClient implements Client {

    private Client            delegate;

    private FeignClientTracer feignClientTracer;

    public SofaTracerFeignClient(Client delegate) {
        this.delegate = delegate;
    }

    @Override
    public Response execute(Request request, Request.Options options) {
        if (feignClientTracer == null) {
            feignClientTracer = FeignClientTracer.getFeignClientTracerSingleton();
        }
        SofaTracerSpan sofaTracerSpan = null;
        int resultCode = -1;
        try {
            sofaTracerSpan = feignClientTracer.clientSend(request.method());
            // set tags
            try {
                appendRequestSpanTagsAndInject(request, sofaTracerSpan);
            } catch (UnsupportedOperationException e) {
                // if header is Unmodifiable，renew request
                request = Request.create(request.httpMethod(), request.url(), new LinkedHashMap<>(
                    request.headers()), request.requestBody());
                // ignore appendRequestSpanTags,appendRequestSpanTagsAndInject has do it
                injectCarrier(request, sofaTracerSpan);
            }
            Response response = delegate.execute(request, options);
            // set result tags
            appendResponseSpanTags(response, sofaTracerSpan);
            resultCode = response.status();
            return response;
        } catch (Exception ex) {
            appendExceptionSpanTags(request, sofaTracerSpan, ex);
            throw new RuntimeException(ex);
        } finally {
            if (sofaTracerSpan != null) {
                // do clear tl
                feignClientTracer.clientReceive(String.valueOf(resultCode));
            }
        }
    }

    /**
     * append exception tags for current span
     * @param request
     * @param sofaTracerSpan
     */
    private void appendExceptionSpanTags(Request request, SofaTracerSpan sofaTracerSpan,
                                         Exception ex) {
        appendRequestSpanTags(request, sofaTracerSpan);
        sofaTracerSpan.setTag(CommonSpanTags.RESULT_CODE, -1);
        sofaTracerSpan.setTag(Tags.ERROR.getKey(), ex.getMessage());
    }

    private String[] parseRemoteHostAndPort(Request request) {
        String[] hostWithPort = new String[2];
        URL requestUrl = null;
        try {
            requestUrl = new URL(request.url());
        } catch (MalformedURLException e) {
            SelfLog.error("cannot parse remote host and port. request:" + request.url(), e);
        }
        hostWithPort[0] = requestUrl != null ? requestUrl.getHost() : "";
        hostWithPort[1] = String.valueOf(requestUrl != null ? requestUrl.getPort() : -1);
        return hostWithPort;
    }

    /**
     * append response tags for current span
     * @param response
     * @param sofaTracerSpan
     */
    private void appendResponseSpanTags(Response response, SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null) {
            return;
        }
        Integer responseSize = null;
        if (response.body() != null) {
            responseSize = response.body().length();
        }
        sofaTracerSpan.setTag(CommonSpanTags.RESP_SIZE, responseSize);
        sofaTracerSpan.setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread().getName());
        sofaTracerSpan.setTag(CommonSpanTags.RESULT_CODE, response.status());
    }

    /**
     * append request tags for current span
     * @param request
     * @param sofaTracerSpan
     */
    private void appendRequestSpanTagsAndInject(Request request, SofaTracerSpan sofaTracerSpan) {

        appendRequestSpanTags(request, sofaTracerSpan);
        //carrier
        this.injectCarrier(request, sofaTracerSpan);
    }

    private void appendRequestSpanTags(Request request, SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null) {
            return;
        }
        // appName
        String appName = SofaTracerConfiguration.getProperty(
            SofaTracerConfiguration.TRACER_APPNAME_KEY, StringUtils.EMPTY_STRING);
        //methodName
        String methodName = request.method();
        //appName
        sofaTracerSpan.setTag(CommonSpanTags.LOCAL_APP, appName == null ? StringUtils.EMPTY_STRING
            : appName);
        sofaTracerSpan.setTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
        sofaTracerSpan.setTag(CommonSpanTags.REMOTE_APP, StringUtils.EMPTY_STRING);
        sofaTracerSpan.setTag(CommonSpanTags.REQUEST_URL, request.url());
        sofaTracerSpan.setTag(CommonSpanTags.METHOD, methodName);
        String[] hostWithPort = parseRemoteHostAndPort(request);
        sofaTracerSpan.setTag(CommonSpanTags.REMOTE_HOST, hostWithPort[0]);
        sofaTracerSpan.setTag(CommonSpanTags.REMOTE_PORT, hostWithPort[1]);

        if (request.body() != null) {
            sofaTracerSpan.setTag(CommonSpanTags.REQ_SIZE, request.body().length);
        } else {
            sofaTracerSpan.setTag(CommonSpanTags.REQ_SIZE, 0);
        }
    }

    private void injectCarrier(Object request, SofaTracerSpan currentSpan) {
        if (request instanceof Request) {
            SofaTracer sofaTracer = this.feignClientTracer.getSofaTracer();
            sofaTracer.inject(currentSpan.getSofaTracerSpanContext(),
                ExtendFormat.Builtin.B3_HTTP_HEADERS, new FeignRequestCarrier((Request) request));
        }
    }
}
