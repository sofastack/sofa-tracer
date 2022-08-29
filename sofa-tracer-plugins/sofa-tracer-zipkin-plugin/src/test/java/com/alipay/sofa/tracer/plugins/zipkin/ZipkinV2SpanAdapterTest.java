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

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.sofa.tracer.plugins.zipkin.adapter.ZipkinV2SpanAdapter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import zipkin2.Span;

import java.util.HashMap;
import java.util.Map;

/**
 * ZipkinV2SpanAdapterTest
 *
 * @author: guolei.sgl
 * @since: v2.3.0
 **/
public class ZipkinV2SpanAdapterTest {

    private ZipkinV2SpanAdapter zipkinV2SpanAdapter = new ZipkinV2SpanAdapter();

    private final String        tracerType          = "SofaTracerSpanTest";

    private SofaTracer          sofaTracer;

    private SofaTracerSpan      sofaTracerSpan;

    @Before
    public void init() throws InterruptedException {
        sofaTracer = new SofaTracer.Builder(tracerType).withTag("tracer", "SofaTraceZipkinTest")
            .build();
        sofaTracerSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("SofaTracerSpanTest").start();
        sofaTracerSpan.setTag("tagsStrkey", "tagsStrVal");
        sofaTracerSpan.setTag("tagsBooleankey", true);
        sofaTracerSpan.setTag("tagsBooleankey", 2018);
        sofaTracerSpan.setBaggageItem("baggageKey", "baggageVal");
        sofaTracerSpan.setTag(CommonSpanTags.LOCAL_APP, "SofaTracerSpanTest");
        Map<String, String> logMap = new HashMap<String, String>();
        logMap.put("logKey", "logVal");
        LogData logData = new LogData(System.currentTimeMillis(), logMap);
        sofaTracerSpan.log(logData);
        // mock process
        Thread.sleep(30);
        sofaTracerSpan.setEndTime(System.currentTimeMillis());
    }

    @Test
    public void testConvertToZipkinSpan() {
        Span span = zipkinV2SpanAdapter.convertToZipkinSpan(sofaTracerSpan);
        Assert.assertTrue(span != null);
        Assert.assertTrue(span.name().equalsIgnoreCase(sofaTracerSpan.getOperationName()));
        Assert.assertTrue(span.tags().size() == 6);
        // zipkin's tracerId will be high position 0
        Assert.assertTrue(span.traceId().contains(
            sofaTracerSpan.getSofaTracerSpanContext().getTraceId()));
        Assert
            .assertTrue(span.localEndpoint().serviceName().equalsIgnoreCase("SofaTracerSpanTest"));
    }
}
