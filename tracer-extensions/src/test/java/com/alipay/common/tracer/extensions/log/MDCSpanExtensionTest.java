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
package com.alipay.common.tracer.extensions.log;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.async.SofaTracerRunnable;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.extensions.log.constants.MDCKeyConstants;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.MDC;

import static org.mockito.Mockito.mock;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/2/21 11:41 AM
 * @since:
 **/
public class MDCSpanExtensionTest {

    @Test
    public void testMDC() {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpanContext sofaTracerSpanContext = new SofaTracerSpanContext("12321", "0");
        SofaTracer sofaTracer = mock((SofaTracer.class));
        final SofaTracerSpan mockSpan = new SofaTracerSpan(sofaTracer, System.currentTimeMillis(),
            "test", sofaTracerSpanContext, null);
        sofaTraceContext.push(mockSpan);
        final String traceId = sofaTracerSpanContext.getTraceId();
        TestRunnable testRunnable = new TestRunnable(traceId, mockSpan);
        Thread thread = new Thread(new SofaTracerRunnable(testRunnable));
        thread.start();
    }

    class TestRunnable implements Runnable {

        private String traceId;
        SofaTracerSpan mockSpan;

        public TestRunnable(String traceId, SofaTracerSpan mockSpan) {
            this.traceId = traceId;
            this.mockSpan = mockSpan;
        }

        @Override
        public void run() {
            SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
            SofaTracerSpan currentSpan = sofaTraceContext.getCurrentSpan();
            Assert.assertTrue(currentSpan != mockSpan);
            String traceIdMdc = MDC.get(MDCKeyConstants.MDC_TRACEID);
            Assert.assertTrue(traceId.equals(traceIdMdc));
        }
    }

}
