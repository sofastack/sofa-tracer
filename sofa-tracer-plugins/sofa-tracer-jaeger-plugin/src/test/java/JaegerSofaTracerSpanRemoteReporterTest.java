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
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.sofa.tracer.plugins.jaeger.JaegerSofaTracerSpanRemoteReporter;
import com.alipay.sofa.tracer.plugins.jaeger.adapter.JaegerSpanAdapter;
import com.alipay.sofa.tracer.plugins.jaeger.properties.JaegerProperties;
import io.jaegertracing.internal.JaegerTracer;
import org.apache.thrift.transport.TTransportException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * JaegerSofaTracerSpanRemoteReporterTest
 * @author: zhaochen
 */
public class JaegerSofaTracerSpanRemoteReporterTest {
    private JaegerSpanAdapter jaegerSpanAdapter = new JaegerSpanAdapter();

    private final String      tracerType        = "SofaTracerSpanTest";

    private SofaTracer        sofaTracer;

    private SofaTracerSpan    sofaTracerSpan;

    @Before
    public void init() throws InterruptedException {
        sofaTracer = new SofaTracer.Builder(tracerType).withTag("tracer", "SofaTraceZipkinTest")
            .build();
        sofaTracerSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("SofaTracerSpanTest").start();
        sofaTracerSpan.setTag("tagsStrkey", "tagsStrVal");
        sofaTracerSpan.setTag("tagsBooleankey", true);
        sofaTracerSpan.setTag("tagsNumberkey", 2018);
        sofaTracerSpan.setTag("span.kind", "client");
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
    public void testAgentSpanReport() throws TTransportException {

        JaegerSofaTracerSpanRemoteReporter remoteReporter = new JaegerSofaTracerSpanRemoteReporter(
            "127.0.0.1", 6831, 65000, "testService", 1000, 10000, 1000);
        JaegerTracer jaegerTracer = remoteReporter.getJaegerTracer();
        jaegerSpanAdapter.convertAndReport(sofaTracerSpan, jaegerTracer);

    }

    @Test
    public void testCollectorSpanReport() throws TTransportException {

        JaegerSofaTracerSpanRemoteReporter remoteReporter = new JaegerSofaTracerSpanRemoteReporter(
            "http://localhost:14268/", 2 * 1024 * 1024, "testService", 1000, 10000, 1000);
        JaegerTracer jaegerTracer = remoteReporter.getJaegerTracer();
        jaegerSpanAdapter.convertAndReport(sofaTracerSpan, jaegerTracer);
    }

    @Test
    public void testCommandQueueSetting() throws TTransportException {
        JaegerSofaTracerSpanRemoteReporter reporter = new JaegerSofaTracerSpanRemoteReporter(
            "127.0.0.1", 6831, 0, "testService", 1000, 10000, 1000);
        Assert.assertTrue(SofaTracerConfiguration.getIntegerDefaultIfNull(
            JaegerProperties.JAEGER_FLUSH_INTERVAL_MS_KEY, 1000) == 200);
        Assert.assertTrue(SofaTracerConfiguration.getIntegerDefaultIfNull(
            JaegerProperties.JAEGER_MAX_QUEUE_SIZE_KEY, 10000) == 200);
        Assert.assertTrue(SofaTracerConfiguration.getIntegerDefaultIfNull(
            JaegerProperties.JAEGER_CLOSE_ENQUEUE_TIMEOUT_MILLIS_KEY, 1000) == 2000);
    }

}
