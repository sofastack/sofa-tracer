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

import io.opentracing.tag.Tags;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * SkywalkingSpanRemoteReporterTest
 * @author zhaochen
 */
public class SkywalkingSpanRemoteReporterTest {
    private SkywalkingSpanRemoteReporter reporter;

    private final String                 tracerType = ComponentNameConstants.REDIS;

    private SofaTracer                   sofaTracer;

    private SofaTracerSpan               sofaTracerSpan;

    @Before
    public void init() throws InterruptedException {
        reporter = new SkywalkingSpanRemoteReporter("http://127.0.0.1:12800", 10000);

        sofaTracer = new SofaTracer.Builder(tracerType).withTag("tracer", "SofaTraceZipkinTest")
            .build();
        sofaTracerSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("http//asynictest.com").start();
        sofaTracerSpan.setTag("tagsStrkey", "tagsStrVal");
        sofaTracerSpan.setTag("tagsBooleankey", true);
        sofaTracerSpan.setTag("tagsBooleankey", 2018);
        sofaTracerSpan.setTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
        sofaTracerSpan.setBaggageItem("baggageKey", "baggageVal");
        sofaTracerSpan.setTag(CommonSpanTags.LOCAL_APP, "TESTABC");
        Map<String, String> logMap = new HashMap<String, String>();
        logMap.put("logKey", "logVal");
        LogData logData = new LogData(System.currentTimeMillis(), logMap);
        sofaTracerSpan.log(logData);
        // mock process
        Thread.sleep(30);
        sofaTracerSpan.setEndTime(System.currentTimeMillis());
    }

    @Test
    public void testOnSpanReport() {
        reporter.onSpanReport(sofaTracerSpan);
    }

}
