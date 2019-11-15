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
package com.alipay.sofa.tracer.plugins.zipkin;

import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * ZipkinSofaTracerSpanRemoteReporterTest
 * Add testOnSpanReport() method 2019/11/11
 * @author: guolei.sgl
 * @author: qingfeng.bf
 * @since: v2.3.0
 **/
public class ZipkinSofaTracerSpanRemoteReporterTest {
    private SofaTracerSpanContext sofaTracerSpanContext;

    @Before
    public void before() throws Exception {
        sofaTracerSpanContext = new SofaTracerSpanContext("traceId", "spanId", "parentId", false);
    }

    @Test
    public void testOnSpanReport() {
        SofaTracerSpanContext context = new SofaTracerSpanContext("traceId111", "spanId111",
            "parentId111", false);
        assertEquals(false, context.isSampled());
    }

    @Test
    public void testTraceIdToId() {
        // hex to decimal
        long ff = ZipkinSofaTracerSpanRemoteReporter.traceIdToId("FF");
        Assert.assertTrue(ff == 255);

        long decimal88 = ZipkinSofaTracerSpanRemoteReporter.traceIdToId("8");
        Assert.assertTrue(decimal88 == 8);

    }
}
