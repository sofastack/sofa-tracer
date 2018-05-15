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
package com.alipay.common.tracer.core.listener;

import java.util.ArrayList;
import java.util.List;

/**
 * SpanReportListenerHolder
 *
 * @author yangguanchao
 * @since 2018/05/07
 */
public class SpanReportListenerHolder {

    private static List<SpanReportListener> spanReportListenersHolder = new ArrayList<SpanReportListener>();

    public static List<SpanReportListener> getSpanReportListenersHolder() {
        return spanReportListenersHolder;
    }

    public static void addSpanReportListeners(List<SpanReportListener> spanReportListenersHolder) {
        if (spanReportListenersHolder != null && spanReportListenersHolder.size() > 0) {
            SpanReportListenerHolder.spanReportListenersHolder.addAll(spanReportListenersHolder);
        }
    }

    public static void addSpanReportListener(SpanReportListener spanReportListener) {
        if (spanReportListener != null) {
            SpanReportListenerHolder.spanReportListenersHolder.add(spanReportListener);
        }
    }

    public static void clear() {
        spanReportListenersHolder.clear();
    }
}
