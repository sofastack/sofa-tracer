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
import com.alibaba.fastjson.JSON;
import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.sofa.tracer.plugins.skywalking.adapter.SkywalkingSegmentAdapter;
import com.alipay.sofa.tracer.plugins.skywalking.model.Segment;

import com.alipay.sofa.tracer.plugins.skywalking.model.Span;
import com.alipay.sofa.tracer.plugins.skywalking.model.SpanLayer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * SkywalkingSegmentAdapterTest
 * @author zhaochen
 */
public class SkywalkingSegmentAdapterTest {
    private SkywalkingSegmentAdapter adapter    = new SkywalkingSegmentAdapter();

    private final String             tracerType = ComponentNameConstants.DATA_SOURCE;

    private SofaTracer               sofaTracer;

    private SofaTracerSpan           sofaTracerSpan;

    @Before
    public void init() throws InterruptedException {
        sofaTracer = new SofaTracer.Builder(tracerType).withTag("tracer", "SofaTraceZipkinTest")
            .build();
        sofaTracerSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("SofaTracerSpanTest").start();
        sofaTracerSpan.setTag("tagsStrkey", "tagsStrVal");
        sofaTracerSpan.setTag("tagsBooleankey", true);
        sofaTracerSpan.setTag("tagsNumkey", 2018);
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
    public void testConvertToSegment() {
        Segment segment = adapter.convertToSkywalkingSegment(sofaTracerSpan);
        Assert.assertTrue(segment != null);
        Span span = segment.getSpans().get(0);
        Assert.assertTrue(span.getOperationName().equalsIgnoreCase(
            sofaTracerSpan.getOperationName()));
        Assert.assertTrue(span.getTags().size() == 4);
        Assert.assertTrue(segment.getTraceId().contains(
            sofaTracerSpan.getSofaTracerSpanContext().getTraceId()));
        Assert.assertTrue(span.getLogs().size() == 1);
        Assert.assertTrue(span.getSpanLayer().equals(SpanLayer.Database));
        Assert.assertTrue(span.getComponentId() == 0);
    }
}
