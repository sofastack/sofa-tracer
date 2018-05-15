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
package com.alipay.common.tracer.core.context.trace;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * SofaTracerThreadLocalTraceContext Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>七月 6, 2017</pre>
 */
public class SofaTracerThreadLocalTraceContextTest {

    private SofaTracer sofaTracer = new SofaTracer.Builder("SofaTracerThreadLocalTraceContextTest")
                                      .withTag("tracer", "SofaTraceContextHolderTest").build();

    /**
     * Method: push(SofaTracerSpan span)
     */
    @Test
    public void testPushPop() throws Exception {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan sofaTracerSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("op").start();
        sofaTraceContext.push(sofaTracerSpan);
        SofaTracerSpan tlSpan = sofaTraceContext.getCurrentSpan();
        assertEquals(sofaTracerSpan, tlSpan);
        assertTrue(sofaTraceContext.getThreadLocalSpanSize() == 1);
        //pop
        SofaTracerSpan popSpan = sofaTraceContext.pop();
        assertEquals(sofaTracerSpan, popSpan);
        //isEmpty
        assertTrue(sofaTraceContext.isEmpty());
        //push
        SofaTracerSpan sofaTracerSpan1 = (SofaTracerSpan) this.sofaTracer.buildSpan("1").start();
        SofaTracerSpan sofaTracerSpan2 = (SofaTracerSpan) this.sofaTracer.buildSpan("2").start();
        sofaTraceContext.push(sofaTracerSpan1);
        sofaTraceContext.push(sofaTracerSpan2);
        SofaTracerSpan getSpan = sofaTraceContext.getCurrentSpan();
        assertEquals("Size = " + sofaTraceContext.getThreadLocalSpanSize(), sofaTracerSpan2,
            getSpan);

        assertEquals(1, sofaTraceContext.getThreadLocalSpanSize());
        //clear
        sofaTraceContext.clear();
        assertTrue(sofaTraceContext.isEmpty());
    }

}
