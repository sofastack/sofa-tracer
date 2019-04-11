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
package com.alipay.sofa.tracer.plugins.zipkin;

import com.alipay.common.tracer.core.listener.SpanReportListener;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.TracerUtils;
import com.alipay.sofa.tracer.plugins.zipkin.adapter.ZipkinV2SpanAdapter;
import com.alipay.sofa.tracer.plugins.zipkin.sender.ZipkinRestTemplateSender;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import java.io.Closeable;
import java.io.Flushable;

/**
 * zipkin report
 * @author guolei.sgl
 * @since v2.3.0
 */
public class ZipkinSofaTracerSpanRemoteReporter implements SpanReportListener, Flushable, Closeable {

    private static String                  processId = TracerUtils.getPID();

    private final ZipkinRestTemplateSender sender;

    private final AsyncReporter<Span>      delegate;

    private final ZipkinV2SpanAdapter      zipkinV2SpanAdapter;

    public ZipkinSofaTracerSpanRemoteReporter(RestTemplate restTemplate, String baseUrl) {

        this.zipkinV2SpanAdapter = new ZipkinV2SpanAdapter();
        this.sender = new ZipkinRestTemplateSender(restTemplate, baseUrl);
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
    public void flush() {
        this.delegate.flush();
    }

    @Override
    public void close() {
        this.delegate.close();
    }

    /**
     * To convert hexadecimal string to decimal integer
     * @param hexString hexadecimal
     * @return decimal
     */
    public static long traceIdToId(String hexString) {
        Assert.hasText(hexString, "Can't convert empty hex string to long");
        int length = hexString.length();
        if (length < 1) {
            throw new IllegalArgumentException("Malformed id(length must be more than zero): "
                                               + hexString);
        }
        if (length <= 8) {
            //hex
            return Long.parseLong(hexString, 16);
        } else if (hexString.endsWith(processId)) {
            //time
            return Long.parseLong(hexString.substring(8, hexString.lastIndexOf(processId)), 10);
        } else {
            //delete ip and processor id
            return Long.parseLong(hexString.substring(8), 10);
        }
    }
}
