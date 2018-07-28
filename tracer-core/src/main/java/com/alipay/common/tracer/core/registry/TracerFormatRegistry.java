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

import io.opentracing.propagation.Format;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TracerFormatRegistry
 *
 * @author yangguanchao
 * @since 2017/06/23
 */
public class TracerFormatRegistry {

    private final static Map<Format<?>, RegistryExtractorInjector<?>> injectorsAndExtractors = new ConcurrentHashMap<Format<?>, RegistryExtractorInjector<?>>();

    static {
        TextMapFormatter textMapFormatter = new TextMapFormatter();
        HttpHeadersFormatter httpHeadersFormatter = new HttpHeadersFormatter();
        BinaryFormater binaryFormater = new BinaryFormater();
        TextMapB3Formatter textMapB3Formatter = new TextMapB3Formatter();
        HttpHeadersB3Formatter httpHeadersB3Formatter = new HttpHeadersB3Formatter();
        injectorsAndExtractors.put(textMapFormatter.getFormatType(), textMapFormatter);
        injectorsAndExtractors.put(httpHeadersFormatter.getFormatType(), httpHeadersFormatter);
        injectorsAndExtractors.put(binaryFormater.getFormatType(), binaryFormater);
        injectorsAndExtractors.put(textMapB3Formatter.getFormatType(), textMapB3Formatter);
        injectorsAndExtractors.put(httpHeadersB3Formatter.getFormatType(), httpHeadersB3Formatter);
    }

    @SuppressWarnings("unchecked")
    public static <T> RegistryExtractorInjector<T> getRegistry(Format<T> format) {
        return (RegistryExtractorInjector<T>) injectorsAndExtractors.get(format);
    }

    public static <T> void register(Format<T> format, RegistryExtractorInjector<T> extractor) {
        injectorsAndExtractors.put(format, extractor);
    }
}
