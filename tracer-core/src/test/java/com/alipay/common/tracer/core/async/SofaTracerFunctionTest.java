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
package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.reporter.digest.DiskReporterImpl;
import com.alipay.common.tracer.core.reporter.facade.Reporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracertest.encoder.ClientSpanEncoder;
import com.alipay.common.tracer.core.tracertest.encoder.ServerSpanEncoder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author khotyn
 * @version SofaTracerFunctionTest.java, v 0.1 2021年02月07日 10:01 下午 khotyn
 */
public class SofaTracerFunctionTest {
    private SofaTracerSpan sofaTracerSpan;

    @Before
    public void setup() {
        String clientLogType = "client-log-test.log";
        Reporter clientReporter = new DiskReporterImpl(clientLogType, new ClientSpanEncoder());
        String serverLogType = "server-log-test.log";
        Reporter serverReporter = new DiskReporterImpl(serverLogType, new ServerSpanEncoder());
        String tracerType = "SofaTracerSpanTest";
        SofaTracer sofaTracer = new SofaTracer.Builder(tracerType)
            .withTag("tracer", "SofaTraceContextHolderTest").withClientReporter(clientReporter)
            .withServerReporter(serverReporter).build();
        sofaTracerSpan = (SofaTracerSpan) sofaTracer.buildSpan("SofaTracerSpanTest").start();
    }

    @Test
    public void testFunction() {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        sofaTraceContext.push(sofaTracerSpan);
        List<String> list = Arrays.asList("1", "2", "2", "2", "2", "2");
        List<SofaTracerSpan> sofaTracerSpans = list.parallelStream().map(new SofaTracerFunction<>(s -> SofaTraceContextHolder.getSofaTraceContext().getCurrentSpan())).collect(Collectors.toList());
        Assert.assertTrue(sofaTracerSpans.stream().allMatch(s -> s.equals(sofaTracerSpan)));
    }

    @Test
    public void testRawFunction() {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        sofaTraceContext.push(sofaTracerSpan);
        List<String> list = Arrays.asList("1", "2", "2", "2", "2", "2");
        List<SofaTracerSpan> sofaTracerSpans = list.parallelStream().map(s -> SofaTraceContextHolder.getSofaTraceContext().getCurrentSpan()).collect(Collectors.toList());
        Assert.assertTrue(sofaTracerSpans.stream().anyMatch(Objects::isNull));
    }
}