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
import com.alipay.common.tracer.core.base.AbstractTestBase;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * MockSofaTracer Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>八月 10, 2017</pre>
 */
public class MockSofaTracerTest extends AbstractTestBase {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getMockSofaTracer()
     */
    @Test
    public void testGetMockSofaTracer() throws Exception {
        SofaTracer mockSofaTracer = MockSofaTracer.getMockSofaTracer();
        assertNull(mockSofaTracer.getServerReporter());
        assertNull(mockSofaTracer.getClientReporter());
        assertTrue(mockSofaTracer != null);
    }

}
