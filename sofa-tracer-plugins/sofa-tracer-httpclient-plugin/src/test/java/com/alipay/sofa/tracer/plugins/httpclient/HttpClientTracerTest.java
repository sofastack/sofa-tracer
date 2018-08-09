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
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.httpclient.base.AbstractTestBase;
import com.alipay.sofa.tracer.plugins.httpclient.base.client.HttpClientInstance;
import com.alipay.sofa.tracer.plugins.httpclient.base.controller.PostBody;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * HttpClientTracer Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>08/08/2018</pre>
 */
public class HttpClientTracerTest extends AbstractTestBase {

    @Before
    public void setUp() {
        super.setUp();
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.STAT_LOG_INTERVAL, "1");
    }

    @After
    public void after() {
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.STAT_LOG_INTERVAL, "");
    }

    /**
     * Method: getHttpClientTracerSingleton()
     */
    @Test
    public void testHttpClientTracer() throws Exception {
        HttpClientTracer httpClientTracer = HttpClientTracer.getHttpClientTracerSingleton();
        HttpClientTracer httpClientTracer1 = HttpClientTracer.getHttpClientTracerSingleton();
        assertEquals(httpClientTracer, httpClientTracer1);
        String httpGetUrl = urlHttpPrefix;
        String path = "/httpclient";
        String responseStr = new HttpClientInstance(10 * 1000)
                .executeGet(httpGetUrl + path);
        assertFalse(StringUtils.isBlank(responseStr));
        Thread.sleep(3000);
        //wait for async output
        List<String> contents = FileUtils.readLines(new File(logDirectoryPath
                + File.separator
                + HttpClientLogEnum.HTTP_CLIENT_DIGEST
                .getDefaultLogName()));
        assertTrue(contents.size() == 1);
        //stat log
        List<String> statContents = FileUtils.readLines(new File(logDirectoryPath
                + File.separator
                + HttpClientLogEnum.HTTP_CLIENT_STAT
                .getDefaultLogName()));
        assertTrue(statContents.size() == 1);
    }

    @Test
    public void testPostHttpClient() throws Exception {
        PostBody postBody = new PostBody();
        postBody.setAge(111);
        postBody.setFemale(false);
        postBody.setName("guanchao.ygc/xuelian");
        String httpGetUrl = urlHttpPrefix + "/httpclient";
        String responseStr = new HttpClientInstance((10 * 1000)).executePost(httpGetUrl, JSON.toJSONString(postBody));
        PostBody resultPostBody = JSON.parseObject(responseStr, PostBody.class);
        assertEquals(postBody, resultPostBody);
    }
}
