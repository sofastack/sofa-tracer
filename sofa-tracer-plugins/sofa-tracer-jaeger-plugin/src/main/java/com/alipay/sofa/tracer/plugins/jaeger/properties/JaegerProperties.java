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
package com.alipay.sofa.tracer.plugins.jaeger.properties;

/**
 * JaegerProperties
 * @author: zhaochen
 */
public class JaegerProperties {
    public static final String JAEGER_IS_ENABLED_KEY                   = "com.alipay.sofa.tracer.jaeger.enabled";
    public static final String JAEGER_COLLECTOR_BASE_URL_KEY           = "com.alipay.sofa.tracer.jaeger.collector.baseUrl";
    public static final String JAEGER_COLLECTOR_MAX_PACKET_SIZE_KEY    = "com.alipay.sofa.tracer.jaeger.collector.maxPacketSizeBytes";
    public static final String JAEGER_RECEIVER_KEY                     = "com.alipay.sofa.tracer.jaeger.receiver";
    public static final String JAEGER_AGENT_HOST_KEY                   = "com.alipay.sofa.tracer.jaeger.agent.host";
    public static final String JAEGER_AGENT_PORT_KEY                   = "com.alipay.sofa.tracer.jaeger.agent.port";
    public static final String JAEGER_AGENT_MAX_PACKET_SIZE_KEY        = "com.alipay.sofa.tracer.jaeger.agent.maxPacketSizeBytes";
    public static final String JAEGER_FLUSH_INTERVAL_MS_KEY            = "com.alipay.sofa.tracer.jaeger.flushInterval";
    public static final String JAEGER_MAX_QUEUE_SIZE_KEY               = "com.alipay.sofa.tracer.jaeger.maxQueueSize";
    public static final String JAEGER_CLOSE_ENQUEUE_TIMEOUT_MILLIS_KEY = "com.alipay.sofa.tracer.jaeger.closeEnqueueTimeout";
    public static final String JAEGER_SERVICE_NAME_KEY                 = "spring.application.name";
}
