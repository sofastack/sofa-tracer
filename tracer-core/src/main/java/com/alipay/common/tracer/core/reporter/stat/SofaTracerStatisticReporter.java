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
package com.alipay.common.tracer.core.reporter.stat;

import com.alipay.common.tracer.core.reporter.stat.model.StatKey;
import com.alipay.common.tracer.core.reporter.stat.model.StatValues;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.util.Map;

/**
 * SofaTracerStatisticReporter
 * <p>
 * Reference: {com.alipay.common.tracer.tracer.StatTracer}
 * </p>
 * @author yangguanchao
 * @since 2017/06/26
 */
public interface SofaTracerStatisticReporter {

    /**
     * get the period time
     * @return
     */
    long getPeriodTime();

    /**
     * Get the unique identifier of the statistic type
     * @return
     */
    String getStatTracerName();

    /**
     * Update data to the slot
     * @param sofaTracerSpan
     */
    void reportStat(SofaTracerSpan sofaTracerSpan);

    /**
     * Switch the current subscript and return the stat before switching
     * @return
     */
    Map<StatKey, StatValues> shiftCurrentIndex();

    /**
     * When the method is called, it indicates that a cycle has passed,
     * to determine whether enough cycles have passed, and whether flush is needed.
     *
     * @return true:stat log can be printed and the framework will call {@link SofaTracerStatisticReporter#print}
     */
    boolean shouldPrintNow();

    /**
     * Print, you can print to a local disk, or you can report to a remote server
     * @param statKey
     * @param values
     */
    void print(StatKey statKey, long[] values);

    /**
     * close print
     */
    void close();
}
