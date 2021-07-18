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

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("com.alipay.sofa.tracer.jaeger.agent")
public class JaegerAgentSofaTracerProperties {
    /**
     * the address of agent
     */
    private String  host          = "127.0.0.1";
    /**
     * whether report span to jaeger agent
     */
    private boolean enabled       = false;
    /**
     * jaeger agent port to accept jaeger.thrift
     */
    private int     port          = 6831;
    /**
     * the max byte of the packet
     * In UDP over IPv4, the limit is 65,507 bytes
     */
    private int     maxPacketSize = 65000;

    public String getHost() {
        return this.host;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxPacketSize() {
        return this.maxPacketSize;
    }

    public void setMaxPacketSize(int maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
    }
}
