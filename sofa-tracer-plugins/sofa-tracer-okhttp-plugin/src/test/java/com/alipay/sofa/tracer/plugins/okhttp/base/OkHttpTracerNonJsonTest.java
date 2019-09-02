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
package com.alipay.sofa.tracer.plugins.okhttp.base;

import com.alibaba.fastjson.JSON;
import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.appender.TracerLogRootDaemon;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.reporter.digest.DiskReporterImpl;
import com.alipay.common.tracer.core.reporter.facade.Reporter;
import com.alipay.common.tracer.core.samplers.SofaTracerPercentageBasedSampler;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.okhttp.OkHttpDigestEncoder;
import com.alipay.sofa.tracer.plugins.okhttp.OkHttpLogEnum;
import com.alipay.sofa.tracer.plugins.okhttp.OkHttpTracer;
import com.alipay.sofa.tracer.plugins.okhttp.base.client.OkHttpClientInstance;
import com.alipay.sofa.tracer.plugins.okhttp.base.controller.PostBody;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/9/1 8:58 PM
 * @since:
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBootWebApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.properties")
public class OkHttpTracerNonJsonTest {

    protected static String logDirectoryPath = TracerLogRootDaemon.LOG_FILE_DIR;

    @LocalServerPort
    private int             definedPort;

    private String          urlHttpPrefix;

    @Before
    public void setUp() throws Exception {
        urlHttpPrefix = "http://localhost:" + definedPort;

        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.STAT_LOG_INTERVAL, "1");
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.SAMPLER_STRATEGY_NAME_KEY,
            SofaTracerPercentageBasedSampler.TYPE);
    }

    @Test
    public void testOkHttpTracer() throws Exception {
        testOkHttpTracerUnique();
        //get
        testOkHttpGet();
        //post
        testOkHttpPost();
    }

    private void testOkHttpGet() throws Exception {
        String httpGetUrl = urlHttpPrefix;
        String path = "/httpclient";

        String responseStr = new OkHttpClientInstance().executeGet(httpGetUrl + path);
        assertFalse(StringUtils.isBlank(responseStr));
    }

    private void testOkHttpPost() throws Exception {
        PostBody postBody = new PostBody();
        postBody.setAge(111);
        postBody.setFemale(false);
        postBody.setName("guanchao.ygc/xuelian");
        String httpGetUrl = urlHttpPrefix + "/httpclient";
        //baggage
        SofaTracer sofaTracer = OkHttpTracer.getOkHttpTracerSingleton().getSofaTracer();
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan sofaTracerSpan = (SofaTracerSpan) sofaTracer.buildSpan(
            "HttpClientTracer Baggage").start();
        sofaTracerSpan.setBaggageItem("key1", "baggage1");
        sofaTracerSpan.setBaggageItem("key2", "baggage2");
        sofaTraceContext.push(sofaTracerSpan);

        String responseStr = new OkHttpClientInstance().executePost(httpGetUrl,
            JSON.toJSONString(postBody));
        PostBody resultPostBody = JSON.parseObject(responseStr, PostBody.class);
        assertEquals(postBody, resultPostBody);

        //wait for async output
        List<String> contents = FileUtils.readLines(customFileLog(OkHttpLogEnum.OK_HTTP_DIGEST
            .getDefaultLogName()));
        Assert.assertTrue(contents.size() > 0);
        Assert.assertTrue(contents.get(0).split(",")[1].equalsIgnoreCase("test"));
    }

    protected static File customFileLog(String fileName) {
        return new File(logDirectoryPath + File.separator + fileName);
    }

    private void testOkHttpTracerUnique() {
        OkHttpTracer a = OkHttpTracer.getOkHttpTracerSingleton();
        OkHttpTracer b = OkHttpTracer.getOkHttpTracerSingleton();
        assertEquals(a, b);
        Reporter clientReporter = a.getSofaTracer().getClientReporter();
        Assert.assertTrue(clientReporter instanceof DiskReporterImpl);
        Assert
            .assertTrue(((DiskReporterImpl) clientReporter).getContextEncoder() instanceof OkHttpDigestEncoder);
    }
}
