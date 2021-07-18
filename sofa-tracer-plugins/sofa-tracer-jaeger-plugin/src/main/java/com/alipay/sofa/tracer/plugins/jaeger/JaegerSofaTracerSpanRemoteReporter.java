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
package com.alipay.sofa.tracer.plugins.jaeger;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.listener.SpanReportListener;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.sofa.tracer.plugins.jaeger.adapter.JaegerSpanAdapter;
import com.alipay.sofa.tracer.plugins.jaeger.properties.JaegerProperties;
import io.jaegertracing.internal.reporters.RemoteReporter;
import io.jaegertracing.thrift.internal.senders.UdpSender;
import org.apache.thrift.transport.TTransportException;
import java.io.Closeable;
import java.io.IOException;

public class JaegerSofaTracerSpanRemoteReporter implements SpanReportListener, Closeable {
    private JaegerSpanAdapter adapter = new JaegerSpanAdapter();
    private UdpSender         jaegerUdpSender;
    private RemoteReporter    reporter;

    public JaegerSofaTracerSpanRemoteReporter(String host, int port, int maxPacketSize)
                                                                                       throws TTransportException {
        //默认使用的compact
        jaegerUdpSender = new UdpSender(host, port, maxPacketSize);

        //使用配置文件 sofa.tracer.properties来配置jaeger 中的command queue
        //向command queue中写入FlushCommand的间隔时间
        Integer flushInterval = SofaTracerConfiguration.getIntegerDefaultIfNull(
            JaegerProperties.JAEGER_AGENT_FLUSH_INTERVAL_MS_KEY, 1000);
        //command queue的大小，过大浪费空间，过小会导致span丢失
        Integer maxQueueSize = SofaTracerConfiguration.getIntegerDefaultIfNull(
            JaegerProperties.JAEGER_AGENT_MAX_QUEUE_SIZE_KEY, 100);
        //写入CloseCommand的超时时间
        Integer closeEnqueueTimeout = SofaTracerConfiguration.getIntegerDefaultIfNull(
            JaegerProperties.JAEGER_AGENT_CLOSE_ENQUEUE_TIMEOUT_MILLIS_KEY, 1000);

        reporter = new RemoteReporter.Builder().withSender(jaegerUdpSender)
            .withFlushInterval(flushInterval).withMaxQueueSize(maxQueueSize)
            .withCloseEnqueueTimeout(closeEnqueueTimeout).build();

    }

    @Override
    public void onSpanReport(SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null || !sofaTracerSpan.getSofaTracerSpanContext().isSampled()) {
            return;
        }
        //转换过程中创建的SofaTracer中包含的reporter会自动上传转换好的jaegerSpan
        adapter.convertAndReport(sofaTracerSpan, reporter);
    }

    @Override
    public void close() throws IOException {
        this.reporter.close();
    }

    public RemoteReporter getReporter() {
        return this.reporter;
    }

}
