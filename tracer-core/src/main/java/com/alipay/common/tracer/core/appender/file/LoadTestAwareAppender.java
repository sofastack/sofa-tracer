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

import java.io.File;
import java.io.IOException;

/**
 * LoadTestAwareAppender
 *
 * @author yangguanchao
 * @since 2017/06/25
 */
public final class LoadTestAwareAppender implements TraceAppender {

    // 非压测数据的 TraceAppender
    private TraceAppender nonLoadTestTraceAppender;
    // 压测数据的 TraceAppender
    private TraceAppender loadTestTraceAppender;

    private LoadTestAwareAppender(TraceAppender nonLoadTestTraceAppender,
                                  TraceAppender loadTestTraceAppender) {
        this.nonLoadTestTraceAppender = nonLoadTestTraceAppender;
        this.loadTestTraceAppender = loadTestTraceAppender;
    }

    public static LoadTestAwareAppender createLoadTestAwareTimedRollingFileAppender(String logName,
                                                                                    boolean append) {
        TraceAppender nonLoadTestTraceAppender = new TimedRollingFileAppender(logName, append);
        TraceAppender loadTestTraceAppender = new TimedRollingFileAppender("shadow"
                                                                           + File.separator
                                                                           + logName, append);
        return new LoadTestAwareAppender(nonLoadTestTraceAppender, loadTestTraceAppender);
    }

    public static LoadTestAwareAppender createLoadTestAwareTimedRollingFileAppender(String logName,
                                                                                    String rollingPolicy,
                                                                                    String logReserveConfig) {
        TraceAppender nonLoadTestTraceAppender = new TimedRollingFileAppender(logName,
            rollingPolicy, logReserveConfig);
        TraceAppender loadTestTraceAppender = new TimedRollingFileAppender("shadow"
                                                                           + File.separator
                                                                           + logName,
            rollingPolicy, logReserveConfig);
        return new LoadTestAwareAppender(nonLoadTestTraceAppender, loadTestTraceAppender);
    }

    public void append(String log, boolean loadTest) throws IOException {
        if (loadTest) {
            loadTestTraceAppender.append(log);
        } else {
            nonLoadTestTraceAppender.append(log);
        }
    }

    @Override
    public void flush() throws IOException {
        nonLoadTestTraceAppender.flush();
        loadTestTraceAppender.flush();
    }

    @Override
    public void append(String log) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanup() {
        nonLoadTestTraceAppender.cleanup();
        loadTestTraceAppender.cleanup();
    }
}
