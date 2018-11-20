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
package com.sofa.tracer.plugins.resttemplate;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.sofa.alipay.tracer.plugins.rest.RestTemplateLogEnum;
import com.sofa.alipay.tracer.plugins.rest.SofaTracerRestTemplateBuilder;
import com.sofa.tracer.plugins.resttemplate.base.AbstractTestBase;
import com.sofa.tracer.plugins.resttemplate.base.PostBody;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertTrue;

/**
 * AsyncRestTemplateTest
 * @author: guolei.sgl
 * @since : v2.3.0
 */
public class AsyncRestTemplateTest extends AbstractTestBase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.STAT_LOG_INTERVAL, "1");
    }

    @After
    public void after() throws IOException {
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.STAT_LOG_INTERVAL, "");
    }

    @Test
    public void testAsyncRestTemplate() throws Exception {
        testGetFromEntity();
        testPostFromEntity();
    }

    public void testGetFromEntity() throws IOException, InterruptedException {
        AsyncRestTemplate restTemplate = SofaTracerRestTemplateBuilder.buildAsyncRestTemplate();
        ListenableFuture<ResponseEntity<Map>> forEntity = restTemplate.getForEntity(urlHttpPrefix,
            Map.class);
        forEntity.addCallback(new ListenableFutureCallback<ResponseEntity<Map>>() {
            @Override
            public void onFailure(Throwable ex) {
                Assert.assertTrue(false);
            }

            @Override
            public void onSuccess(ResponseEntity<Map> result) {
                Assert.assertTrue(result.getBody().containsKey("name"));
            }
        });
        Thread.sleep(1000);
        //wait for async output
        List<String> contents = FileUtils.readLines(new File(
            logDirectoryPath + File.separator
                    + RestTemplateLogEnum.REST_TEMPLATE_DIGEST.getDefaultLogName()));
        assertTrue(contents.size() == 1);
        //stat log
        List<String> statContents = FileUtils.readLines(new File(
            logDirectoryPath + File.separator
                    + RestTemplateLogEnum.REST_TEMPLATE_STAT.getDefaultLogName()));
        assertTrue(statContents.size() == 1);
    }

    public void testPostFromEntity() throws IOException, InterruptedException, ExecutionException {
        AsyncRestTemplate restTemplate = SofaTracerRestTemplateBuilder.buildAsyncRestTemplate();
        PostBody postBody = new PostBody();
        postBody.setName("weiheng");
        HttpEntity<PostBody> entity = new HttpEntity<PostBody>(postBody);
        ListenableFuture<ResponseEntity<PostBody>> responseEntityListenableFuture = restTemplate
            .postForEntity(urlHttpPrefix, entity, PostBody.class);
        ResponseEntity<PostBody> postBodyResponseEntity = responseEntityListenableFuture.get();
        Assert.assertTrue(postBodyResponseEntity.getBody().getName().equals("weiheng"));
        Thread.sleep(1000);
        //wait for async output
        List<String> contents = FileUtils.readLines(new File(
            logDirectoryPath + File.separator
                    + RestTemplateLogEnum.REST_TEMPLATE_DIGEST.getDefaultLogName()));
        assertTrue(contents.size() == 2);
        //stat log
        List<String> statContents = FileUtils.readLines(new File(
            logDirectoryPath + File.separator
                    + RestTemplateLogEnum.REST_TEMPLATE_STAT.getDefaultLogName()));
        assertTrue(statContents.size() == 2);
    }
}
