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
package com.alipay.sofa.tracer.plugins.webflux;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.reactor.SofaTracerReactorTransformer;
import com.alipay.common.tracer.core.registry.ExtendFormat;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractTracer;
import com.alipay.common.tracer.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;

/**
 * @author qilong.zql
 * @since 3.0.0
 */
public class WebfluxSofaTracerFilter implements WebFilter {

    @Value("${" + SofaTracerConfiguration.TRACER_APPNAME_KEY + "}")
    private String appName = StringUtils.EMPTY_STRING;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
                .transform(
                        new SofaTracerReactorTransformer<>(
                                () -> startSpan(exchange),
                                (springMvcSpan, throwable) -> finishSpan(exchange, springMvcSpan, throwable)
                        )
                );
    }

    private void startSpan(ServerWebExchange exchange) {
        SpringWebfluxTracer springWebfluxTracer = SpringWebfluxTracer
            .getSpringWebfluxTracerSingleton();
        SofaTracer tracer = springWebfluxTracer.getSofaTracer();

        SofaTraceableRequest request = new ServerWebExchangeSofaTraceableRequest(exchange);
        SofaTracerSpanContext spanContext = (SofaTracerSpanContext) tracer.extract(
            ExtendFormat.Builtin.B3_HTTP_HEADERS, new SpringWebfluxHeadersCarrier(new HashMap<>(
                request.getHeaders().toSingleValueMap())));

        /**
         * try to unpack span context from http request headers,
         *
         * if the span id is not zero, it means we are down stream of an existed span,
         * keep it and treat it as parent
         *
         * if the span id is 0, it means we create an new tracer span,
         * we should ignore it, to make {@link AbstractTracer#serverReceive(SofaTracerSpanContext)} work normally
         *
         * @see AbstractTracer#serverReceive(SofaTracerSpanContext)
         */
        if (spanContext == null || spanContext.getSpanId().equalsIgnoreCase("0")) {
            spanContext = null;
        }

        SofaTracerSpan springMvcSpan = springWebfluxTracer.serverReceive(spanContext);
        springMvcSpan.setOperationName(request.getUri().getPath());
        springMvcSpan.setTag(CommonSpanTags.LOCAL_APP, this.appName);
        springMvcSpan.setTag(CommonSpanTags.REMOTE_APP, request.getRemoteAddress());
        springMvcSpan.setTag(CommonSpanTags.REQUEST_URL, request.getUri().toString());
        springMvcSpan.setTag(CommonSpanTags.METHOD, request.getMethod());
        springMvcSpan.setTag(CommonSpanTags.REQ_SIZE, request.getHeaders().getContentLength());
    }

    private Void finishSpan(ServerWebExchange exchange, SofaTracerSpan springMvcSpan,
                            Throwable throwable) {
        SpringWebfluxTracer springMvcTracer = SpringWebfluxTracer.getSpringWebfluxTracerSingleton();
        SofaTraceableResponse response = new ServerWebExchangeSofaTraceableResponse(
            throwable != null ? new SofaStatusResponseDecorator(throwable, exchange.getResponse())
                : exchange.getResponse());
        springMvcSpan.setTag(CommonSpanTags.RESP_SIZE, response.getHeaders().getContentLength());
        springMvcTracer.serverSend(String.valueOf(response.getStatus()));

        return null;
    }

    static class SofaStatusResponseDecorator extends ServerHttpResponseDecorator {
        private final HttpStatus status;

        SofaStatusResponseDecorator(Throwable throwable, ServerHttpResponse delegate) {
            super(delegate);
            this.status = throwable instanceof ResponseStatusException ? ((ResponseStatusException) throwable)
                .getStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
        }

        @Override
        public HttpStatus getStatusCode() {
            return status;
        }
    }

    static class ServerWebExchangeSofaTraceableResponse implements SofaTraceableResponse {
        private final ServerHttpResponse responseDecorator;

        ServerWebExchangeSofaTraceableResponse(ServerHttpResponse responseDecorator) {
            this.responseDecorator = responseDecorator;
        }

        @Override
        public int getStatus() {
            return (this.responseDecorator.getStatusCode() != null ? this.responseDecorator
                .getStatusCode().value() : 200);
        }

        @Override
        public HttpHeaders getHeaders() {
            return this.responseDecorator.getHeaders();
        }
    }

    static class ServerWebExchangeSofaTraceableRequest implements SofaTraceableRequest {
        private final String      method;

        private final HttpHeaders headers;

        private final URI         uri;

        private final String      remoteAddress;

        ServerWebExchangeSofaTraceableRequest(ServerWebExchange exchange) {
            ServerHttpRequest request = exchange.getRequest();
            this.method = request.getMethodValue();
            this.headers = request.getHeaders();
            this.uri = request.getURI();
            this.remoteAddress = (request.getRemoteAddress() != null) ? request.getRemoteAddress()
                .getAddress().toString() : StringUtils.EMPTY_STRING;
        }

        @Override
        public String getMethod() {
            return method;
        }

        @Override
        public URI getUri() {
            return uri;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public String getRemoteAddress() {
            return remoteAddress;
        }
    }
}