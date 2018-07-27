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
package com.alipay.common.tracer.core.extensions;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.base.AbstractTestBase;
import com.alipay.common.tracer.core.reporter.digest.DiskReporterImpl;
import com.alipay.common.tracer.core.reporter.facade.Reporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracertest.encoder.ClientSpanEncoder;
import com.alipay.common.tracer.core.tracertest.encoder.ServerSpanEncoder;
import io.opentracing.Span;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * @description: [描述文本]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/7/25
 */
public class SpanExtensionFactoryTest extends AbstractTestBase {

    private final String   tracerType    = "SofaTracerSpanTest";

    private final String   clientLogType = "client-log-test.log";

    private final String   serverLogType = "server-log-test.log";

    private SofaTracer     sofaTracer;

    private SofaTracerSpan sofaTracerSpan;

    @Before
    public void before() throws Exception {
        SpanExtensionFactory spanExtensionFactory = new SpanExtensionFactory();
        Field spanExtensions = spanExtensionFactory.getClass().getDeclaredField("spanExtensions");
        spanExtensions.setAccessible(true);
        SpanExtension spanExtension = new SpanExtension() {
            @Override
            public void logStartedSpan(Span currentSpan) {

            }

            @Override
            public void logStoppedSpan(Span currentSpan) {

            }

            @Override
            public void logStoppedSpanInRunnable(Span currentSpan) {

            }

            @Override
            public String supportName() {
                return null;
            }
        };
        //init spanExtensions filed
        Set<SpanExtension> set = new HashSet<SpanExtension>();
        set.add(spanExtension);
        spanExtensions.set(spanExtensionFactory, set);

        //init resources
        Reporter clientReporter = new DiskReporterImpl(clientLogType, new ClientSpanEncoder());
        Reporter serverReporter = new DiskReporterImpl(serverLogType, new ServerSpanEncoder());
        sofaTracer = new SofaTracer.Builder(tracerType)
            .withTag("tracer", "SofaTraceContextHolderTest").withClientReporter(clientReporter)
            .withServerReporter(serverReporter).build();
        sofaTracerSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("SofaTracerSpanTest").start();

    }

    @Test
    public void logStartedSpan() {
        SpanExtensionFactory.logStartedSpan(sofaTracerSpan);
    }

    @Test
    public void logStoppedSpan() {
        SpanExtensionFactory.logStoppedSpan(sofaTracerSpan);
    }

    @Test
    public void logStoppedSpanInRunnable() {
        SpanExtensionFactory.logStoppedSpanInRunnable(sofaTracerSpan);
    }

}