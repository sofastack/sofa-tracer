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
package com.alipay.common.tracer.core.holder;

import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.context.trace.SofaTracerThreadLocalTraceContext;

/**
 * SofaTraceContextHolder
 *
 * @author yangguanchao
 * @since 2017/06/17
 */
public class SofaTraceContextHolder {

    /***
     * singleton SofaTraceContext
     */
    private static final SofaTraceContext SOFA_TRACE_CONTEXT = new SofaTracerThreadLocalTraceContext();

    /***
     * Get threadlocal alipay trace context
     * @return SofaTraceContext
     */
    public static SofaTraceContext getSofaTraceContext() {
        return SOFA_TRACE_CONTEXT;
    }
}
