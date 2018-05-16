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
package com.alipay.common.tracer.core.appender.file;

import com.alipay.common.tracer.core.appender.TraceAppender;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CompositeTraceAppender
 *
 * @author yangguanchao
 * @since 2017/06/25
 */
public class CompositeTraceAppender implements TraceAppender {

    private Map<String, TraceAppender> traceAppenders = new ConcurrentHashMap<String, TraceAppender>();

    /**
     * @param logName 日志名
     * @return 输出实例
     */
    public TraceAppender getAppender(String logName) {
        return traceAppenders.get(logName);
    }

    /**
     * @param logName 日志名
     * @param traceAppender 输出实例
     */
    public void putAppender(String logName, TraceAppender traceAppender) {
        traceAppenders.put(logName, traceAppender);
    }

    /**
     * @throws IOException 异常
     */
    @Override
    public void flush() throws IOException {
        for (TraceAppender traceAppender : traceAppenders.values()) {
            traceAppender.flush();
        }
    }

    /**
     * @param log 内容
     * @throws IOException 异常
     */
    @Override
    public void append(String log) throws IOException {
        for (TraceAppender traceAppender : traceAppenders.values()) {
            traceAppender.append(log);
        }
    }

    @Override
    public void cleanup() {
        for (TraceAppender traceAppender : traceAppenders.values()) {
            traceAppender.cleanup();
        }
    }
}
