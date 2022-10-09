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
package com.alipay.sofa.tracer.plugins.httpclient;

import com.alipay.common.tracer.core.tracer.AbstractTracer;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.execchain.ClientExecChain;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SofaTracerHttpClientBuilder
 *
 * @author yangguanchao
 * @since 2018/08/07
 */
public class SofaTracerHttpClientBuilder extends HttpClientBuilder {
    private static AbstractTracer httpClientTracer;
    private List<SofaTracerClientSpanDecorator> spanDecorators;

    public SofaTracerHttpClientBuilder(RedirectStrategy redirectStrategy,
                                       boolean redirectHandlingDisabled,
                                       //  AbstractTracer tracer,
                                       List<SofaTracerClientSpanDecorator> spanDecorators) {
        this.httpClientTracer = getHttpClientTracer();
        this.spanDecorators = new ArrayList<>(spanDecorators);
    }

    public SofaTracerHttpClientBuilder() {
        this.httpClientTracer = getHttpClientTracer();
        this.spanDecorators = Collections.<SofaTracerClientSpanDecorator>singletonList(new SofaTracerClientSpanDecorator.StandardTags());
    }


    public static AbstractTracer getHttpClientTracer() {
        if (httpClientTracer == null) {
            synchronized (SofaTracerHttpClientBuilder.class) {
                if (httpClientTracer == null) {
                    //default json format
                    httpClientTracer = HttpClientTracer.getHttpClientTracerSingleton();
                }
            }
        }
        return httpClientTracer;
    }

    public static void clientBuilder(HttpClientBuilder httpClientBuilder) {
    }


    @Override
    protected ClientExecChain decorateProtocolExec(final ClientExecChain requestExecutor) {
        return new SofaTracerClientExec(requestExecutor, httpClientTracer, spanDecorators);
    }


}
