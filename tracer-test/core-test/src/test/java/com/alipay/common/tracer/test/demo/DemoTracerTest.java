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
package com.alipay.common.tracer.test.demo;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.reporter.digest.DiskReporterImpl;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.test.base.AbstractTestBase;
import com.alipay.common.tracer.test.core.sofatracer.encoder.ClientSpanEncoder;
import com.alipay.common.tracer.test.core.sofatracer.encoder.ServerSpanEncoder;
import io.opentracing.tag.Tags;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * DemoTracerTest
 *
 * @author yangguanchao
 * @since 2017/08/12
 */
public class DemoTracerTest extends AbstractTestBase {

    private SofaTracer sofaTracer;

    @Before
    public void beforeInstance() throws IOException {
        //client
        DiskReporterImpl clientReporter = new DiskReporterImpl("client-digest.log",
            new ClientSpanEncoder());
        //server
        DiskReporterImpl serverReporter = new DiskReporterImpl("server-digest.log",
            new ServerSpanEncoder());

        sofaTracer = new SofaTracer.Builder("DEMO_TRACER").withClientReporter(clientReporter)
            .withServerReporter(serverReporter).build();
    }

    /**
     * Method: buildSpan(String operationName)
     */
    @Test
    public void testBuildSpan() throws Exception {
        //client span
        String clientSpanOperation = "clientSpanOperation";
        SofaTracerSpan clientSpan = (SofaTracerSpan) this.sofaTracer.buildSpan(clientSpanOperation)
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();
        //client do something
        //client finish
        clientSpan.finish();

        //server span
        String serverSpanOperation = "clientSpanOperation";
        SofaTracerSpan serverSpan = (SofaTracerSpan) this.sofaTracer.buildSpan(serverSpanOperation)
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();
        //server do something
        //server finish
        serverSpan.finish();

        //Log printed  asynchronous,but in order to show this demo we sleep 1 second
        TimeUnit.SECONDS.sleep(1);

        //check
        //client digest
        List<String> clientDigestContents = FileUtils.readLines(new File(logDirectoryPath
                                                                         + File.separator
                                                                         + "client-digest.log"));
        assertEquals(1, clientDigestContents.size());
        //server digest
        List<String> serverDigestContents = FileUtils.readLines(new File(logDirectoryPath
                                                                         + File.separator
                                                                         + "server-digest.log"));
        assertEquals(1, serverDigestContents.size());
    }
}
