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
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.registry.AbstractTextB3Formatter;
import com.alipay.common.tracer.core.registry.ExtendFormat;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import io.opentracing.tag.Tags;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * SpringMvcSofaTracerFilter
 *
 * @author yangguanchao
 * @since 2018/04/30
 */
public class SpringMvcSofaTracerFilter implements Filter {

    private String          appName = StringUtils.EMPTY_STRING;

    private SpringMvcTracer springMvcTracer;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no operation and lazy init
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) {

        if (this.springMvcTracer == null) {
            this.springMvcTracer = SpringMvcTracer.getSpringMvcTracerSingleton();
        }
        SofaTracerSpan springMvcSpan = null;
        long responseSize = -1;
        int httpStatus = -1;
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            SofaTracerSpanContext spanContext = getSpanContextFromRequest(request);
            // sr
            springMvcSpan = springMvcTracer.serverReceive(spanContext);

            if (StringUtils.isBlank(this.appName)) {
                this.appName = SofaTracerConfiguration
                    .getProperty(SofaTracerConfiguration.TRACER_APPNAME_KEY);
            }
            //set service name
            springMvcSpan.setOperationName(request.getRequestURL().toString());
            //app name
            springMvcSpan.setTag(CommonSpanTags.LOCAL_APP, this.appName);
            springMvcSpan.setTag(CommonSpanTags.REQUEST_URL, request.getRequestURL().toString());
            springMvcSpan.setTag(CommonSpanTags.METHOD, request.getMethod());
            springMvcSpan.setTag(CommonSpanTags.REQ_SIZE, request.getContentLength());
            //wrapper
            ResponseWrapper responseWrapper = new ResponseWrapper(response);

            //filter begin
            filterChain.doFilter(servletRequest, responseWrapper);
            //filter end
            httpStatus = responseWrapper.getStatus();
            responseSize = responseWrapper.getContentLength();
        } catch (Throwable t) {
            httpStatus = 500;
            springMvcSpan.setTag(Tags.ERROR.getKey(), t.getMessage());
            // 异常抛出
            throw new RuntimeException(t);
        } finally {
            if (springMvcSpan != null) {
                springMvcSpan.setTag(CommonSpanTags.RESP_SIZE, responseSize);
                //ss
                springMvcTracer.serverSend(String.valueOf(httpStatus));
            }
        }
    }

    @Override
    public void destroy() {
        // no operation
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getFilterName() {
        return "SpringMvcSofaTracerFilter";
    }

    /***
     * Extract tracing context from request received from previous node
     * @param request Servlet http request object
     * @return SofaTracerSpanContext Tracing context extract from request
     */
    public SofaTracerSpanContext getSpanContextFromRequest(HttpServletRequest request) {
        HashMap<String, String> headers = new HashMap<String, String>();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            headers.put(key, value);
        }
        // Delay the initialization of the SofaTracerSpanContext to execute the serverReceive method
        if (headers.isEmpty() || !isContainSofaTracerMark(headers)) {
            return null;
        }

        SofaTracer tracer = springMvcTracer.getSofaTracer();
        SofaTracerSpanContext spanContext = (SofaTracerSpanContext) tracer.extract(
            ExtendFormat.Builtin.B3_HTTP_HEADERS, new SpringMvcHeadersCarrier(headers));
        return spanContext;
    }

    /**
     * To check is contain sofaTracer mark
     * @param headers
     * @return
     */
    private boolean isContainSofaTracerMark(HashMap<String, String> headers) {
        return (headers.containsKey(AbstractTextB3Formatter.TRACE_ID_KEY_HEAD.toLowerCase()) || headers
            .containsKey(AbstractTextB3Formatter.TRACE_ID_KEY_HEAD))
               && (headers.containsKey(AbstractTextB3Formatter.SPAN_ID_KEY_HEAD.toLowerCase()) || headers
                   .containsKey(AbstractTextB3Formatter.SPAN_ID_KEY_HEAD));
    }

    class ResponseWrapper extends HttpServletResponseWrapper {

        int contentLength = 0;

        /**
         * @param httpServletResponse httpServletResponse
         */
        public ResponseWrapper(HttpServletResponse httpServletResponse) throws IOException {
            super(httpServletResponse);
        }

        /**
         * @see javax.servlet.ServletResponseWrapper#setContentLength(int)
         */
        @Override
        public void setContentLength(int len) {
            contentLength = len;
            super.setContentLength(len);
        }

        public int getContentLength() {
            return contentLength;
        }
    }

}