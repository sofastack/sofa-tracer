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
package com.alipay.sofa.tracer.boot.redis;

import com.alipay.sofa.tracer.boot.base.SpringBootWebApplication;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import redis.embedded.RedisServer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/11/19 8:05 PM
 * @since:
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBootWebApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-redis.properties")
public class SofaTracerRedisConnectionTest {

    private static RedisServer  redisServer;

    @Autowired
    private StringRedisTemplate template;

    @BeforeClass
    public static void beforeClass() {
        redisServer = RedisServer.builder().setting("bind 127.0.0.1").build();
        redisServer.start();
    }

    @AfterClass
    public static void afterClass() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    @Test
    public void test() {
        template.opsForValue().set("key", "value");
        assertEquals("value", template.opsForValue().get("key"));
    }
}
