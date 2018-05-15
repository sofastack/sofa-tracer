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
package com.alipay.common.tracer.core.holder;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * SofaTraceContextHolder Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>七月 6, 2017</pre>
 */
public class SofaTraceContextHolderTest {

    private final String tracerType = "SofaTraceContextHolderTest";

    private SofaTracer   sofaTracer;

    @Before
    public void before() throws Exception {
        sofaTracer = new SofaTracer.Builder(tracerType).withTag("tracer",
            "SofaTraceContextHolderTest").build();
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getSofaTraceContext()
     */
    @Test
    public void testGetSofaTraceContext() throws Exception {
        SofaTracerSpan pushedSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("span1").start();
        //span
        SofaTracerSpanContext pushedSofaTracerSpanContext = pushedSpan.getSofaTracerSpanContext();
        String traceId = pushedSofaTracerSpanContext.getTraceId();
        String spanId = pushedSofaTracerSpanContext.getSpanId();
        String parentSpanId = pushedSofaTracerSpanContext.getParentId();
        //assert
        assertTrue("\n" + pushedSpan.getSofaTracerSpanContext(), StringUtils.isBlank(parentSpanId));
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();

        sofaTraceContext.clear();
        SofaTracerSpan peekTracerSpan = sofaTraceContext.getCurrentSpan();
        SofaTracerSpan popTracerSpan = sofaTraceContext.pop();
        assertNull(peekTracerSpan);
        assertNull(popTracerSpan);
        assertTrue(sofaTraceContext.isEmpty());
        //push
        sofaTraceContext.push(null);
        assertEquals(0, sofaTraceContext.getThreadLocalSpanSize());
        sofaTraceContext.push(pushedSpan);
        assertEquals(1, sofaTraceContext.getThreadLocalSpanSize());
        sofaTraceContext.push(null);
        assertEquals(1, sofaTraceContext.getThreadLocalSpanSize());
        //get
        SofaTracerSpan gettedSpan = sofaTraceContext.getCurrentSpan();
        assertTrue(gettedSpan == pushedSpan);
        assertEquals(1, sofaTraceContext.getThreadLocalSpanSize());
        //pop
        SofaTracerSpan poppedSpan = sofaTraceContext.pop();
        assertEquals(0, sofaTraceContext.getThreadLocalSpanSize());
        assertTrue(poppedSpan == pushedSpan);
        assertTrue(sofaTraceContext.isEmpty());
    }
}
