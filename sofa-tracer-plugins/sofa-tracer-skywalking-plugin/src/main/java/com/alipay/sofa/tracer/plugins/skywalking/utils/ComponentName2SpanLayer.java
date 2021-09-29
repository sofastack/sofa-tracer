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
import com.alipay.sofa.tracer.plugins.skywalking.model.SpanLayer;

import java.util.HashMap;

/**
 * map componentName in sofaTracer to SpanLayer in SkyWalking
 * @author zhaochen
 */
public class ComponentName2SpanLayer {
    public static final HashMap<String, SpanLayer> map = new HashMap<>();
    static {
        map.put(ComponentNameConstants.DATA_SOURCE, SpanLayer.Database);
        map.put(ComponentNameConstants.DUBBO_CLIENT, SpanLayer.RPCFramework);
        map.put(ComponentNameConstants.DUBBO_SERVER, SpanLayer.RPCFramework);
        map.put(ComponentNameConstants.SOFA_RPC, SpanLayer.RPCFramework);
        map.put(ComponentNameConstants.HTTP_CLIENT, SpanLayer.Http);
        map.put(ComponentNameConstants.OK_HTTP, SpanLayer.Http);
        map.put(ComponentNameConstants.REST_TEMPLATE, SpanLayer.Http);
        map.put(ComponentNameConstants.SPRING_MVC, SpanLayer.Http);
        map.put(ComponentNameConstants.FLEXIBLE, SpanLayer.Http);
        map.put(ComponentNameConstants.FEIGN_CLIENT, SpanLayer.Http);
        map.put(ComponentNameConstants.KAFKAMQ_CONSUMER, SpanLayer.MQ);
        map.put(ComponentNameConstants.KAFKAMQ_SEND, SpanLayer.MQ);
        map.put(ComponentNameConstants.ROCKETMQ_CONSUMER, SpanLayer.MQ);
        map.put(ComponentNameConstants.ROCKETMQ_SEND, SpanLayer.MQ);
        map.put(ComponentNameConstants.RABBITMQ_CONSUMER, SpanLayer.MQ);
        map.put(ComponentNameConstants.RABBITMQ_SEND, SpanLayer.MQ);
        map.put(ComponentNameConstants.MSG_PUB, SpanLayer.MQ);
        map.put(ComponentNameConstants.MSG_SUB, SpanLayer.MQ);
        map.put(ComponentNameConstants.MONGO_CLIENT, SpanLayer.Cache);
        map.put(ComponentNameConstants.REDIS, SpanLayer.Cache);
    }
}
