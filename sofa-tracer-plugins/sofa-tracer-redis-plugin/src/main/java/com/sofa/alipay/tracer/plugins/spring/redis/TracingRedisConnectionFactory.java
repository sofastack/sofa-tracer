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
package com.sofa.alipay.tracer.plugins.spring.redis;

import com.sofa.alipay.tracer.plugins.spring.redis.common.RedisActionWrapperHelper;
import com.sofa.alipay.tracer.plugins.spring.redis.connections.TracingReactiveRedisClusterConnection;
import com.sofa.alipay.tracer.plugins.spring.redis.connections.TracingReactiveRedisConnection;
import com.sofa.alipay.tracer.plugins.spring.redis.connections.TracingRedisClusterConnection;
import com.sofa.alipay.tracer.plugins.spring.redis.connections.TracingRedisConnection;
import com.sofa.alipay.tracer.plugins.spring.redis.connections.TracingRedisSentinelConnection;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.ReactiveRedisClusterConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;

/**
 * OpenTracing instrumentation of a {@link RedisConnectionFactory}.
 * <p>
 * This class delegates invocations to the given {@link RedisConnectionFactory}, returning
 * OpenTracing wrappers for {@link RedisConnection} and {@link RedisClusterConnection} only.
 *
 * @author Daniel del Castillo
 */
public class TracingRedisConnectionFactory implements RedisConnectionFactory,
                                          ReactiveRedisConnectionFactory {

    private final RedisConnectionFactory   delegate;

    private final RedisActionWrapperHelper actionWrapper;

    public TracingRedisConnectionFactory(RedisConnectionFactory delegate,
                                         RedisActionWrapperHelper actionWrapper) {
        this.delegate = delegate;
        this.actionWrapper = actionWrapper;
    }

    @Override
    public RedisConnection getConnection() {
        // support cluster connection
        RedisConnection connection = this.delegate.getConnection();
        if (connection instanceof RedisClusterConnection) {
            return new TracingRedisClusterConnection((RedisClusterConnection) connection,
                actionWrapper);
        }
        return new TracingRedisConnection(connection, actionWrapper);
    }

    @Override
    public RedisClusterConnection getClusterConnection() {
        return new TracingRedisClusterConnection(delegate.getClusterConnection(), actionWrapper);
    }

    @Override
    public boolean getConvertPipelineAndTxResults() {
        return delegate.getConvertPipelineAndTxResults();
    }

    @Override
    public RedisSentinelConnection getSentinelConnection() {
        return new TracingRedisSentinelConnection(delegate.getSentinelConnection(), actionWrapper);
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException e) {
        return delegate.translateExceptionIfPossible(e);
    }

    @Override
    public ReactiveRedisConnection getReactiveConnection() {
        if (this.delegate instanceof ReactiveRedisConnectionFactory) {
            ReactiveRedisConnectionFactory connectionFactory = (ReactiveRedisConnectionFactory) this.delegate;
            ReactiveRedisConnection connection = connectionFactory.getReactiveConnection();
            // support cluster connection
            if (connection instanceof ReactiveRedisClusterConnection) {
                return new TracingReactiveRedisClusterConnection(
                    (ReactiveRedisClusterConnection) connection, actionWrapper);
            }
            return new TracingReactiveRedisConnection(connectionFactory.getReactiveConnection(),
                actionWrapper);
        }
        // TODO: shouldn't we throw an exception?
        return null;
    }

    @Override
    public ReactiveRedisClusterConnection getReactiveClusterConnection() {
        if (delegate instanceof ReactiveRedisConnectionFactory) {
            return ((ReactiveRedisConnectionFactory) delegate).getReactiveClusterConnection();
        }
        // TODO: shouldn't we throw an exception?
        return null;
    }
}
