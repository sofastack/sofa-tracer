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
package com.alipay.common.tracer.core.reporter.digest;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.reporter.stat.SofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracertest.encoder.ClientSpanEncoder;
import com.alipay.common.tracer.core.tracertest.type.TracerTestLogEnum;
import com.alipay.common.tracer.core.utils.StringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * DiskReporterImpl Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>五月 25, 2018</pre>
 */
public class DiskReporterImplTest {

    private String            clientLogType             = "client-log-disk-report.log";

    private String            expectRollingPolicy       = SofaTracerConfiguration
                                                            .getRollingPolicy(TracerTestLogEnum.RPC_CLIENT
                                                                .getRollingKey());

    private String            expectLogReserveConfig    = SofaTracerConfiguration
                                                            .getLogReserveConfig(TracerTestLogEnum.RPC_CLIENT
                                                                .getLogReverseKey());

    private ClientSpanEncoder expectedClientSpanEncoder = new ClientSpanEncoder();

    private DiskReporterImpl  clientReporter;

    private SofaTracerSpan    sofaTracerSpan;

    @Before
    public void before() throws Exception {
        this.clientReporter = new DiskReporterImpl(clientLogType, expectRollingPolicy,
            expectLogReserveConfig, expectedClientSpanEncoder);
        this.sofaTracerSpan = mock(SofaTracerSpan.class);
    }

    /**
     * Method: getStatReporter()
     */
    @Test
    public void testGetSetStatReporter() throws Exception {
        SofaTracerStatisticReporter statisticReporter = mock(SofaTracerStatisticReporter.class);
        this.clientReporter.setStatReporter(statisticReporter);
        assertEquals(statisticReporter, this.clientReporter.getStatReporter());
    }

    /**
     * Method: getDigestReporterType()
     */
    @Test
    public void testGetDigestReporterType() throws Exception {
        assertEquals(clientLogType, this.clientReporter.getDigestLogType());
    }

    /**
     * Method: getStatReporterType()
     */
    @Test
    public void testGetStatReporterType() throws Exception {
        assertTrue(StringUtils.isBlank(this.clientReporter.getStatReporterType()));
    }

    /**
     * Method: digestReport(SofaTracerSpan span)
     */
    @Test
    public void testDigestReport() throws Exception {
        this.clientReporter.digestReport(this.sofaTracerSpan);
        assertEquals(true, this.clientReporter.getIsDigestFileInited().get());
    }

    /**
     * Method: getDigestLogType()
     */
    @Test
    public void testGetDigestLogType() throws Exception {
        assertEquals(this.clientLogType, this.clientReporter.getDigestLogType());
    }

    /**
     * Method: getDigestRollingPolicy()
     */
    @Test
    public void testGetDigestRollingPolicy() throws Exception {
        String rollingPolicy = this.clientReporter.getDigestRollingPolicy();
        assertEquals(expectRollingPolicy, rollingPolicy);
    }

    /**
     * Method: getDigestLogReserveConfig()
     */
    @Test
    public void testGetDigestLogReserveConfig() throws Exception {
        String logReserveConfig = this.clientReporter.getDigestLogReserveConfig();
        assertEquals(expectLogReserveConfig, logReserveConfig);
    }

    /**
     * Method: getContextEncoder()
     * Method: getLogNameKey()
     */
    @Test
    public void testGetContextEncoder() throws Exception {
        assertEquals(expectedClientSpanEncoder, this.clientReporter.getContextEncoder());
        String logNameKey = this.clientReporter.getLogNameKey();
        assertTrue(StringUtils.isBlank(logNameKey));
    }

    /**
     * Method: initDigestFile()
     */
    @Test
    public void testInitDigestFile() throws Exception {

        /* 
        try { 
           Method method = DiskReporterImpl.getClass().getMethod("initDigestFile"); 
           method.setAccessible(true); 
           method.invoke(<Object>, <Parameters>); 
        } catch(NoSuchMethodException e) { 
        } catch(IllegalAccessException e) { 
        } catch(InvocationTargetException e) { 
        } 
        */
    }

}
