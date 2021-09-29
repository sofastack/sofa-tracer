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
package com.alipay.sofa.tracer.plugins.skywalking.utils;

import com.alipay.common.tracer.core.constants.ComponentNameConstants;

import java.util.HashMap;

/**
 * map ComponentName in SOFATracer to ComponentId in SkyWalking
 * @author zhaochen
 */
public class ComponentName2ComponentId {
    public static final HashMap<String, Integer> componentName2IDMap = new HashMap<>();
    static {
        //componentId in SkyWalking: https://github.com/apache/skywalking/blob/master/oap-server/server-starter/src/main/resources/component-libraries.yml
        componentName2IDMap.put(ComponentNameConstants.UNKNOWN, 0);
        componentName2IDMap.put(ComponentNameConstants.H2, 4);
        componentName2IDMap.put(ComponentNameConstants.MYSQL, 5);
        componentName2IDMap.put(ComponentNameConstants.ORACLE, 6);
        componentName2IDMap.put(ComponentNameConstants.REDIS, 7);
        componentName2IDMap.put(ComponentNameConstants.MONGODB, 42);
        componentName2IDMap.put(ComponentNameConstants.MEMCACHED, 20);
        componentName2IDMap.put(ComponentNameConstants.SOFA_RPC, 43);
        componentName2IDMap.put(ComponentNameConstants.DUBBO_CLIENT, 3);
        componentName2IDMap.put(ComponentNameConstants.DUBBO_SERVER, 3);
        componentName2IDMap.put(ComponentNameConstants.HTTP_CLIENT, 2);
        componentName2IDMap.put(ComponentNameConstants.OK_HTTP, 12);
        componentName2IDMap.put(ComponentNameConstants.REST_TEMPLATE, 13);
        componentName2IDMap.put(ComponentNameConstants.SPRING_MVC, 14);
        componentName2IDMap.put(ComponentNameConstants.FEIGN_CLIENT, 11);
        componentName2IDMap.put(ComponentNameConstants.KAFKAMQ_CONSUMER, 41);
        componentName2IDMap.put(ComponentNameConstants.KAFKAMQ_SEND, 40);
        componentName2IDMap.put(ComponentNameConstants.ROCKETMQ_CONSUMER, 25);
        componentName2IDMap.put(ComponentNameConstants.ROCKETMQ_SEND, 25);
        componentName2IDMap.put(ComponentNameConstants.RABBITMQ_CONSUMER, 53);
        componentName2IDMap.put(ComponentNameConstants.RABBITMQ_SEND, 52);
        componentName2IDMap.put(ComponentNameConstants.MONGO_CLIENT, 42);
        componentName2IDMap.put(ComponentNameConstants.REDIS, 7);
        //following componentId doesn't defined in SkyWalking mark it as unknown
        componentName2IDMap.put(ComponentNameConstants.DATA_SOURCE, 0);
        componentName2IDMap.put(ComponentNameConstants.FLEXIBLE, 0);
        componentName2IDMap.put(ComponentNameConstants.MSG_PUB, 0);
        componentName2IDMap.put(ComponentNameConstants.MSG_SUB, 0);

    }
}
