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
package com.alipay.sofa.tracer.plugins.springmvc;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.registry.ExtendFormat;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.HashMap;

/**
 * @author qilong.zql
 * @since 2.3.0
 */
public class WebfluxSofaTracerFilter implements WebFilter {

    private String          appName = StringUtils.EMPTY_STRING;

    private SpringMvcTracer springMvcTracer;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if(this.springMvcTracer == null) {
            this.springMvcTracer = SpringMvcTracer.getSpringMvcTracerSingleton();
        }
        Mono<Void> ret = null;
        SofaTracerSpan springMvcSpan = null;
        long responseSize = -1;
        int httpStatus = -1;
        try {
            ServerHttpRequest serverRequest = exchange.getRequest();
            ServerHttpResponse serverResponse = exchange.getResponse();
            SofaTracerSpanContext spanContext = getSpanContextFromRequest(serverRequest);
            springMvcSpan = springMvcTracer.serverReceive(spanContext);
            if (StringUtils.isBlank(this.appName)) {
                this.appName = SofaTracerConfiguration
                        .getProperty(SofaTracerConfiguration.TRACER_APPNAME_KEY);
            }
            springMvcSpan.setOperationName(serverRequest.getPath().toString());
            springMvcSpan.setTag(CommonSpanTags.LOCAL_APP, appName);
            springMvcSpan.setTag(CommonSpanTags.REQUEST_URL, serverRequest.getPath().toString());
            springMvcSpan.setTag(CommonSpanTags.METHOD, serverRequest.getMethod().name());
            springMvcSpan.setTag(CommonSpanTags.REQ_SIZE, serverRequest.getBody().map((dataBuffer)->dataBuffer.readableByteCount()).reduce((a, b) -> a + b).block());

            chain.filter(exchange);
            httpStatus = serverResponse.getStatusCode().value();
            return ret;
        } catch (Throwable t) {
            SelfLog.error("Spring MVC Tracer error occurs in WebfluxSofaTracerFilter.doFilter.",
                    t);
        } finally {
            if (springMvcSpan != null) {
                springMvcSpan.setTag(CommonSpanTags.RESP_SIZE, responseSize);
                //ss
                springMvcTracer.serverSend(String.valueOf(httpStatus));
            }
            return Mono.empty();
        }
    }

    /***
     * Extract tracing context from request received from previous node
     * @param serverHttpRequest Servlet http request object
     * @return SofaTracerSpanContext Tracing context extract from request
     */
    public SofaTracerSpanContext getSpanContextFromRequest(ServerHttpRequest serverHttpRequest) {
        HttpHeaders httpHeaders = serverHttpRequest.getHeaders();
        HashMap<String, String> headers = new HashMap<String, String>(
            httpHeaders.toSingleValueMap());
        SofaTracer tracer = springMvcTracer.getSofaTracer();
        SofaTracerSpanContext spanContext = (SofaTracerSpanContext) tracer.extract(
            ExtendFormat.Builtin.B3_HTTP_HEADERS, new SpringMvcHeadersCarrier(headers));
        spanContext.setSpanId(spanContext.nextChildContextId());
        return spanContext;
    }

}