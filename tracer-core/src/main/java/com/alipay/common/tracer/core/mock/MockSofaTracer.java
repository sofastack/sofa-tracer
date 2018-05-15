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
package com.alipay.common.tracer.core.mock;

import com.alipay.common.tracer.core.SofaTracer;

/**
 * MockSofaTracer
 *
 * @author yangguanchao
 * @since 2017/08/10
 */
public class MockSofaTracer {

    private static final String MOCK_TRACER      = "MOCK_TRACER";

    private static SofaTracer   MOCK_SOFA_TRACER = null;

    /***
     * Get the mocked OpenTracing Implementation
     * @return mock tracer
     */
    public static SofaTracer getMockSofaTracer() {
        if (MOCK_SOFA_TRACER == null) {
            synchronized (MockSofaTracer.class) {
                if (MOCK_SOFA_TRACER == null) {
                    MOCK_SOFA_TRACER = new SofaTracer.Builder(MOCK_TRACER).build();
                }
            }
        }
        return MOCK_SOFA_TRACER;
    }
}
