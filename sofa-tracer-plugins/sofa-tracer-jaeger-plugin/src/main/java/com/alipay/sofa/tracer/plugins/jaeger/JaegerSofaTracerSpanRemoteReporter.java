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

import com.alipay.common.tracer.core.listener.SpanReportListener;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.sofa.tracer.plugins.jaeger.adapter.JaegerSpanAdapter;
import io.jaegertracing.internal.JaegerSpan;
import io.jaegertracing.internal.reporters.RemoteReporter;
import io.jaegertracing.thrift.internal.senders.UdpSender;
import org.apache.thrift.transport.TTransportException;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

public class JaegerSofaTracerSpanRemoteReporter implements SpanReportListener, Closeable {
    private JaegerSpanAdapter adapter = new JaegerSpanAdapter();
    private UdpSender         jaegerUdpSender;
    private RemoteReporter    reporter;

    public JaegerSofaTracerSpanRemoteReporter(String host, int port, int maxPacketSize) throws TTransportException {
        //默认使用的compact
        jaegerUdpSender = new UdpSender(host, port, maxPacketSize);
        //还可以设置udp的很多参数
        RemoteReporter.Builder builder = new RemoteReporter.Builder().withSender(jaegerUdpSender);
        reporter = builder.build();
    }

    @Override
    public void onSpanReport(SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null || !sofaTracerSpan.getSofaTracerSpanContext().isSampled()) {
            return;
        }
        JaegerSpan jaegerSpan = adapter.convertToJaegerSpan(sofaTracerSpan, reporter);
    }

    @Override
    public void close() throws IOException {
        this.reporter.close();
    }

}
