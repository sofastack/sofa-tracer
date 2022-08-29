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
package com.alipay.sofa.tracer.plugins.jaeger.initialize;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.listener.SpanReportListener;
import com.alipay.common.tracer.core.listener.SpanReportListenerHolder;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.jaeger.JaegerSofaTracerSpanRemoteReporter;
import com.alipay.sofa.tracer.plugins.jaeger.properties.JaegerProperties;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;

/**
 * JaegerReportRegisterBean
 * @author: zhaochen
 */
public class JaegerReportRegisterBean implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws TTransportException {

        //Use the configuration file named sofa.tracer.properties to configure the command queue in jaeger
        //The interval of writing FlushCommand to the command queue
        int flushInterval = SofaTracerConfiguration.getIntegerDefaultIfNull(
            JaegerProperties.JAEGER_FLUSH_INTERVAL_MS_KEY, 1000);
        // size of the command queue is too large will waste space, and too small will cause the span to be lost
        int maxQueueSize = SofaTracerConfiguration.getIntegerDefaultIfNull(
            JaegerProperties.JAEGER_MAX_QUEUE_SIZE_KEY, 10000);
        //Timeout for writing CloseCommand
        int closeEnqueueTimeout = SofaTracerConfiguration.getIntegerDefaultIfNull(
            JaegerProperties.JAEGER_CLOSE_ENQUEUE_TIMEOUT_MILLIS_KEY, 1000);
        String serviceName = SofaTracerConfiguration
            .getProperty(JaegerProperties.JAEGER_SERVICE_NAME_KEY);

        String enabledStr = SofaTracerConfiguration
            .getProperty(JaegerProperties.JAEGER_IS_ENABLED_KEY);
        String receiver = SofaTracerConfiguration.getProperty(JaegerProperties.JAEGER_RECEIVER_KEY,
            "collector");
        boolean enabled = false;
        if (StringUtils.isNotBlank(enabledStr) && "true".equalsIgnoreCase(enabledStr)) {
            enabled = true;
        }
        if (!enabled) {
            return;
        }
        // receiver is collector
        if ("collector".equals(receiver)) {
            String baseUrl = SofaTracerConfiguration.getProperty(
                JaegerProperties.JAEGER_COLLECTOR_BASE_URL_KEY, "http://localhost:14268/");
            Integer maxPacketSize = SofaTracerConfiguration.getIntegerDefaultIfNull(
                JaegerProperties.JAEGER_COLLECTOR_MAX_PACKET_SIZE_KEY, 2 * 1024 * 1024);
            SpanReportListener spanReportListener = new JaegerSofaTracerSpanRemoteReporter(baseUrl,
                maxPacketSize, serviceName, flushInterval, maxQueueSize, closeEnqueueTimeout);
            List<SpanReportListener> spanReportListenerList = new ArrayList<SpanReportListener>();
            spanReportListenerList.add(spanReportListener);
            SpanReportListenerHolder.addSpanReportListeners(spanReportListenerList);
        }
        // receiver is agent
        if ("agent".equals(receiver)) {
            String host = SofaTracerConfiguration.getProperty(
                JaegerProperties.JAEGER_AGENT_HOST_KEY, "127.0.0.1");
            int port = SofaTracerConfiguration.getIntegerDefaultIfNull(
                JaegerProperties.JAEGER_AGENT_PORT_KEY, 6381);
            int maxPacketSize = SofaTracerConfiguration.getIntegerDefaultIfNull(
                JaegerProperties.JAEGER_AGENT_MAX_PACKET_SIZE_KEY, 65000);
            SpanReportListener spanReportListener = new JaegerSofaTracerSpanRemoteReporter(host,
                port, maxPacketSize, serviceName, flushInterval, maxQueueSize, closeEnqueueTimeout);
            List<SpanReportListener> spanReportListenerList = new ArrayList<SpanReportListener>();
            spanReportListenerList.add(spanReportListener);
            SpanReportListenerHolder.addSpanReportListeners(spanReportListenerList);
        }

    }
}
