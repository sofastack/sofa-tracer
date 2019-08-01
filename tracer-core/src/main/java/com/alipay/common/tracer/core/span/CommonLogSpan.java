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
package com.alipay.common.tracer.core.span;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CommonLogSpan
 *
 * Mainly for recording specific sequential data
 *
 *
 * The reason to create a new object is to distinguish between the printing of CommonLogSpan and daily digest.
 *
 * com.alipay.common.tracer.core.span.LogData#EVENT_COMMON_TYPE_VALUE
 *
 * @author yangguanchao
 * @since 2017/07/15
 */
public class CommonLogSpan extends SofaTracerSpan {

    private static final int MAX_SLOT_SIZE = 32;

    /**
     * The common slot, all the log data that needs to be printed are placed in it.
     */
    private List<String>     slots         = new ArrayList<String>();

    private AtomicInteger    slotCounter   = new AtomicInteger(0);

    public CommonLogSpan(SofaTracer sofaTracer, long startTime, String operationName,
                         SofaTracerSpanContext sofaTracerSpanContext, Map<String, ?> tags) {
        this(sofaTracer, startTime, null, operationName, sofaTracerSpanContext, tags);
    }

    public CommonLogSpan(SofaTracer sofaTracer, long startTime,
                         List<SofaTracerSpanReferenceRelationship> spanReferences,
                         String operationName, SofaTracerSpanContext sofaTracerSpanContext,
                         Map<String, ?> tags) {
        super(sofaTracer, startTime, spanReferences, operationName, sofaTracerSpanContext, tags);
    }

    /**
     * Add an item to Slots that needs to be printed
     * @param slot
     */
    public void addSlot(String slot) {
        if (slot == null) {
            slot = StringUtils.EMPTY_STRING;
        }

        if (slotCounter.incrementAndGet() <= MAX_SLOT_SIZE) {
            slots.add(slot);
        } else {
            SelfLog.warn("Slots count（" + MAX_SLOT_SIZE + "）Fully");
        }
    }

    /**
     * Get all the content you need to print
     * @return
     */
    public List<String> getSlots() {
        return slots;
    }

    /**
     * Add slot list
     * @param stringArrayList
     */
    public void addSlots(List<String> stringArrayList) {
        if (stringArrayList == null || stringArrayList.isEmpty()) {
            return;
        }
        for (String slot : stringArrayList) {
            this.addSlot(slot);
        }
    }
}
