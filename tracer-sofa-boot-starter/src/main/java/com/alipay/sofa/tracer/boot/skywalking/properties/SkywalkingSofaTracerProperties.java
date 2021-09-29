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
package com.alipay.sofa.tracer.boot.skywalking.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SkywalkingSofaTracerProperties
 * @author zhaochen
 */
@ConfigurationProperties("com.alipay.sofa.tracer.skywalking")
public class SkywalkingSofaTracerProperties {

    private String  baseUrl       = "http://localhost:12800/";
    private boolean enabled       = false;
    // size of the buffer
    private int     maxBufferSize = 10000;

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setMaxBufferSize(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }

    public int getMaxBufferSize() {
        return this.maxBufferSize;
    }

}
