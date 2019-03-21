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
package com.alipay.sofa.tracer.plugins.zipkin.reporters;

import com.alipay.common.tracer.core.listener.SpanReportListener;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.sofa.tracer.plugins.zipkin.adapter.ZipkinV2SpanAdapter;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/11 3:39 PM
 * @since:
 **/
public class Okhttp3SpanRemoteReporter implements SpanReportListener, Flushable, Closeable {

    private OkHttpSender              okHttpSender;
    private ZipkinV2SpanAdapter       zipkinV2SpanAdapter;
    private final OkHttpSender        sender;

    private final AsyncReporter<Span> delegate;

    public Okhttp3SpanRemoteReporter() {
        this.zipkinV2SpanAdapter = new ZipkinV2SpanAdapter();
        this.sender = OkHttpSender.create("htp://localhost:9411/api/v1/spans");
        this.delegate = AsyncReporter.create(sender);
    }

    @Override
    public void onSpanReport(SofaTracerSpan span) {
        //convert
        Span zipkinSpan = zipkinV2SpanAdapter.convertToZipkinSpan(span);
        this.delegate.report(zipkinSpan);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }
}
