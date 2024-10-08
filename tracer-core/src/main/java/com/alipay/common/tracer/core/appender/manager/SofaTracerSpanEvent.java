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
package com.alipay.common.tracer.core.appender.manager;

import com.alipay.common.tracer.core.span.SofaTracerSpan;

/**
 * @author liangen 3/10/17
 */
public class SofaTracerSpanEvent implements ObjectEvent {
    private volatile SofaTracerSpan sofaTracerSpan;

    /**
     * Getter method for property <tt>sofaTracerSpan</tt>.
     *
     * @return property value of sofaTracerSpan
     */
    public SofaTracerSpan getSofaTracerSpan() {
        return sofaTracerSpan;
    }

    /**
     * Setter method for property <tt>sofaTracerSpan</tt>.
     *
     * @param sofaTracerSpan  value to be assigned to property sofaTracerSpan
     */
    public void setSofaTracerSpan(SofaTracerSpan sofaTracerSpan) {
        this.sofaTracerSpan = sofaTracerSpan;
    }

    @Override
    public void clear() {
        setSofaTracerSpan(null);
    }
}