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
package com.alipay.common.tracer.core.appender.encoder;

import io.opentracing.Span;

import java.io.IOException;

/**
 * SpanEncoder
 * <p>
 * Tracer Span log encoder, optimized for asynchronous queue calls, does not allow multi-threaded concurrent calls
 * </p>
 * @author yangguanchao
 * @since 2017/06/25
 */
public interface SpanEncoder<T extends Span> {

    /**
     * Separate fields according to custom rules and prepare to output to file
     *
     * @param span current span
     * @throws IOException
     * @return formatted output string
     */
    String encode(T span) throws IOException;
}
