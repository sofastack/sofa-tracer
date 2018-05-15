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
package com.alipay.common.tracer.core.reporter.composite;

import com.alipay.common.tracer.core.reporter.facade.Reporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * SofaTracerCompositeDigestReporterImpl Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>五月 8, 2018</pre>
 */
public class SofaTracerCompositeDigestReporterImplTest {

    /**
     * Method: addReporter(Reporter reporter)
     */
    @Test
    public void testAddReporter() throws Exception {
        SofaTracerCompositeDigestReporterImpl sofaTracerCompositeDigestReporter = new SofaTracerCompositeDigestReporterImpl();
        assertEquals(Reporter.COMPOSITE_REPORTER,
            sofaTracerCompositeDigestReporter.getReporterType());
        Reporter reporterMock1 = mock(Reporter.class);
        when(reporterMock1.getReporterType()).thenReturn("mock1");
        Reporter reporterMock2 = mock(Reporter.class);
        when(reporterMock2.getReporterType()).thenReturn("mock2");

        sofaTracerCompositeDigestReporter.addReporter(reporterMock1);
        sofaTracerCompositeDigestReporter.addReporter(reporterMock2);
        SofaTracerSpan span = mock(SofaTracerSpan.class);
        sofaTracerCompositeDigestReporter.report(span);
        verify(reporterMock1, times(1)).report(span);
        verify(reporterMock2, times(1)).report(span);
    }

}
