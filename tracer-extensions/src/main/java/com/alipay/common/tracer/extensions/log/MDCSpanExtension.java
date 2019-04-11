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
package com.alipay.common.tracer.extensions.log;

import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.extensions.SpanExtension;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.extensions.log.constants.MDCKeyConstants;
import io.opentracing.Span;
import org.slf4j.MDC;

/**
 * @author luoguimu123
 * @version $Id: MDCSpanExtension.java, v 0.1 June 23, 2017 11:55 AM luoguimu123 Exp $
 */
public class MDCSpanExtension implements SpanExtension {

    @Override
    public void logStartedSpan(Span currentSpan) {
        if (currentSpan != null) {
            SofaTracerSpan span = (SofaTracerSpan) currentSpan;
            SofaTracerSpanContext sofaTracerSpanContext = span.getSofaTracerSpanContext();
            if (sofaTracerSpanContext != null) {
                MDC.put(MDCKeyConstants.MDC_TRACEID, sofaTracerSpanContext.getTraceId());
                MDC.put(MDCKeyConstants.MDC_SPANID, sofaTracerSpanContext.getSpanId());
            }
        }

    }

    @Override
    public void logStoppedSpan(Span currentSpan) {
        MDC.remove(MDCKeyConstants.MDC_TRACEID);
        MDC.remove(MDCKeyConstants.MDC_SPANID);
        if (currentSpan != null) {
            SofaTracerSpan span = (SofaTracerSpan) currentSpan;
            SofaTracerSpan parentSpan = span.getParentSofaTracerSpan();
            if (parentSpan != null) {
                SofaTracerSpanContext sofaTracerSpanContext = parentSpan.getSofaTracerSpanContext();
                if (sofaTracerSpanContext != null) {
                    MDC.put(MDCKeyConstants.MDC_TRACEID, sofaTracerSpanContext.getTraceId());
                    MDC.put(MDCKeyConstants.MDC_SPANID, sofaTracerSpanContext.getSpanId());
                }
            }
        }
    }

    @Override
    public void logStoppedSpanInRunnable(Span currentSpan) {
        MDC.remove(MDCKeyConstants.MDC_TRACEID);
        MDC.remove(MDCKeyConstants.MDC_SPANID);
    }

    @Override
    public String supportName() {
        return "slf4jmdc";
    }

}