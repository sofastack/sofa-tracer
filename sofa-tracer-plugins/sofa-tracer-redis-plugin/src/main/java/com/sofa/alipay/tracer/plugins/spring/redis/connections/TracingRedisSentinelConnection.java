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

import com.sofa.alipay.tracer.plugins.spring.redis.common.RedisActionWrapperHelper;
import org.springframework.data.redis.connection.NamedNode;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.data.redis.connection.RedisServer;

import java.io.IOException;
import java.util.Collection;

public class TracingRedisSentinelConnection implements RedisSentinelConnection {
    private final RedisSentinelConnection  redisSentinelConnection;
    private final RedisActionWrapperHelper actionWrapper;

    public TracingRedisSentinelConnection(RedisSentinelConnection redisSentinelConnection,
                                          RedisActionWrapperHelper actionWrapper) {
        this.redisSentinelConnection = redisSentinelConnection;
        this.actionWrapper = actionWrapper;
    }

    @Override
  public boolean isOpen() {
    return actionWrapper.decorate(redisSentinelConnection::isOpen,"isOpen");
  }

    @Override
  public void failover(NamedNode master) {
    actionWrapper.decorate(() -> redisSentinelConnection.failover(master),"failover");
  }

    @Override
  public Collection<RedisServer> masters() {
    return actionWrapper.decorate(redisSentinelConnection::masters,"masters");
  }

    @Override
    public Collection<RedisServer> slaves(NamedNode master) {
        return actionWrapper.decorate(() -> redisSentinelConnection.slaves(master), "slaves");
    }

    @Override
  public void remove(NamedNode master) {
    actionWrapper.decorate(() -> redisSentinelConnection.remove(master),"remove");
  }

    @Override
  public void monitor(RedisServer master) {
    actionWrapper.decorate(() -> redisSentinelConnection.monitor(master),"monitor");
  }

    @Override
  public void close() throws IOException {
    actionWrapper.decorateThrowing(() -> redisSentinelConnection.close(),"close");
  }
}
