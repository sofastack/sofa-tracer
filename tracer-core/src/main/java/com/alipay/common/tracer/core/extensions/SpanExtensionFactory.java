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
package com.alipay.common.tracer.core.extensions;

import io.opentracing.Span;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

/**
 *
 * @author luoguimu123
 * @version $Id: SpanExtensionFactory.java, v 0.1 2017年06月23日 上午11:50 luoguimu123 Exp $
 */
public class SpanExtensionFactory {

    private static Set<SpanExtension> spanExtensions = new HashSet<SpanExtension>();

    static {
        for (SpanExtension spanExtension : ServiceLoader.load(SpanExtension.class)) {
            spanExtensions.add(spanExtension);
        }
    }

    public static void logStartedSpan(Span currentSpan) {
        if (!spanExtensions.isEmpty() && currentSpan != null) {
            for (SpanExtension spanExtension : spanExtensions) {
                spanExtension.logStartedSpan(currentSpan);
            }
        }
    }

    public static void logStoppedSpan(Span currentSpan) {
        if (!spanExtensions.isEmpty()) {
            for (SpanExtension spanExtension : spanExtensions) {
                spanExtension.logStoppedSpan(currentSpan);
            }
        }
    }

    public static void logStoppedSpanInRunnable(Span currentSpan) {
        if (!spanExtensions.isEmpty()) {
            for (SpanExtension spanExtension : spanExtensions) {
                spanExtension.logStoppedSpanInRunnable(currentSpan);
            }
        }
    }
}