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
import com.alipay.common.tracer.core.utils.NetUtils;
import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.reporters.RemoteReporter;
import io.jaegertracing.thrift.internal.senders.HttpSender;
import io.jaegertracing.thrift.internal.senders.ThriftSender;
import io.jaegertracing.thrift.internal.senders.UdpSender;
import okhttp3.OkHttpClient;
import org.apache.thrift.transport.TTransportException;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * JaegerSofaTracerSpanRemoteReporter
 * @author: zhaochen
 */
public class JaegerSofaTracerSpanRemoteReporter implements SpanReportListener, Closeable {
    private JaegerSpanAdapter adapter                     = new JaegerSpanAdapter();
    private ThriftSender      sender;
    private RemoteReporter    reporter;
    private JaegerTracer      jaegerTracer;
    private static final int  OKHTTP_MAX_REQUESTS         = 64;
    private static final Long OKHTTP_READ_TIMEOUT_SECONDS = 60L;

    public JaegerSofaTracerSpanRemoteReporter(String host, int port, int maxPacketSize,
                                              String serviceName, int flushInterval,
                                              int maxQueueSize, int closeEnqueueTimeout)
                                                                                        throws TTransportException {
        //use UdpSender to send to the jaeger Agent
        sender = new UdpSender(host, port, maxPacketSize);
        buildTracer(serviceName, flushInterval, maxQueueSize, closeEnqueueTimeout);
    }

    public JaegerSofaTracerSpanRemoteReporter(String baseUrl, int maxPacketSizeBytes,
                                              String serviceName, int flushInterval,
                                              int maxQueueSize, int closeEnqueueTimeout)
                                                                                        throws TTransportException {
        String url = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "api/traces";
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(OKHTTP_READ_TIMEOUT_SECONDS,
            TimeUnit.SECONDS).build();
        client.dispatcher().setMaxRequests(OKHTTP_MAX_REQUESTS);
        client.dispatcher().setMaxRequestsPerHost(OKHTTP_MAX_REQUESTS);
        //use Http sender to send to Jaeger collector directly
        sender = new HttpSender.Builder(url).withMaxPacketSize(maxPacketSizeBytes)
            .withClient(client).build();
        buildTracer(serviceName, flushInterval, maxQueueSize, closeEnqueueTimeout);
    }

    private void buildTracer(String serviceName, int flushInterval, int maxQueueSize,
                             int closeEnqueueTimeout) {
        reporter = new RemoteReporter.Builder().withSender(sender).withFlushInterval(flushInterval)
            .withMaxQueueSize(maxQueueSize).withCloseEnqueueTimeout(closeEnqueueTimeout).build();

        jaegerTracer = new JaegerTracer.Builder(serviceName).withReporter(reporter)
            .withTraceId128Bit().withTag("ip", NetUtils.getLocalIpv4()).build();
    }

    @Override
    public void onSpanReport(SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null || !sofaTracerSpan.getSofaTracerSpanContext().isSampled()) {
            return;
        }
        // the sender in jaegerTracer will send the input sofaTracerSpan according to the setting
        adapter.convertAndReport(sofaTracerSpan, jaegerTracer);
    }

    @Override
    public void close() throws IOException {
        //close jaegerTracer and it will close reporter internal
        this.jaegerTracer.close();
    }

    public RemoteReporter getReporter() {
        return this.reporter;
    }

    public JaegerTracer getJaegerTracer() {
        return this.jaegerTracer;
    }

}
