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
package com.alipay.sofa.tracer.examples.httpclient;

import com.alipay.sofa.tracer.examples.httpclient.instance.OkHttpClientInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * @author xianglong.chen
 * @since 2019/1/17 13:33
 */
@SpringBootApplication
public class OkHttpDemoApplication {

    private static Logger logger = LoggerFactory.getLogger(OkHttpDemoApplication.class);

    public static void main(String[] args) throws Exception {
        SpringApplication.run(OkHttpDemoApplication.class, args);
        OkHttpClientInstance httpClientInstance = new OkHttpClientInstance();
        String httpGetUrl = "http://localhost:8080/okhttp";
        String responseStr = httpClientInstance.executeGet(httpGetUrl);
        logger.info("Response is {}", responseStr);
    }
}
