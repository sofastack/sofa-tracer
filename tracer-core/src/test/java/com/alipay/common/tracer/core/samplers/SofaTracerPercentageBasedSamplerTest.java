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
package com.alipay.common.tracer.core.samplers;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.reporter.digest.DiskReporterImpl;
import com.alipay.common.tracer.core.reporter.facade.Reporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracertest.encoder.ClientSpanEncoder;
import com.alipay.common.tracer.core.tracertest.encoder.ServerSpanEncoder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @description: [test unit for SofaTracerPercentageBasedSampler]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/7/25
 */
public class SofaTracerPercentageBasedSamplerTest {

    private final String             tracerType    = "SofaTracerSpanTest";
    private final String             clientLogType = "client-log-test.log";
    private final String             serverLogType = "server-log-test.log";

    SamplerProperties                samplerProperties;
    SofaTracerPercentageBasedSampler sofaTracerPercentageBasedSampler;

    private SofaTracer               sofaTracer;
    private SofaTracerSpan           sofaTracerSpan;

    @Before
    public void setUp() {
        samplerProperties = new SamplerProperties();
        sofaTracerPercentageBasedSampler = new SofaTracerPercentageBasedSampler(samplerProperties);
        Reporter clientReporter = new DiskReporterImpl(clientLogType, new ClientSpanEncoder());
        Reporter serverReporter = new DiskReporterImpl(serverLogType, new ServerSpanEncoder());
        sofaTracer = new SofaTracer.Builder(tracerType)
            .withTag("tracer", "SofaTraceContextHolderTest").withClientReporter(clientReporter)
            .withServerReporter(serverReporter).build();
        sofaTracerSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("").start();
        sofaTracerSpan.getSofaTracerSpanContext().setTraceId("");
    }

    @After
    public void close() {
        sofaTracerPercentageBasedSampler.close();
    }

    @Test
    public void sample() {
        samplerProperties.setPercentage(0);
        SamplingStatus sampleStatusFalse = sofaTracerPercentageBasedSampler.sample(sofaTracerSpan);
        Assert.assertTrue(!sampleStatusFalse.isSampled());

        samplerProperties.setPercentage(100);
        SamplingStatus sampleStatusTrue = sofaTracerPercentageBasedSampler.sample(sofaTracerSpan);
        Assert.assertTrue(sampleStatusTrue.isSampled());
    }

    @Test
    public void getType() {
        Assert.assertTrue(sofaTracerPercentageBasedSampler.getType().equals(
            "PercentageBasedSampler"));
    }
}