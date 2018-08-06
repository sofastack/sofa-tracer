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
package com.alipay.common.tracer.core.exception;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.reporter.digest.DiskReporterImpl;
import com.alipay.common.tracer.core.tracertest.encoder.ClientSpanEncoder;
import com.alipay.common.tracer.core.tracertest.encoder.ServerSpanEncoder;
import com.alipay.common.tracer.core.tracertest.type.TracerTestLogEnum;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @description: [test for SofaTracerRuntimeExceptionTest]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/7/30
 */
public class SofaTracerRuntimeExceptionTest {

    SofaTracer           sofaTracer;
    private final String tracerType           = "TracerTestService";
    private final String tracerGlobalTagKey   = "tracerkey";
    private final String tracerGlobalTagValue = "tracervalue";

    @Before
    public void init() {
        //client
        DiskReporterImpl clientReporter = new DiskReporterImpl(
            TracerTestLogEnum.RPC_CLIENT.getDefaultLogName(), new ClientSpanEncoder());
        //server
        DiskReporterImpl serverReporter = new DiskReporterImpl(
            TracerTestLogEnum.RPC_SERVER.getDefaultLogName(), new ServerSpanEncoder());
        sofaTracer = new SofaTracer.Builder(tracerType).withTag("tracer", "tracertest")
            .withClientReporter(clientReporter).withServerReporter(serverReporter)
            .withTag(tracerGlobalTagKey, tracerGlobalTagValue).build();
    }

    @Test
    public void testSofaTracerRuntimeException() {
        try {
            buildSofaTracerRuntimeExceptionNpe();
        } catch (SofaTracerRuntimeException e) {
            Assert.assertEquals("NullPointException occurs;", e.toString());
        }

        try {
            buildSofaTracerRuntimeException();
        } catch (SofaTracerRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Unsupported extractor format:"));
        }
    }

    private void buildSofaTracerRuntimeExceptionNpe() throws SofaTracerRuntimeException {
        try {
            sofaTracer.inject(null, null, null);
        } catch (Exception e) {
            throw new SofaTracerRuntimeException("NullPointException occurs;");
        }
    }

    private void buildSofaTracerRuntimeException() throws SofaTracerRuntimeException {
        try {
            HashMap<String, String> headers = new HashMap<String, String>();
            Format format = Mockito.mock(Format.class);
            sofaTracer.extract(format, new TestCarry(headers));
        } catch (Exception e) {
            throw new SofaTracerRuntimeException(e.getMessage());
        }
    }

    private static class TestCarry implements TextMap {
        private HashMap<String, String> headers;

        public TestCarry(HashMap<String, String> headers) {
            this.headers = headers;
        }

        @Override
        public void put(String key, String value) {
            headers.put(key, value);
        }

        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            return headers.entrySet().iterator();
        }
    }
}