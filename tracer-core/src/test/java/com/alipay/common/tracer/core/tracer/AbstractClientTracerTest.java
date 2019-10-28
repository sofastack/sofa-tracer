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
        String serverDigestReporterLogName = clientTracer.getDigestReporterLogName();
        Assert.assertTrue(serverDigestReporterLogName.equalsIgnoreCase("client-digest.log"));
    }

    @Test
    public void getServerDigestReporterRollingKey() {
        String serverDigestReporterRollingKey = clientTracer.getDigestReporterRollingKey();
        Assert.assertTrue(serverDigestReporterRollingKey
            .equalsIgnoreCase("client-digest-rolling.log"));
    }

    @Test
    public void getServerDigestReporterLogNameKey() {
        String serverDigestReporterLogNameKey = clientTracer.getDigestReporterLogNameKey();
        Assert.assertTrue(serverDigestReporterLogNameKey.equalsIgnoreCase("client-digest"));
    }

    @Test
    public void getServerDigestEncoder() {
        SpanEncoder<SofaTracerSpan> serverDigestEncoder = clientTracer.getDigestEncoder();
        Assert.assertTrue(serverDigestEncoder instanceof SpanEncoder);
    }

    @Test
    public void generateServerStatReporter() {
        AbstractSofaTracerStatisticReporter abstractSofaTracerStatisticReporter = clientTracer
            .generateStatReporter();
        Assert
            .assertTrue(abstractSofaTracerStatisticReporter instanceof AbstractSofaTracerStatisticReporter);
    }

    class TestClientTracer extends AbstractClientTracer {

        public TestClientTracer(String tracerType) {
            super(tracerType);
        }

        @Override
        protected String getDigestReporterLogName() {
            return "client-digest.log";
        }

        @Override
        protected String getDigestReporterRollingKey() {
            return "client-digest-rolling.log";
        }

        @Override
        protected String getDigestReporterLogNameKey() {
            return "client-digest";
        }

        @Override
        protected SpanEncoder<SofaTracerSpan> getDigestEncoder() {
            return Mockito.mock(SpanEncoder.class);
        }

        @Override
        protected AbstractSofaTracerStatisticReporter generateStatReporter() {
            return Mockito.mock(AbstractSofaTracerStatisticReporter.class);
        }

    }

}