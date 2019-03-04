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
package com.alipay.sofa.tracer.plugins.dubbo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.sofa.tracer.plugins.dubbo.enums.DubboLogEnum;
import com.alipay.sofa.tracer.plugins.dubbo.impl.DubboServiceImpl;
import com.alipay.sofa.tracer.plugins.dubbo.service.DubboService;
import org.apache.commons.io.FileUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.util.List;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/2/27 8:36 PM
 * @since:
 **/
public class DubboSofaTracerTest {

    protected static String logDirectoryPath = System.getProperty("user.home") + File.separator
                                               + "logs" + File.separator + "tracelog";

    private static String   address          = "";

    @Before
    public void testBefore() throws Exception {
        cleanFile();
        // application
        ApplicationConfig application = new ApplicationConfig();
        application.setName("test-server");
        // registry
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("N/A");
        // 服务提供者协议配置
        ProtocolConfig protocol = new ProtocolConfig();
        protocol.setName("dubbo");
        protocol.setThreadpool("fixed");
        protocol.setPort(12280);
        protocol.setSerialization("hessian2");
        // 服务提供者连接注册中心，设置属性
        DubboServiceImpl dubboServiceImpl = new DubboServiceImpl();
        ServiceConfig<DubboService> service = new ServiceConfig<DubboService>();
        service.setApplication(application);
        service.setProtocol(protocol); // 多个协议可以用setProtocols()
        service.setInterface(DubboService.class.getName());
        service.setRef(dubboServiceImpl);
        service.setGroup("tracer");
        service.setVersion("1.0");
        service.setFilter("dubboSofaTracerFilter");
        service.setRegistry(registryConfig);
        //services.setRegister(false);
        // 暴露及注册服务
        service.export();
        List<URL> exportedUrls = service.getExportedUrls();
        Assert.assertTrue(exportedUrls.size() == 1);
        address = exportedUrls.get(0).toString();
    }

    @Test
    public void testTracer() throws Exception {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("N/A");
        // 服务调用者连接注册中心，设置属性
        ReferenceConfig<DubboService> reference = new ReferenceConfig<DubboService>(); // 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
        reference.setInterface(DubboService.class);
        reference.setRegistry(registryConfig);
        reference.setUrl(address);
        reference.setVersion("1.0");
        reference.setGroup("tracer");
        reference.setFilter("dubboSofaTracerFilter");
        DubboService service = reference.get();
        Assert.assertEquals(service.echoStr("sofa-tarcer"), "sofa-tarcer");

        Thread.sleep(500);
        //wait for async output
        List<String> serverContent = FileUtils.readLines(new File(
            logDirectoryPath + File.separator
                    + DubboLogEnum.DUBBO_SERVER_DIGEST.getDefaultLogName()));
        Assert.assertTrue(serverContent.size() == 1);
        String jsonData = serverContent.get(0);
        JSONObject json = JSON.parseObject(jsonData);
        Assert.assertEquals(json.getString(CommonSpanTags.SERVICE),
            "com.alipay.sofa.tracer.plugins.dubbo.service.DubboService");
        Assert.assertEquals(json.getString(CommonSpanTags.LOCAL_APP), "test-server");
        Assert.assertEquals(json.getString("spanId"), "0");
        Assert.assertEquals(json.getString(CommonSpanTags.METHOD), "echoStr");
        Assert.assertEquals(json.getString(CommonSpanTags.PROTOCOL), "dubbo");
        Assert.assertEquals(json.getString("span.kind"), "server");

        //wait for async output
        List<String> clientContents = FileUtils.readLines(new File(
            logDirectoryPath + File.separator
                    + DubboLogEnum.DUBBO_CLIENT_DIGEST.getDefaultLogName()));

        Assert.assertTrue(clientContents.size() == 1);
        String clientData = clientContents.get(0);
        JSONObject clientJson = JSON.parseObject(clientData);

        Assert.assertEquals(clientJson.getString(CommonSpanTags.SERVICE),
            "com.alipay.sofa.tracer.plugins.dubbo.service.DubboService");
        Assert.assertEquals(clientJson.getString(CommonSpanTags.LOCAL_APP), "test-server");
        Assert.assertEquals(clientJson.getString("spanId"), "0");
        Assert.assertEquals(clientJson.getString(CommonSpanTags.METHOD), "echoStr");
        Assert.assertEquals(clientJson.getString(CommonSpanTags.PROTOCOL), "dubbo");
        Assert.assertEquals(clientJson.getString("span.kind"), "client");

        Thread.sleep(60 * 1000);

        //wait for async output
        List<String> clientStatContents = FileUtils
            .readLines(new File(logDirectoryPath + File.separator
                                + DubboLogEnum.DUBBO_CLIENT_STAT.getDefaultLogName()));

        Assert.assertTrue(clientStatContents.size() == 1);

        //wait for async output
        List<String> serverStatContents = FileUtils
            .readLines(new File(logDirectoryPath + File.separator
                                + DubboLogEnum.DUBBO_SERVER_STAT.getDefaultLogName()));

        Assert.assertTrue(serverStatContents.size() == 1);

    }

    private void cleanFile() {
        DubboLogEnum[] dubboLogEnums = DubboLogEnum.values();
        for (DubboLogEnum dubboLogEnum : dubboLogEnums) {
            String logName = dubboLogEnum.getDefaultLogName();
            File logFile = new File(logDirectoryPath + File.separator + logName);
            if (logFile.exists()) {
                logFile.delete();
            }
        }
    }
}
