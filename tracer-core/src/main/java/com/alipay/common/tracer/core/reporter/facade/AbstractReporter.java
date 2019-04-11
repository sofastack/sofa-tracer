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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AbstractDiskReporter
 *
 * Abstract class definition for the Reporter
 * @author yangguanchao
 * @since 2017/07/14
 */
public abstract class AbstractReporter implements Reporter {

    /**
     * Whether to turn off digest log print, the default is not closed;
     * closing means closing the digest and stat log
     */
    private AtomicBoolean isClosePrint = new AtomicBoolean(false);

    /**
     * report span
     * @param span
     */
    @Override
    public void report(SofaTracerSpan span) {
        if (span == null) {
            return;
        }
        //close print
        if (isClosePrint.get()) {
            return;
        }
        this.doReport(span);
    }

    /**
     * Subclass needs to implement the report method
     * @param span
     */
    public abstract void doReport(SofaTracerSpan span);

    @Override
    public void close() {
        isClosePrint.set(true);
    }

    public AtomicBoolean getIsClosePrint() {
        return isClosePrint;
    }

    public void setIsClosePrint(AtomicBoolean isClosePrint) {
        if (isClosePrint == null) {
            return;
        }
        this.isClosePrint.set(isClosePrint.get());
    }
}
