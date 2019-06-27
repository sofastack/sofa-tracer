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
package com.alipay.common.tracer.core.reactor;

import com.alipay.common.tracer.core.span.SofaTracerSpan;
import reactor.util.annotation.NonNull;

import java.util.NoSuchElementException;

/**
 * hold sofa tracer span in reactor mode
 *
 * @author sx
 */
public class SofaTracerSpanContainer {
    private SofaTracerSpan span;

    public SofaTracerSpanContainer(SofaTracerSpan span) {
        this.span = span;
    }

    public SofaTracerSpanContainer() {
        this.span = null;
    }

    public boolean isPresent() {
        return span != null;
    }

    @NonNull
    public SofaTracerSpan get() {
        if (span == null) {
            throw new NoSuchElementException("No value present");
        }
        return span;
    }

    public void clear() {
        this.span = null;
    }

    public void set(SofaTracerSpan sofaTracerSpan) {
        this.span = sofaTracerSpan;
    }

    @Override
    public String toString() {
        return "SofaTracerSpanContainer{" +
                "span=" + span +
                '}';
    }
}
