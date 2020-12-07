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
package com.sofa.alipay.tracer.plugins.spring.redis.connections;

import com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand;
import com.sofa.alipay.tracer.plugins.spring.redis.common.RedisActionWrapperHelper;
import org.springframework.data.redis.connection.ReactiveGeoCommands;
import org.springframework.data.redis.connection.ReactiveHashCommands;
import org.springframework.data.redis.connection.ReactiveHyperLogLogCommands;
import org.springframework.data.redis.connection.ReactiveKeyCommands;
import org.springframework.data.redis.connection.ReactiveListCommands;
import org.springframework.data.redis.connection.ReactiveNumberCommands;
import org.springframework.data.redis.connection.ReactivePubSubCommands;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveScriptingCommands;
import org.springframework.data.redis.connection.ReactiveServerCommands;
import org.springframework.data.redis.connection.ReactiveSetCommands;
import org.springframework.data.redis.connection.ReactiveStringCommands;
import org.springframework.data.redis.connection.ReactiveZSetCommands;
import reactor.core.publisher.Mono;

public class TracingReactiveRedisConnection implements ReactiveRedisConnection {
    private final ReactiveRedisConnection  reactiveRedisConnection;
    private final RedisActionWrapperHelper actionWrapper;

    public TracingReactiveRedisConnection(ReactiveRedisConnection reactiveRedisConnection,
                                          RedisActionWrapperHelper actionWrapper) {
        this.reactiveRedisConnection = reactiveRedisConnection;
        this.actionWrapper = actionWrapper;
    }

    @Override
    public void close() {
        reactiveRedisConnection.close();
    }

    @Override
    public Mono<Void> closeLater() {
        return reactiveRedisConnection.closeLater();
    }

    @Override
    public ReactiveKeyCommands keyCommands() {
        return reactiveRedisConnection.keyCommands();
    }

    @Override
    public ReactiveStringCommands stringCommands() {
        return reactiveRedisConnection.stringCommands();
    }

    @Override
    public ReactiveNumberCommands numberCommands() {
        return reactiveRedisConnection.numberCommands();
    }

    @Override
    public ReactiveListCommands listCommands() {
        return reactiveRedisConnection.listCommands();
    }

    @Override
    public ReactiveSetCommands setCommands() {
        return reactiveRedisConnection.setCommands();
    }

    @Override
    public ReactiveZSetCommands zSetCommands() {
        return reactiveRedisConnection.zSetCommands();
    }

    @Override
    public ReactiveHashCommands hashCommands() {
        return reactiveRedisConnection.hashCommands();
    }

    @Override
    public ReactiveGeoCommands geoCommands() {
        return reactiveRedisConnection.geoCommands();
    }

    @Override
    public ReactiveHyperLogLogCommands hyperLogLogCommands() {
        return reactiveRedisConnection.hyperLogLogCommands();
    }

    @Override
    public ReactivePubSubCommands pubSubCommands() {
        return reactiveRedisConnection.pubSubCommands();
    }

    @Override
    public ReactiveScriptingCommands scriptingCommands() {
        return reactiveRedisConnection.scriptingCommands();
    }

    @Override
    public ReactiveServerCommands serverCommands() {
        return reactiveRedisConnection.serverCommands();
    }

    @Override
  public Mono<String> ping() {
    return actionWrapper.doInScope(RedisCommand.PING, reactiveRedisConnection::ping);
  }
}
