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
package com.alipay.common.tracer.core.reporter.facade;

import com.alipay.common.tracer.core.span.SofaTracerSpan;

/**
 * Reporter
 *
 * @author yangguanchao
 * @since 2017/07/14
 */
public interface Reporter {

    /***
     * 上报到远程服务器的持久化类型
     */
    String REMOTE_REPORTER    = "REMOTE_REPORTER";

    /**
     * 组合类型
     */
    String COMPOSITE_REPORTER = "COMPOSITE_REPORTER";

    /***
     * 获取 Reporter 实例类型
     * @return 类型
     */
    String getReporterType();

    /***
     * 输出 span
     * @param span 要被输出的 span
     */
    void report(SofaTracerSpan span);

    /***
     *关闭输出 span 的能力
     */
    void close();
}
