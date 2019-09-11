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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 *  TestTemplateTest
 * @author: guolei.sgl
 * @since : v2.3.0
 */
public class RestTemplateTest extends AbstractTestBase {

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
    public void testRestTemplate() throws IOException, InterruptedException {
        testGetFromEntity();
        testPostFromEntity();
        testRestTemplate404();
    }

    public void testRestTemplate404() throws InterruptedException, IOException {
        RestTemplate restTemplate = SofaTracerRestTemplateBuilder.buildRestTemplate();
        String restUrl = "http://localhost:8888/greeting";
        try {
            restTemplate.getForEntity(restUrl, String.class);
        } catch (Exception e) {
            Assert.assertTrue(e != null);
        }

        Thread.sleep(1000);
        //wait for async output
       List<String> contents = FileUtils.readLines(new File(                        
           logDirectoryPath + File.separator                                        
                   + RestTemplateLogEnum.REST_TEMPLATE_DIGEST.getDefaultLogName()));
        assertTrue(contents.size() == 3);
    }

    public void testGetFromEntity() throws IOException, InterruptedException {
        RestTemplate restTemplate = SofaTracerRestTemplateBuilder.buildRestTemplate();
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(urlHttpPrefix, Map.class);
        Assert.assertTrue(forEntity.getBody().containsKey("name"));
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

    public void testPostFromEntity() throws IOException, InterruptedException {
        RestTemplate restTemplate = SofaTracerRestTemplateBuilder.buildRestTemplate();
        PostBody postBody = new PostBody();
        postBody.setName("weiheng");
        ResponseEntity<PostBody> postBodyResponseEntity = restTemplate.postForEntity(urlHttpPrefix,
            postBody, PostBody.class);
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
