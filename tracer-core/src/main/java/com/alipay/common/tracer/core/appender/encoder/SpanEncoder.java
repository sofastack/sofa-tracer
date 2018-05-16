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
 * Tracer 日志格式编码，为针对异步队列调用做优化，不允许多线程并发调用
 *
 * @author yangguanchao
 * @since 2017/06/25
 */
public interface SpanEncoder<T extends Span> {

    /***
     * 对 span 按照自定义的规则分隔字段,并准备输出到文件中
     *
     * @param span 要被格式化输出的上下文
     * @throws IOException 文件输出异常
     * @return 返回格式化输出的字符串
     */
    String encode(T span) throws IOException;
}
