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

    /***
     * 作为跨进程传输字段的关键字key或者头部标识信息,其 value 就是 {@link SofaTracerSpanContext} 的序列化表现:sofa tracer head
     */
    String FORMATER_KEY_HEAD = "sftc_head";

    /***
     * 获取支持的格式类型
     * @return 格式类型 {@link Format}
     */
    Format<T> getFormatType();

    /**
     * 从负载中提取出 Span 上下文
     *
     * @param carrier 负载
     * @return Span 上下文
     */
    SofaTracerSpanContext extract(T carrier);

    /***
     * 向负载中注入 Span 上下文
     * @param spanContext 要注入或者序列化的 span 上下文
     * @param carrier 负载
     */
    void inject(SofaTracerSpanContext spanContext, T carrier);
}
