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
 * @description: [test AbstractServerTracer]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/8/1
 */
public class AbstractServerTracerTest {

    private AbstractServerTracer serverTracer;

    @Before
    public void init() {
        serverTracer = new TestServerTracer("TracerTestService");
    }

    @Test
    public void getClientDigestReporterLogName() {
        //user super method
        String clientDigestReporterLogName = serverTracer.getClientDigestReporterLogName();
        Assert.assertTrue(clientDigestReporterLogName == null);
    }

    @Test
    public void getClientDigestReporterRollingKey() {
        //user super method
        String clientDigestReporterRollingKey = serverTracer.getClientDigestReporterRollingKey();
        Assert.assertTrue(clientDigestReporterRollingKey == null);
    }

    @Test
    public void getClientDigestReporterLogNameKey() {
        //user super method
        String clientDigestReporterLogNameKey = serverTracer.getClientDigestReporterLogNameKey();
        Assert.assertTrue(clientDigestReporterLogNameKey == null);
    }

    @Test
    public void getClientDigestEncoder() {
        //user super method
        SpanEncoder<SofaTracerSpan> clientDigestEncoder = serverTracer.getClientDigestEncoder();
        Assert.assertTrue(clientDigestEncoder == null);
    }

    @Test
    public void generateClientStatReporter() {
        //user super method
        AbstractSofaTracerStatisticReporter abstractSofaTracerStatisticReporter = serverTracer
            .generateClientStatReporter();
        Assert.assertTrue(abstractSofaTracerStatisticReporter == null);
    }

    class TestServerTracer extends AbstractServerTracer {

        public TestServerTracer(String tracerType) {
            super(tracerType);
        }

        @Override
        protected String getServerDigestReporterLogName() {
            return "server-digest.log";
        }

        @Override
        protected String getServerDigestReporterRollingKey() {
            return "server-digest_rolling";
        }

        @Override
        protected String getServerDigestReporterLogNameKey() {
            return "server-digest_log_name";
        }

        @Override
        protected SpanEncoder<SofaTracerSpan> getServerDigestEncoder() {
            return Mockito.mock(SpanEncoder.class);
        }

        @Override
        protected AbstractSofaTracerStatisticReporter generateServerStatReporter() {
            return Mockito.mock(AbstractSofaTracerStatisticReporter.class);
        }
    }
}