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
package com.alipay.common.tracer.core.registry;

import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import io.opentracing.propagation.Format;

public interface RegistryExtractorInjector<T> {

    /**
     * As the keyword key or header identification information of the cross-process transmission field,
     * its value is the serialization representation of {@link SofaTracerSpanContext}: sofa tracer head
     */
    String FORMATER_KEY_HEAD = "sftc_head";

    /**
     * Get supported format types
     * @return Format type {@link Format}
     */
    Format<T> getFormatType();

    /**
     * Extract the Span context from the payload
     *
     * @param carrier payload
     * @return SpanContext
     */
    SofaTracerSpanContext extract(T carrier);

    /**
     * Inject a Span context into the payload
     * @param spanContext The span context to be injected or serialized
     * @param carrier payload
     */
    void inject(SofaTracerSpanContext spanContext, T carrier);
}
