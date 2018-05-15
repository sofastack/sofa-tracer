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
package com.alipay.common.tracer.test;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author luoguimu123
 * @version $Id: MDCMultiThreadTest.java, v 0.1 2017年06月19日 下午7:42 luoguimu123 Exp $
 */
public class MDCMultiThreadTest {

    private static final Logger logger = LoggerFactory.getLogger("traceLog");
    private int                 i      = 0;

    @Test
    public void test() throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (; i < 5; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    MDC.put("sessionId" + i, "f9e287fad9e84cff8b2c2f2ed92adbe6" + i);
                    Assert.assertEquals("sessionId" + i,
                        MDC.get("f9e287fad9e84cff8b2c2f2ed92adbe6" + i));
                }
            });
        }
        Thread.sleep(1000);
        Assert.assertNull(MDC.get("sessionId1"));
        Assert.assertNull(MDC.get("sessionId2"));
        Assert.assertNull(MDC.get("sessionId3"));
        Assert.assertNull(MDC.get("sessionId4"));
        Assert.assertNull(MDC.get("sessionId5"));
    }

}