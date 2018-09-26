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
package com.alipay.sofa.tracer.spring.zipkin;

import com.alipay.common.tracer.core.listener.SpanReportListener;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.sofa.tracer.spring.zipkin.adapter.ZipkinV2SpanAdapter;
import com.alipay.sofa.tracer.spring.zipkin.properties.PropertiesHolder;
import com.alipay.sofa.tracer.spring.zipkin.sender.ZipkinRestTemplateSender;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * zipkin report
 * @author guolei.sgl
 */
@Component
public class ZipkinSofaTracerSpanRemoteReporter implements SpanReportListener, Flushable, Closeable {
    private final ZipkinRestTemplateSender sender;

    private final AsyncReporter<Span>      delegate;

    private final ZipkinV2SpanAdapter      zipkinV2SpanAdapter;

    public ZipkinSofaTracerSpanRemoteReporter() {
        RestTemplate restTemplate = new RestTemplate();
        ZipkinSofaTracerRestTemplateCustomizer zipkinSofaTracerRestTemplateCustomizer = new ZipkinSofaTracerRestTemplateCustomizer();
        zipkinSofaTracerRestTemplateCustomizer.customize(restTemplate);
        this.zipkinV2SpanAdapter = new ZipkinV2SpanAdapter();
        this.sender = new ZipkinRestTemplateSender(restTemplate, PropertiesHolder.getBaseUrl());
        this.delegate = AsyncReporter.create(sender);
    }

    @Override
    public void onSpanReport(SofaTracerSpan span) {
        if (span == null) {
            return;
        }
        //convert
        Span zipkinSpan = zipkinV2SpanAdapter.convertToZipkinSpan(span);
        this.delegate.report(zipkinSpan);
    }

    @Override
    public void flush() throws IOException {
        this.delegate.flush();
    }

    @Override
    public void close() {
        this.delegate.close();
    }
}
