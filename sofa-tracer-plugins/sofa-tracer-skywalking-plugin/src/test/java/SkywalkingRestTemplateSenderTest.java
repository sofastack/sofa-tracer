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
import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.sofa.tracer.plugins.skywalking.SkywalkingSpanRemoteReporter;
import com.alipay.sofa.tracer.plugins.skywalking.adapter.SkywalkingSegmentAdapter;
import com.alipay.sofa.tracer.plugins.skywalking.model.Segment;
import com.alipay.sofa.tracer.plugins.skywalking.sender.SkywalkingRestTemplateSender;
import io.opentracing.tag.Tags;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SkywalkingRestTemplateSenderTest
 * @author zhaochen
 */
public class SkywalkingRestTemplateSenderTest {
    private final String                 tracerType = ComponentNameConstants.REDIS;
    private SkywalkingSegmentAdapter     adapter    = new SkywalkingSegmentAdapter();
    private SofaTracer                   sofaTracer;
    private SkywalkingRestTemplateSender sender;
    SofaTracerSpan                       sofaTracerSpan;

    @Before
    public void init() throws InterruptedException {
        sender = new SkywalkingRestTemplateSender(new RestTemplate(), "http://127.0.0.1:12800");
        sofaTracer = new SofaTracer.Builder(tracerType).withTag("tracer", "SofaTraceZipkinTest")
            .build();
        sofaTracerSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("http//asynictest.com").start();
        sofaTracerSpan.setTag("tagsStrkey", "tagsStrVal");
        // mock process
        Thread.sleep(30);
        sofaTracerSpan.setEndTime(System.currentTimeMillis());
    }

    @Test
    public void testPost() {
        ArrayList<Segment> segments = new ArrayList<>();
        Segment segment = adapter.convertToSkywalkingSegment(sofaTracerSpan);
        segments.add(segment);
        Assert.assertTrue(sender.post(segments));
    }
}
