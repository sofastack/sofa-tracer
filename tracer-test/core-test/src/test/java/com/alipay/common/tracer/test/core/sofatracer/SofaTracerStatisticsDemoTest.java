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

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.generator.TraceIdGenerator;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.common.tracer.test.base.AbstractTestBase;
import com.alipay.common.tracer.test.core.sofatracer.type.TracerTestLogEnum;
import io.opentracing.tag.Tags;
import org.apache.commons.io.FileUtils;
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

    protected String buildString(String[] keys) {
        XStringBuilder sb = new XStringBuilder();
        int i;
        for (i = 0; i < keys.length - 1; i++) {
            sb.append(keys[i] == null ? "" : keys[i]);
        }
        sb.appendRaw(keys[i] == null ? "" : keys[i]);
        return sb.toString();
    }

    @Test
    public void testBuildTracerStatisticsTest() throws Exception {

        //清除上下文避免影响
        SofaTraceContextHolder.getSofaTraceContext().clear();

        //避免上一个测试用例对统计日志的影响
        Thread.sleep(70000);

        int rpcServerDigestInitSize = 0;
        File f = new File(logDirectoryPath + File.separator
                          + TracerTestLogEnum.RPC_SERVER_DIGEST.getDefaultLogName());
        if (f.exists()) {
            rpcServerDigestInitSize = FileUtils.readLines(f).size();
        }

        int rpcClientStatInitSize = 0;
        File f1 = new File(logDirectoryPath + File.separator
                           + TracerTestLogEnum.RPC_CLIENT_STAT.getDefaultLogName());
        if (f1.exists()) {
            rpcClientStatInitSize = FileUtils.readLines(f1).size();
        }

        int rpcServerStatInitSize = 0;
        File f2 = new File(logDirectoryPath + File.separator
                           + TracerTestLogEnum.RPC_SERVER_STAT.getDefaultLogName());
        if (f2.exists()) {
            rpcServerStatInitSize = FileUtils.readLines(f2).size();
        }

        int rpcClientDigestInitSize = 0;
        File f3 = new File(logDirectoryPath + File.separator
                           + TracerTestLogEnum.RPC_CLIENT_DIGEST.getDefaultLogName());
        if (f3.exists()) {
            rpcClientDigestInitSize = FileUtils.readLines(f3).size();
        }

        String serverSpanId = "0.2.3";
        SofaTracerSpan serverSpan = recoverServerSpan(serverSpanId);
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        //放到线程上下文
        sofaTraceContext.push(serverSpan);
        //在调用5次
        int times = 5;
        callClientTimes(times);
        //dodo
        //dodo
        SofaTracerSpan parentSpan1 = sofaTraceContext.pop();
        assertSame(parentSpan1, serverSpan);
        parentSpan1.finish();
        //statistics
        Thread.sleep(70 * 1000);
        //assert
        //client digest
        List<String> clientDigestContents = FileUtils.readLines(new File(
            logDirectoryPath + File.separator
                    + TracerTestLogEnum.RPC_CLIENT_DIGEST.getDefaultLogName()));
        assertEquals(5, times);
        String traceId = serverSpan.getSofaTracerSpanContext().getTraceId();
        for (int i = rpcClientDigestInitSize; i < clientDigestContents.size(); i++) {
            String log = clientDigestContents.get(i);
            String[] logArray = log.split(",");
            assertEquals(traceId, logArray[1]);
            String beginId = logArray[2].substring(0, logArray[2].lastIndexOf("."));
            String endId = logArray[2].substring(logArray[2].lastIndexOf(".") + 1);
            assertEquals("Client Span Id : " + logArray[2], beginId, serverSpanId);
            assertTrue(Integer.parseInt(endId) <= times);
        }
        //server digest
        List<String> serverDigestContents = FileUtils.readLines(new File(
            logDirectoryPath + File.separator
                    + TracerTestLogEnum.RPC_SERVER_DIGEST.getDefaultLogName()));
        assertTrue((serverDigestContents.size() - rpcServerDigestInitSize) == 1);
        //client stat
        List<String> clientStatContents = FileUtils.readLines(new File(
            logDirectoryPath + File.separator
                    + TracerTestLogEnum.RPC_CLIENT_STAT.getDefaultLogName()));
        assertTrue((clientStatContents.size() - rpcClientStatInitSize) == 1);
        //统计
        String[] clientArray = clientStatContents.get(clientStatContents.size() - 1).split(",");
        assertEquals(clientArray[6], String.valueOf(times * duration));
        //server stat
        List<String> serverStatContents = FileUtils.readLines(new File(
            logDirectoryPath + File.separator
                    + TracerTestLogEnum.RPC_SERVER_STAT.getDefaultLogName()));
        assertTrue((serverStatContents.size() - rpcServerStatInitSize) == 1);
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

    public SofaTracerSpan recoverServerSpan(String serverSpanId) {
        //假设反序列化回的信息
        //生成 traceId
        String traceId = TraceIdGenerator.generate();
        //默认不采样
        SofaTracerSpanContext spanContext = new SofaTracerSpanContext(traceId, serverSpanId,
            StringUtils.EMPTY_STRING, false);

        String callServiceName = "callServiceName";
        //create server
        SofaTracerSpan serverSpan = new SofaTracerSpan(tracer, System.currentTimeMillis(),
            callServiceName, spanContext, null);
        serverSpan.setTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
        return serverSpan;
    }

    public SofaTracerSpan createClientSpan() {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        //pop
        SofaTracerSpan serverSpan = sofaTraceContext.pop();
        SofaTracer.SofaTracerSpanBuilder sofaTracerSpanBuilder = (SofaTracer.SofaTracerSpanBuilder) tracer
            .buildSpan("callService").asChildOf(serverSpan != null ? serverSpan.context() : null)
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
        SofaTracerSpan clientSpan = (SofaTracerSpan) sofaTracerSpanBuilder.start();
        clientSpan.setParentSofaTracerSpan(serverSpan);
        //push
        sofaTraceContext.push(clientSpan);
        return clientSpan;
    }
}
