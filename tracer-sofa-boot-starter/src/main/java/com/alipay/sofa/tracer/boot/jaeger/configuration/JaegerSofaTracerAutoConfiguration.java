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
package com.alipay.sofa.tracer.boot.jaeger.configuration;

import com.alipay.sofa.tracer.boot.jaeger.properties.JaegerSofaTracerProperties;
import com.alipay.sofa.tracer.plugins.jaeger.JaegerSofaTracerSpanRemoteReporter;
import io.jaegertracing.internal.JaegerSpan;
import io.jaegertracing.internal.JaegerSpanContext;
import io.jaegertracing.internal.JaegerTracer;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JaegerSofaTracerAutoConfiguration
 * @author: zhaochen
 */
@Configuration
@EnableConfigurationProperties(JaegerSofaTracerProperties.class)
@ConditionalOnProperty(value = "com.alipay.sofa.tracer.jaeger.enabled", matchIfMissing = false)
@ConditionalOnClass({ JaegerSpan.class, JaegerTracer.class, JaegerSpanContext.class })
public class JaegerSofaTracerAutoConfiguration {
    @Autowired
    private JaegerSofaTracerProperties jaegerProperties;

    @Value("${spring.application.name}")
    private String                     serviceName;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "com.alipay.sofa.tracer.jaeger.receiver", havingValue = "collector", matchIfMissing = true)
    public JaegerSofaTracerSpanRemoteReporter jaegerSofaTracerSpanRemoteReporter()
                                                                                  throws TTransportException {
        return new JaegerSofaTracerSpanRemoteReporter(jaegerProperties.getCollectorBaseUrl(),
            jaegerProperties.getCollectorMaxPacketSizeBytes(), serviceName,
            jaegerProperties.getFlushIntervalMill(), jaegerProperties.getMaxQueueSize(),
            jaegerProperties.getCloseEnqueueTimeoutMill());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "com.alipay.sofa.tracer.jaeger.receiver", havingValue = "agent", matchIfMissing = false)
    public JaegerSofaTracerSpanRemoteReporter jaegerAgentSofaTracerSpanReporter()
                                                                                 throws TTransportException {
        return new JaegerSofaTracerSpanRemoteReporter(jaegerProperties.getAgentHost(),
            jaegerProperties.getAgentPort(), jaegerProperties.getAgentMaxPacketSizeBytes(),
            serviceName, jaegerProperties.getFlushIntervalMill(),
            jaegerProperties.getMaxQueueSize(), jaegerProperties.getCloseEnqueueTimeoutMill());
    }
}
