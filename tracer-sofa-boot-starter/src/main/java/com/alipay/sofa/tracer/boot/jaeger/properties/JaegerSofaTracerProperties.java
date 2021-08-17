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
package com.alipay.sofa.tracer.boot.jaeger.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JaegerSofaTracerProperties
 * @author: zhaochen
 */
@Data
@ConfigurationProperties("com.alipay.sofa.tracer.jaeger")
public class JaegerSofaTracerProperties {

    private Agent     agent                   = new Agent();
    private Collector collector               = new Collector();

    private boolean   enabled                 = false;
    /**
     * receiver of span, it can be collector or agent
     */
    private String    receiver                = "collector";

    /**
     *The interval of writing FlushCommand to the command queue
     */
    private int       flushIntervalMill       = 1000;
    /**
     * size of the command queue is too large will waste space, and too small will cause the span to be lost
     */
    private Integer   maxQueueSize            = 10000;
    /**
     * Timeout for writing CloseCommand
     */
    private Integer   closeEnqueueTimeoutMill = 1000;

    public String getCollectorBaseUrl() {
        return this.collector.getBaseUrl();
    }

    public int getCollectorMaxPacketSizeBytes() {
        return this.collector.getMaxPacketSizeBytes();
    }

    public String getAgentHost() {
        return this.agent.getHost();
    }

    public int getAgentPort() {
        return this.agent.getPort();
    }

    public int getAgentMaxPacketSizeBytes() {
        return this.agent.getMaxPacketSizeBytes();
    }

    @Data
    private class Agent {
        private String host               = "127.0.0.1";
        private int    port               = 6831;
        /**
         * the max byte of the packet
         * In UDP over IPv4, the limit is 65,507 bytes
         */
        private int    maxPacketSizeBytes = 65000;
    }

    @Data
    private class Collector {
        private String baseUrl            = "http://localhost:14268/";
        /**
         * the max packet size in default it is 2MB
         */
        private int    maxPacketSizeBytes = 2 * 1024 * 1024;
    }

}
