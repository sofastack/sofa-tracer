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
package com.alipay.common.tracer.test.core.sofatracer;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.samplers.SofaTracerPercentageBasedSampler;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.test.TestUtil;
import com.alipay.common.tracer.test.base.AbstractTestBase;
import com.alipay.common.tracer.test.core.sofatracer.type.TracerTestLogEnum;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * SofaTracerStatisticsDemoTest
 *
 * @author yangguanchao
 * @since 2017/07/13
 */
public class SofaTracerStatisticsDemoTest extends AbstractTestBase {

    @Before
    public void beforeTest() throws Exception {

        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.SAMPLER_STRATEGY_NAME_KEY,
            SofaTracerPercentageBasedSampler.TYPE);
        SofaTracerConfiguration.setProperty(
            SofaTracerConfiguration.SAMPLER_STRATEGY_PERCENTAGE_KEY, "100");

        File f = customFileLog(TracerTestLogEnum.RPC_SERVER_DIGEST.getDefaultLogName());
        if (f.exists()) {
            FileUtils.writeStringToFile(f, "");
        }

        File f1 = customFileLog(TracerTestLogEnum.RPC_CLIENT_STAT.getDefaultLogName());
        if (f1.exists()) {
            FileUtils.writeStringToFile(f1, "");
        }

        File f2 = customFileLog(TracerTestLogEnum.RPC_SERVER_STAT.getDefaultLogName());
        if (f2.exists()) {
            FileUtils.writeStringToFile(f2, "");
        }

        File f3 = customFileLog(TracerTestLogEnum.RPC_CLIENT_DIGEST.getDefaultLogName());
        if (f3.exists()) {
            FileUtils.writeStringToFile(f3, "");
        }
    }

    @Test
    public void testBuildTracerStatisticsTest() throws Exception {
        //清除上下文避免影响
        SofaTraceContextHolder.getSofaTraceContext().clear();

        String serverSpanId = "0.2.3";
        SofaTracerSpan serverSpan = recoverServerSpan(serverSpanId);
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        //放到线程上下文
        sofaTraceContext.push(serverSpan);
        //在调用5次
        int times = 5;
        callClientTimes(times);

        SofaTracerSpan parentSpan1 = sofaTraceContext.pop();
        assertSame(parentSpan1, serverSpan);
        parentSpan1.finish();

        TestUtil.waitForAsyncLog();

        //assert
        //client digest
        List<String> clientDigestContents = FileUtils
            .readLines(customFileLog(TracerTestLogEnum.RPC_CLIENT_DIGEST.getDefaultLogName()));
        assertEquals(5, times);
        String traceId = serverSpan.getSofaTracerSpanContext().getTraceId();
        for (int i = 0; i < clientDigestContents.size(); i++) {
            String log = clientDigestContents.get(i);
            String[] logArray = log.split(",");
            assertEquals(traceId, logArray[1]);
            String beginId = logArray[2].substring(0, logArray[2].lastIndexOf("."));
            String endId = logArray[2].substring(logArray[2].lastIndexOf(".") + 1);
            assertEquals("Client Span Id : " + logArray[2], beginId, serverSpanId);
            assertTrue(Integer.parseInt(endId) <= times);
        }
        //server digest
        List<String> serverDigestContents = FileUtils
            .readLines(customFileLog(TracerTestLogEnum.RPC_SERVER_DIGEST.getDefaultLogName()));
        assertTrue(serverDigestContents.size() == 1);

        // stat cycle 1s
        Thread.sleep(1000);

        //client stat
        List<String> clientStatContents = FileUtils
            .readLines(customFileLog(TracerTestLogEnum.RPC_CLIENT_STAT.getDefaultLogName()));
        assertTrue(clientStatContents.size() == 1);

        //统计
        String[] clientArray = clientStatContents.get(clientStatContents.size() - 1).split(",");
        assertEquals(clientArray[6], String.valueOf(times * duration));
        //server stat
        List<String> serverStatContents = FileUtils
            .readLines(customFileLog(TracerTestLogEnum.RPC_SERVER_STAT.getDefaultLogName()));
        assertTrue(serverStatContents.size() == 1);
    }

    public void callClientTimes(int times) {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        for (int i = 0; i < times; i++) {
            SofaTracerSpan clientSpan = createClientSpan();
            SofaTracerSpan client = sofaTraceContext.pop();
            client.finish();
            assertSame(client, clientSpan);
            sofaTraceContext.push(client.getParentSofaTracerSpan());
        }
    }
}
