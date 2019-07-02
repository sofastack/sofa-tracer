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

import com.alipay.common.tracer.core.span.SofaTracerSpan;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * SpanReportListenerHolder Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>五月 7, 2018</pre>
 */
public class SpanReportListenerHolderTest {

    @Before
    public void before() throws Exception {
        SpanReportListenerHolder.clear();
        assertEquals(0, SpanReportListenerHolder.getSpanReportListenersHolder().size());
    }

    /**
     * Method: getSpanReportListenersHolder()
     * addSpanReportListener(List<SpanReportListener> spanReportListenersHolder)
     */
    @Test
    public void testGetSpanReportListenersHolder() throws Exception {
        SpanReportListener listener1 = new SpanReportListener() {
            @Override
            public void onSpanReport(SofaTracerSpan sofaTracerSpan) {

            }
        };
        SpanReportListener listener2 = new SpanReportListener() {
            @Override
            public void onSpanReport(SofaTracerSpan sofaTracerSpan) {

            }
        };
        SpanReportListenerHolder.addSpanReportListener(listener1);
        SpanReportListenerHolder.addSpanReportListener(listener2);
        //2
        assertEquals(2, SpanReportListenerHolder.getSpanReportListenersHolder().size());
        SpanReportListenerHolder.clear();
        assertEquals(0, SpanReportListenerHolder.getSpanReportListenersHolder().size());
        List<SpanReportListener> listenerList = new ArrayList<SpanReportListener>();
        listenerList.add(listener1);
        listenerList.add(listener2);
        SpanReportListenerHolder.addSpanReportListeners(listenerList);
        assertEquals(2, SpanReportListenerHolder.getSpanReportListenersHolder().size());
    }

}
