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
package com.alipay.common.tracer.core.tags;

import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.sofa.common.code.LogCode2Description;
import io.opentracing.tag.StringTag;

/**
 * SpanTags
 *
 * @author yangguanchao
 * @since 2017/07/01
 */
public class SpanTags {

    /**
     * current span tags
     */
    public static final StringTag CURR_APP_TAG = new StringTag("curr.app");

    public static void putTags(String key, String val) {
        SofaTracerSpan currentSpan = SofaTraceContextHolder.getSofaTraceContext().getCurrentSpan();
        if (checkTags(currentSpan)) {
            currentSpan.setTag(key, val);
        }
    }

    public static void putTags(String key, Number val) {
        SofaTracerSpan currentSpan = SofaTraceContextHolder.getSofaTraceContext().getCurrentSpan();
        if (checkTags(currentSpan)) {
            currentSpan.setTag(key, val);
        }
    }

    public static void putTags(String key, Boolean val) {
        SofaTracerSpan currentSpan = SofaTraceContextHolder.getSofaTraceContext().getCurrentSpan();
        if (checkTags(currentSpan)) {
            currentSpan.setTag(key, val);
        }
    }

    private static boolean checkTags(SofaTracerSpan currentSpan) {
        if (currentSpan == null) {
            SelfLog.error(LogCode2Description.convert(SofaTracerConstant.SPACE_ID, "01-00013"));
            return false;
        }
        String componentType = currentSpan.getSofaTracer().getTracerType();
        if (!componentType.equalsIgnoreCase(ComponentNameConstants.FLEXIBLE)) {
            SelfLog
                .error(String.format(
                    LogCode2Description.convert(SofaTracerConstant.SPACE_ID, "01-00014"),
                    componentType));
            return false;
        }
        return true;
    }
}
