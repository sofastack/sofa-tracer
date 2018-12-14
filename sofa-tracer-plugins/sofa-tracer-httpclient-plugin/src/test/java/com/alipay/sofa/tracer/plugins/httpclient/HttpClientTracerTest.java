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
package com.alipay.sofa.tracer.plugins.httpclient;

import com.alibaba.fastjson.JSON;
import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.samplers.SofaTracerPercentageBasedSampler;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.httpclient.base.AbstractTestBase;
import com.alipay.sofa.tracer.plugins.httpclient.base.client.HttpClientInstance;
import com.alipay.sofa.tracer.plugins.httpclient.base.controller.PostBody;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * HttpClientTracer Tester.
 *
 * @author guanchao.ygc
 * @version 2.2.2
 */
public class HttpClientTracerTest extends AbstractTestBase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.STAT_LOG_INTERVAL, "1");
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.SAMPLER_STRATEGY_NAME_KEY,
            SofaTracerPercentageBasedSampler.TYPE);
        SofaTracerConfiguration.setProperty(
            SofaTracerConfiguration.SAMPLER_STRATEGY_PERCENTAGE_KEY, "100");
    }

    @After
    public void after() {
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.STAT_LOG_INTERVAL, "");
    }

    @Test
    public void testHttpClientTracer() throws Exception {
        testHttpTracerUnique();
        //get
        testHttpClientGet(1);
        //post
        testHttpClientPost(2);
        //head
        testHttpClientHead(3);
    }

    public void testHttpTracerUnique() {
        HttpClientTracer a = HttpClientTracer.getHttpClientTracerSingleton();
        HttpClientTracer b = HttpClientTracer.getHttpClientTracerSingleton();
        assertEquals(a, b);
    }

    private void testHttpClientGet(int expectedLength) throws Exception {
        String httpGetUrl = urlHttpPrefix;
        String path = "/httpclient";
        String responseStr = new HttpClientInstance(10 * 1000).executeGet(httpGetUrl + path);
        assertFalse(StringUtils.isBlank(responseStr));

        TestUtil.waitForAsyncLog();

        //wait for async output
        List<String> contents = FileUtils
            .readLines(customFileLog(HttpClientLogEnum.HTTP_CLIENT_DIGEST.getDefaultLogName()));
        assertTrue(contents.size() == expectedLength);

        // stat log print cycle: 1s
        Thread.sleep(1000);

        //stat log
        List<String> statContents = FileUtils
            .readLines(customFileLog(HttpClientLogEnum.HTTP_CLIENT_STAT.getDefaultLogName()));
        assertTrue(statContents.size() == expectedLength);
    }

    private void testHttpClientHead(int expectedLength) throws Exception {
        String httpHeadUrl = urlHttpPrefix;
        String path = "/httpclient";
        String responseStr = new HttpClientInstance(10 * 1000).executeHead(httpHeadUrl + path);
        assertTrue(StringUtils.isBlank(responseStr));

        TestUtil.waitForAsyncLog();

        //wait for async output
        List<String> contents = FileUtils
            .readLines(customFileLog(HttpClientLogEnum.HTTP_CLIENT_DIGEST.getDefaultLogName()));
        assertTrue(contents.size() == expectedLength);

        // stat log print cycle: 1s
        Thread.sleep(1000);

        //stat log
        List<String> statContents = FileUtils
            .readLines(customFileLog(HttpClientLogEnum.HTTP_CLIENT_STAT.getDefaultLogName()));
        assertTrue(statContents.size() == expectedLength);
    }

    private void testHttpClientPost(int expectedLength) throws Exception {
        PostBody postBody = new PostBody();
        postBody.setAge(111);
        postBody.setFemale(false);
        postBody.setName("guanchao.ygc/xuelian");
        String httpGetUrl = urlHttpPrefix + "/httpclient";
        //baggage
        SofaTracer sofaTracer = HttpClientTracer.getHttpClientTracerSingleton().getSofaTracer();
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan sofaTracerSpan = (SofaTracerSpan) sofaTracer.buildSpan(
            "HttpClientTracer Baggage").start();
        sofaTracerSpan.setBaggageItem("key1", "baggage1");
        sofaTracerSpan.setBaggageItem("key2", "baggage2");
        sofaTraceContext.push(sofaTracerSpan);

        String responseStr = new HttpClientInstance((10 * 1000)).executePost(httpGetUrl,
            JSON.toJSONString(postBody));
        PostBody resultPostBody = JSON.parseObject(responseStr, PostBody.class);
        assertEquals(postBody, resultPostBody);

        TestUtil.waitForAsyncLog();

        //wait for async output
        List<String> contents = FileUtils
            .readLines(customFileLog(HttpClientLogEnum.HTTP_CLIENT_DIGEST.getDefaultLogName()));
        assertTrue(contents.size() == expectedLength);

        // stat log print cycle: 1s
        Thread.sleep(1000);

        //stat log
        List<String> statContents = FileUtils
            .readLines(customFileLog(HttpClientLogEnum.HTTP_CLIENT_STAT.getDefaultLogName()));
        assertTrue(statContents.size() == expectedLength);
    }
}
