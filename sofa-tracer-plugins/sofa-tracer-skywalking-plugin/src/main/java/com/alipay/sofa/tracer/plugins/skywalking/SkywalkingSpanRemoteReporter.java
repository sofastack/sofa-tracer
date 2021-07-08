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
package com.alipay.sofa.tracer.plugins.skywalking;

import com.alibaba.fastjson.JSON;
import com.alipay.common.tracer.core.listener.SpanReportListener;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.sofa.tracer.plugins.skywalking.adapter.SkywalkingSegmentAdapter;
import com.alipay.sofa.tracer.plugins.skywalking.reporter.AsyncReporter;
import com.alipay.sofa.tracer.plugins.skywalking.sender.SkywalkingRestTemplateSender;
import com.alipay.sofa.tracer.plugins.skywalking.model.Segment;

import org.springframework.web.client.RestTemplate;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * SkywalkingSpanRemoteReporter
 * @author zhaochen
 */
public class SkywalkingSpanRemoteReporter implements SpanReportListener, Closeable, Flushable {
    private AsyncReporter                reporter;
    private SkywalkingRestTemplateSender sender;
    private SkywalkingSegmentAdapter     adapter;

    public SkywalkingSpanRemoteReporter(String baseUrl, int maxBufferSize) {
        adapter = new SkywalkingSegmentAdapter();
        sender = new SkywalkingRestTemplateSender(new RestTemplate(), baseUrl);
        reporter = new AsyncReporter(maxBufferSize, sender);
    }

    @Override
    public void onSpanReport(SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null || !sofaTracerSpan.getSofaTracerSpanContext().isSampled()) {
            return;
        }
        Segment segment = adapter.convertToSkywalkingSegment(sofaTracerSpan);
        int segmentSize = JSON.toJSONString(segment).length();
        reporter.report(segment, segmentSize);
    }

    @Override
    public void close() throws IOException {
        reporter.close();
    }

    @Override
    public void flush() throws IOException {
        reporter.flush();
    }
}
