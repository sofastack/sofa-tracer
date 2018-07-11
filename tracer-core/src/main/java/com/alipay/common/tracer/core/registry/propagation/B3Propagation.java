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
package com.alipay.common.tracer.core.registry.propagation;

public interface B3Propagation<T> extends Propagation<T> {
    /**
     * 128/64 bit traceId lower-hex string (required)
     */
    static final String TRACE_ID_KEY_HEAD       = "X-B3-TraceId";
    /**
     * 64 bit spanId lower-hex string (required)
     */
    static final String SPAN_ID_KEY_HEAD        = "X-B3-SpanId";
    /**
     * 64 bit parentSpanId lower-hex string (absent on root span)
     */
    static final String PARENT_SPAN_ID_KEY_HEAD = "X-B3-ParentSpanId";
    /**
     * "1" means report this span to the tracing system, "0" means do not. (absent means defer the
     * decision to the receiver of this header).
     */
    static final String SAMPLED_KEY_HEAD        = "X-B3-Sampled";
    /**
     * "1" implies sampled and is a request to override collection-tier sampling policy.
     */
    static final String FLAGS_KEY_HEAD          = "X-B3-Flags";
    /**
     * Baggage items prefix
     */
    static final String BAGGAGE_KEY_PREFIX      = "baggage-";
    /**
     * System Baggage items prefix
     */
    static final String BAGGAGE_SYS_KEY_PREFIX  = "baggage-sys-";
}
