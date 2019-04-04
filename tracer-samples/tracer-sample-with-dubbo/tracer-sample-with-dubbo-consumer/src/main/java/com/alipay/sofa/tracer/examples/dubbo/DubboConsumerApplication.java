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
package com.alipay.sofa.tracer.examples.dubbo;

import com.alipay.sofa.tracer.examples.dubbo.facade.HelloService;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author: guolei.sgl (glmapper_2018@163.com) 2019/2/26 2:16 PM
 * @since:
 **/
@SpringBootApplication
public class DubboConsumerApplication {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference(async = false)
    public HelloService  helloService;

    public static void main(String[] args) {
        SpringApplication.run(DubboConsumerApplication.class);
    }

    @Bean
    public ApplicationRunner runner() {
        return args -> {
            logger.info(helloService.SayHello("sofa"));
        };
    }
}
