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
 * 主要为了记录具体的顺序数据
 *
 * 之所以要新创建一个对象，主要是为了区分 CommonLogSpan 和日常摘要等的打印
 *
 * com.alipay.common.tracer.core.span.LogData#EVENT_COMMON_TYPE_VALUE
 *
 * @author yangguanchao
 * @since 2017/07/15
 */
public class CommonLogSpan extends SofaTracerSpan {

    private static final int MAX_SLOT_SIZE = 32;

    /** 通用的槽位，需要打印的日志数据全部都放到这里面 */
    private List<String>     slots         = new ArrayList<String>();

    private AtomicInteger    slotCounter   = new AtomicInteger(0);

    public CommonLogSpan(SofaTracer sofaTracer, long startTime, String operationName,
                         SofaTracerSpanContext sofaTracerSpanContext, Map<String, ?> tags) {
        //SofaTracer 可以 mock 在 此中,因为打印是通过显示 report
        this(sofaTracer, startTime, null, operationName, sofaTracerSpanContext, tags);
    }

    public CommonLogSpan(SofaTracer sofaTracer, long startTime,
                         List<SofaTracerSpanReferenceRelationship> spanReferences,
                         String operationName, SofaTracerSpanContext sofaTracerSpanContext,
                         Map<String, ?> tags) {
        //SofaTracer 可以 mock 在 此中,因为打印是通过显示 report
        super(sofaTracer, startTime, spanReferences, operationName, sofaTracerSpanContext, tags);
    }

    /**
     * 往 Slots 中增加一项需要打印的内容
     * @param slot 槽位
     */
    public void addSlot(String slot) {
        if (slot == null) {
            slot = StringUtils.EMPTY_STRING;
        }

        if (slotCounter.incrementAndGet() <= MAX_SLOT_SIZE) {
            slots.add(slot);
        } else {
            SelfLog.warn("槽位数量（" + MAX_SLOT_SIZE + "）已满");
        }
    }

    /**
     * 获取所有需要打印的内容
     * @return 栏位列表
     */
    public List<String> getSlots() {
        return slots;
    }

    /***
     * 添加 slot 列表
     * @param stringArrayList slot 列表
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
