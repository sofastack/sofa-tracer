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
import org.springframework.data.redis.connection.ReactiveClusterGeoCommands;
import org.springframework.data.redis.connection.ReactiveClusterHashCommands;
import org.springframework.data.redis.connection.ReactiveClusterHyperLogLogCommands;
import org.springframework.data.redis.connection.ReactiveClusterKeyCommands;
import org.springframework.data.redis.connection.ReactiveClusterListCommands;
import org.springframework.data.redis.connection.ReactiveClusterNumberCommands;
import org.springframework.data.redis.connection.ReactiveClusterServerCommands;
import org.springframework.data.redis.connection.ReactiveClusterSetCommands;
import org.springframework.data.redis.connection.ReactiveClusterStringCommands;
import org.springframework.data.redis.connection.ReactiveClusterZSetCommands;
import org.springframework.data.redis.connection.ReactivePubSubCommands;
import org.springframework.data.redis.connection.ReactiveRedisClusterConnection;
import org.springframework.data.redis.connection.ReactiveScriptingCommands;
import org.springframework.data.redis.connection.RedisClusterNode;
import reactor.core.publisher.Mono;

public class TracingReactiveRedisClusterConnection implements ReactiveRedisClusterConnection {

    private final ReactiveRedisClusterConnection reactiveRedisClusterConnection;
    private final RedisActionWrapperHelper       actionWrapper;

    public TracingReactiveRedisClusterConnection(ReactiveRedisClusterConnection reactiveRedisClusterConnection,
                                                 RedisActionWrapperHelper actionWrapper) {
        this.reactiveRedisClusterConnection = reactiveRedisClusterConnection;
        this.actionWrapper = actionWrapper;
    }

    @Override
    public ReactiveClusterKeyCommands keyCommands() {
        return reactiveRedisClusterConnection.keyCommands();
    }

    @Override
    public ReactiveClusterStringCommands stringCommands() {
        return reactiveRedisClusterConnection.stringCommands();
    }

    @Override
    public ReactiveClusterNumberCommands numberCommands() {
        return reactiveRedisClusterConnection.numberCommands();
    }

    @Override
    public ReactiveClusterListCommands listCommands() {
        return reactiveRedisClusterConnection.listCommands();
    }

    @Override
    public ReactiveClusterSetCommands setCommands() {
        return reactiveRedisClusterConnection.setCommands();
    }

    @Override
    public ReactiveClusterZSetCommands zSetCommands() {
        return reactiveRedisClusterConnection.zSetCommands();
    }

    @Override
    public ReactiveClusterHashCommands hashCommands() {
        return reactiveRedisClusterConnection.hashCommands();
    }

    @Override
    public ReactiveClusterGeoCommands geoCommands() {
        return reactiveRedisClusterConnection.geoCommands();
    }

    @Override
    public ReactiveClusterHyperLogLogCommands hyperLogLogCommands() {
        return reactiveRedisClusterConnection.hyperLogLogCommands();
    }

    @Override
    public ReactivePubSubCommands pubSubCommands() {
        return reactiveRedisClusterConnection.pubSubCommands();
    }

    @Override
    public ReactiveClusterServerCommands serverCommands() {
        return reactiveRedisClusterConnection.serverCommands();
    }

    @Override
  public Mono<String> ping(RedisClusterNode node) {
    return actionWrapper.doInScope(RedisCommand.PING,
        () -> reactiveRedisClusterConnection.ping(node));
  }

    @Override
    public void close() {
        reactiveRedisClusterConnection.close();
    }

    @Override
    public Mono<Void> closeLater() {
        return reactiveRedisClusterConnection.closeLater();
    }

    @Override
    public ReactiveScriptingCommands scriptingCommands() {
        return reactiveRedisClusterConnection.scriptingCommands();
    }

    @Override
  public Mono<String> ping() {
    return actionWrapper.doInScope(RedisCommand.PING, () -> reactiveRedisClusterConnection.ping());
  }
}
