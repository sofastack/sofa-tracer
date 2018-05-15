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
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * SofaTracerDemoTest
 *
 * @author yangguanchao
 * @since 2017/07/01
 */
public class SofaTracerDemoTest extends AbstractTestBase {

    protected String buildString(String[] keys) {
        XStringBuilder sb = new XStringBuilder();
        int i;
        for (i = 0; i < keys.length - 1; i++) {
            sb.append(keys[i] == null ? "" : keys[i]);
        }
        sb.appendRaw(keys[i] == null ? "" : keys[i]);
        return sb.toString();
    }

    /****
     * 测试摘要日志
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void testBuildTracer() throws Exception {

        SofaTraceContextHolder.getSofaTraceContext().clear();

        int serverInitSize = 0;
        File f = new File(logDirectoryPath + File.separator
                          + TracerTestLogEnum.RPC_SERVER_DIGEST.getDefaultLogName());
        if (f.exists()) {
            serverInitSize = FileUtils.readLines(f).size();
        }

        String serverSpanId = "0.1";
        SofaTracerSpan serverSpan = recoverServerSpan(serverSpanId);
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        //放到线程上下文
        sofaTraceContext.push(serverSpan);
        //假设处理很多逻辑 do start

        //create client1
        SofaTracerSpan clientSpan = createClientSpan();
        //do do do

        //pop client
        SofaTracerSpan client = sofaTraceContext.pop();
        assertEquals(clientSpan, client);
        client.finish();
        Thread.sleep(1000);
        SofaTracerSpan parentSpan = clientSpan.getParentSofaTracerSpan();
        if (parentSpan != null) {
            sofaTraceContext.push(parentSpan);
        }

        //client 2
        SofaTracerSpan clientSpan2 = createClientSpan();
        //do do do
        SofaTracerSpan client2 = sofaTraceContext.pop();
        assertEquals("Client2 : " + clientSpan2, clientSpan2, client2);
        client2.finish();
        SofaTracerSpan parentSpan2 = client2.getParentSofaTracerSpan();
        sofaTraceContext.push(parentSpan2);

        //server finish
        SofaTracerSpan parentSpan3 = sofaTraceContext.pop();
        parentSpan3.finish();
        Thread.sleep(5000);
        //assert
        assertSame(serverSpan, parentSpan);
        assertSame(parentSpan, parentSpan2);
        assertSame(parentSpan2, parentSpan3);
        //server digest
        List<String> serverDigestContents = FileUtils.readLines(new File(
            logDirectoryPath + File.separator
                    + TracerTestLogEnum.RPC_SERVER_DIGEST.getDefaultLogName()));
        assertTrue((serverDigestContents.size() - serverInitSize) == 1);
        String[] servers = serverDigestContents.get(serverDigestContents.size() - 1).split(",");
        assertEquals(serverSpanId, servers[2]);
        //assert digest log contents
        List<String> clientDigestContents = FileUtils.readLines(new File(
            logDirectoryPath + File.separator
                    + TracerTestLogEnum.RPC_CLIENT_DIGEST.getDefaultLogName()));
        assertTrue("clientDigestContentSize: " + clientDigestContents.size(),
            clientDigestContents.size() >= 2);
        String[] client1Strs = clientDigestContents.get(clientDigestContents.size() - 2).split(",");
        assertTrue(client1Strs[2].contains(serverSpanId));

        String[] client2Strs = clientDigestContents.get(clientDigestContents.size() - 1).split(",");
        assertTrue(client2Strs[2].contains(serverSpanId));

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
