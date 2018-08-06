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
package com.alipay.common.tracer.core.tracer;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @description: [test AbstractClientTracer]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/8/1
 */
public class AbstractClientTracerTest {

    AbstractClientTracer clientTracer;

    @Before
    public void init() {
        clientTracer = new TestClientTracer("springmvc");
    }

    @Test
    public void getServerDigestReporterLogName() {
        String serverDigestReporterLogName = clientTracer.getServerDigestReporterLogName();
        Assert.assertTrue(serverDigestReporterLogName == null);
    }

    @Test
    public void getServerDigestReporterRollingKey() {
        String serverDigestReporterRollingKey = clientTracer.getServerDigestReporterRollingKey();
        Assert.assertTrue(serverDigestReporterRollingKey == null);
    }

    @Test
    public void getServerDigestReporterLogNameKey() {
        String serverDigestReporterLogNameKey = clientTracer.getServerDigestReporterLogNameKey();
        Assert.assertTrue(serverDigestReporterLogNameKey == null);
    }

    @Test
    public void getServerDigestEncoder() {
        SpanEncoder<SofaTracerSpan> serverDigestEncoder = clientTracer.getServerDigestEncoder();
        Assert.assertTrue(serverDigestEncoder == null);
    }

    @Test
    public void generateServerStatReporter() {
        AbstractSofaTracerStatisticReporter abstractSofaTracerStatisticReporter = clientTracer
            .generateServerStatReporter();
        Assert.assertTrue(abstractSofaTracerStatisticReporter == null);
    }

    class TestClientTracer extends AbstractClientTracer {

        public TestClientTracer(String tracerType) {
            super(tracerType);
        }

        @Override
        protected String getClientDigestReporterLogName() {
            return "client-digest.log";
        }

        @Override
        protected String getClientDigestReporterRollingKey() {
            return "client-digest-rolling.log";
        }

        @Override
        protected String getClientDigestReporterLogNameKey() {
            return "client-digest";
        }

        @Override
        protected SpanEncoder<SofaTracerSpan> getClientDigestEncoder() {
            return Mockito.mock(SpanEncoder.class);
        }

        @Override
        protected AbstractSofaTracerStatisticReporter generateClientStatReporter() {
            return Mockito.mock(AbstractSofaTracerStatisticReporter.class);
        }

    }

}