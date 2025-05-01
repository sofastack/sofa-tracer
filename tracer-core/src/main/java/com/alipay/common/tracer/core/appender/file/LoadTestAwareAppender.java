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

    /** TraceAppender for non-pressure */
    private TimedRollingFileAppender nonLoadTestTraceAppender;
    /** TraceAppender for pressure*/
    private TimedRollingFileAppender loadTestTraceAppender;

    private LoadTestAwareAppender(TimedRollingFileAppender nonLoadTestTraceAppender,
                                  TimedRollingFileAppender loadTestTraceAppender) {
        this.nonLoadTestTraceAppender = nonLoadTestTraceAppender;
        this.loadTestTraceAppender = loadTestTraceAppender;
    }

    public static LoadTestAwareAppender createLoadTestAwareTimedRollingFileAppender(String logName,
                                                                                    boolean append) {
        TimedRollingFileAppender nonLoadTestTraceAppender = new TimedRollingFileAppender(logName,
                append);
        TimedRollingFileAppender loadTestTraceAppender = new TimedRollingFileAppender(
                "shadow" + File.separator + logName, append);
        return new LoadTestAwareAppender(nonLoadTestTraceAppender, loadTestTraceAppender);
    }

    public static LoadTestAwareAppender createLoadTestAwareTimedRollingFileAppender(String logName,
                                                                                    String rollingPolicy,
                                                                                    String logReserveConfig) {
        TimedRollingFileAppender nonLoadTestTraceAppender = new TimedRollingFileAppender(logName,
                rollingPolicy, logReserveConfig);
        TimedRollingFileAppender loadTestTraceAppender = new TimedRollingFileAppender(
                "shadow" + File.separator + logName, rollingPolicy, logReserveConfig);
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

    public void reset(String datePattern) {
        nonLoadTestTraceAppender.resetTimedRollingFilleConfig(datePattern);
        loadTestTraceAppender.resetTimedRollingFilleConfig(datePattern);
    }
}