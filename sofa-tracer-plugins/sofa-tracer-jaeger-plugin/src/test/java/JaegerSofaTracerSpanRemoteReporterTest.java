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
import io.jaegertracing.internal.JaegerSpan;
import io.jaegertracing.internal.reporters.RemoteReporter;
import io.jaegertracing.spi.Sender;
import io.jaegertracing.thrift.internal.senders.UdpSender;
import org.apache.thrift.transport.TTransportException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class JaegerSofaTracerSpanRemoteReporterTest {
    private JaegerSpanAdapter jaegerSpanAdapter = new JaegerSpanAdapter();

    private final String      tracerType        = "SofaTracerSpanTest";

    private SofaTracer        sofaTracer;

    private SofaTracerSpan    sofaTracerSpan;

    private JaegerSpan        jaegerSpan;

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
    public void testSpanReport() throws TTransportException {

        Sender sender = new UdpSender("127.0.0.1", 6831, 0);
        RemoteReporter reporter = new RemoteReporter.Builder().withSender(sender).build();
        jaegerSpan = jaegerSpanAdapter.convertAndReport(sofaTracerSpan, reporter);

    }

    @Test
    public void testCommandQueueSetting() throws TTransportException {
        JaegerSofaTracerSpanRemoteReporter reporter = new JaegerSofaTracerSpanRemoteReporter(
            "127.0.0.1", 6831, 0);
        Assert.assertTrue(SofaTracerConfiguration.getIntegerDefaultIfNull(
            JaegerProperties.JAEGER_AGENT_FLUSH_INTERVAL_MS_KEY, 1000) == 200);
        Assert.assertTrue(SofaTracerConfiguration.getIntegerDefaultIfNull(
            JaegerProperties.JAEGER_AGENT_MAX_QUEUE_SIZE_KEY, 100) == 200);
        Assert.assertTrue(SofaTracerConfiguration.getIntegerDefaultIfNull(
            JaegerProperties.JAEGER_AGENT_CLOSE_ENQUEUE_TIMEOUT_MILLIS_KEY, 1000) == 2000);
    }

}
