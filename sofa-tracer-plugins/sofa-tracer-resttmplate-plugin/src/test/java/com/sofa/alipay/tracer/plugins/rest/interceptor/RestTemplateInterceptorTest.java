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

import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractTracer;
import org.junit.After;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.net.URI;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RestTemplateInterceptorTest {

    private final SofaTraceContext traceContext = SofaTraceContextHolder.getSofaTraceContext();

    @After
    public void tearDown() {
        traceContext.clear();
    }

    @Test
    public void shouldFinishClientSpanWhenResponseHeadersCannotBeRead() throws Exception {
        AbstractTracer tracer = mock(AbstractTracer.class);
        SofaTracerSpan span = mock(SofaTracerSpan.class);
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        byte[] body = new byte[0];

        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("http://localhost/test"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(tracer.clientSend(HttpMethod.GET.name())).thenReturn(span);
        when(execution.execute(request, body)).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.getHeaders()).thenThrow(new IllegalStateException("Read timed out"));
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                traceContext.pop();
                return null;
            }
        }).when(tracer).clientReceive(anyString());

        traceContext.push(span);
        RestTemplateInterceptor interceptor = new RestTemplateInterceptor(tracer) {
            @Override
            public void injectCarrier(HttpRequest request, SofaTracerSpan currentSpan) {
            }
        };

        ClientHttpResponse result = interceptor.intercept(request, body, execution);

        assertSame(response, result);
        verify(tracer).clientReceive(String.valueOf(HttpStatus.OK.value()));
        assertTrue(traceContext.isEmpty());
    }
}
