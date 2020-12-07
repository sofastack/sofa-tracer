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
import org.springframework.data.redis.connection.ClusterInfo;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.RedisClusterNode.SlotRange;
import org.springframework.data.redis.connection.RedisClusterServerCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.types.RedisClientInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.BGREWRITEAOF;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.BGSAVE;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CLIENT_LIST;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CLUSTER_ADDSLOTS;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CLUSTER_COUNTKEYSINSLOT;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CLUSTER_DELSLOTS;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CLUSTER_FORGET;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CLUSTER_GETKEYSINSLOT;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CLUSTER_INFO;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CLUSTER_KEYSLOT;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CLUSTER_MASTER_SLAVE_MAP;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CLUSTER_MEET;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CLUSTER_NODES;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CLUSTER_NODE_FOR_KEY;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CLUSTER_NODE_FOR_SLOT;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CLUSTER_REPLICATE;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CLUSTER_SETSLOT;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CLUSTER_SLAVES;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CONFIG_GET;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CONFIG_RESETSTAT;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.CONFIG_SET;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.DBSIZE;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.EXECUTE;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.FLUSHALL;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.FLUSHDB;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.INFO;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.KEYS;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.LASTSAVE;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.PING;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.RANDOMKEY;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.SAVE;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.SCAN;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.SHUTDOWN;
import static com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand.TIME;

/**
 * OpenTracing instrumentation of a {@link RedisClusterConnection}.
 *
 * @author Daniel del Castillo
 */
public class TracingRedisClusterConnection extends TracingRedisConnection implements
                                                                         RedisClusterConnection {

    private final RedisClusterConnection   connection;
    private final RedisActionWrapperHelper actionWrapper;

    public TracingRedisClusterConnection(RedisClusterConnection connection,
                                         RedisActionWrapperHelper actionWrapper) {
        super(connection, actionWrapper);
        this.connection = connection;
        this.actionWrapper = actionWrapper;
    }

    @Override
    public RedisClusterServerCommands serverCommands() {
        return connection.serverCommands();
    }

    @Override
  public Iterable<RedisClusterNode> clusterGetNodes() {
    return actionWrapper.doInScope(CLUSTER_NODES, () -> connection.clusterGetNodes());
  }

    @Override
  public Collection<RedisClusterNode> clusterGetSlaves(RedisClusterNode master) {
    return actionWrapper.doInScope(CLUSTER_SLAVES, () -> connection.clusterGetSlaves(master));
  }

    @Override
  public Map<RedisClusterNode, Collection<RedisClusterNode>> clusterGetMasterSlaveMap() {
    return actionWrapper.doInScope(CLUSTER_MASTER_SLAVE_MAP, () -> connection.clusterGetMasterSlaveMap());
  }

    @Override
  public Integer clusterGetSlotForKey(byte[] key) {
    return actionWrapper.doInScope(CLUSTER_KEYSLOT, () -> connection.clusterGetSlotForKey(key));
  }

    @Override
  public RedisClusterNode clusterGetNodeForSlot(int slot) {
    return actionWrapper.doInScope(CLUSTER_NODE_FOR_SLOT, () -> connection.clusterGetNodeForSlot(slot));
  }

    @Override
  public RedisClusterNode clusterGetNodeForKey(byte[] key) {
    return actionWrapper.doInScope(CLUSTER_NODE_FOR_KEY, () -> connection.clusterGetNodeForKey(key));
  }

    @Override
  public ClusterInfo clusterGetClusterInfo() {
    return actionWrapper.doInScope(CLUSTER_INFO, () -> connection.clusterGetClusterInfo());
  }

    @Override
  public void clusterAddSlots(RedisClusterNode node, int... slots) {
    actionWrapper.doInScope(CLUSTER_ADDSLOTS, () -> connection.clusterAddSlots(node, slots));
  }

    @Override
  public void clusterAddSlots(RedisClusterNode node, SlotRange range) {
    actionWrapper.doInScope(CLUSTER_ADDSLOTS, () -> connection.clusterAddSlots(node, range));
  }

    @Override
  public Long clusterCountKeysInSlot(int slot) {
    return actionWrapper.doInScope(CLUSTER_COUNTKEYSINSLOT, () -> connection.clusterCountKeysInSlot(slot));
  }

    @Override
  public void clusterDeleteSlots(RedisClusterNode node, int... slots) {
    actionWrapper.doInScope(CLUSTER_DELSLOTS, () -> connection.clusterDeleteSlots(node, slots));
  }

    @Override
  public void clusterDeleteSlotsInRange(RedisClusterNode node, SlotRange range) {
    actionWrapper.doInScope(CLUSTER_DELSLOTS, () -> connection.clusterDeleteSlotsInRange(node, range));
  }

    @Override
  public void clusterForget(RedisClusterNode node) {
    actionWrapper.doInScope(CLUSTER_FORGET, () -> connection.clusterForget(node));
  }

    @Override
  public void clusterMeet(RedisClusterNode node) {
    actionWrapper.doInScope(CLUSTER_MEET, () -> connection.clusterMeet(node));
  }

    @Override
  public void clusterSetSlot(RedisClusterNode node, int slot, AddSlots mode) {
    actionWrapper.doInScope(CLUSTER_SETSLOT, () -> connection.clusterSetSlot(node, slot, mode));
  }

    @Override
  public List<byte[]> clusterGetKeysInSlot(int slot, Integer count) {
    return actionWrapper
        .doInScope(CLUSTER_GETKEYSINSLOT, () -> connection.clusterGetKeysInSlot(slot, count));
  }

    @Override
  public void clusterReplicate(RedisClusterNode master, RedisClusterNode slave) {
    actionWrapper.doInScope(CLUSTER_REPLICATE, () -> connection.clusterReplicate(master, slave));
  }

    @Override
  public String ping(RedisClusterNode node) {
    return actionWrapper.doInScope(PING, () -> connection.ping(node));
  }

    @Override
  public void bgReWriteAof(RedisClusterNode node) {
    actionWrapper.doInScope(BGREWRITEAOF, () -> connection.bgReWriteAof(node));
  }

    @Override
  public void bgSave(RedisClusterNode node) {
    actionWrapper.doInScope(BGSAVE, () -> connection.bgSave(node));
  }

    @Override
  public Long lastSave(RedisClusterNode node) {
    return actionWrapper.doInScope(LASTSAVE, () -> connection.lastSave(node));
  }

    @Override
  public void save(RedisClusterNode node) {
    actionWrapper.doInScope(SAVE, () -> connection.save(node));
  }

    @Override
  public Long dbSize(RedisClusterNode node) {
    return actionWrapper.doInScope(DBSIZE, () -> connection.dbSize(node));
  }

    @Override
  public void flushDb(RedisClusterNode node) {
    actionWrapper.doInScope(FLUSHDB, () -> connection.flushDb(node));
  }

    @Override
  public void flushAll(RedisClusterNode node) {
    actionWrapper.doInScope(FLUSHALL, () -> connection.flushAll(node));
  }

    @Override
  public Properties info(RedisClusterNode node) {
    return actionWrapper.doInScope(INFO, () -> connection.info(node));
  }

    @Override
  public Properties info(RedisClusterNode node, String section) {
    return actionWrapper.doInScope(INFO, () -> connection.info(node, section));
  }

    @Override
  public Set<byte[]> keys(RedisClusterNode node, byte[] pattern) {
    return actionWrapper.doInScope(KEYS, () -> connection.keys(node, pattern));
  }

    @Override
  public Cursor<byte[]> scan(RedisClusterNode node, ScanOptions options) {
    return actionWrapper.doInScope(SCAN, () -> connection.scan(node, options));
  }

    @Override
  public byte[] randomKey(RedisClusterNode node) {
    return actionWrapper.doInScope(RANDOMKEY, () -> connection.randomKey(node));
  }

    @Override
  public <T> T execute(String command, byte[] key, Collection<byte[]> args) {
    return actionWrapper.doInScope(EXECUTE, () -> connection.execute(command, key, args));
  }

    @Override
  public void shutdown(RedisClusterNode node) {
    actionWrapper.doInScope(SHUTDOWN, () -> connection.shutdown(node));
  }

    @Override
  public Properties getConfig(RedisClusterNode node, String pattern) {
    return actionWrapper.doInScope(CONFIG_GET, () -> connection.getConfig(node, pattern));
  }

    @Override
  public void setConfig(RedisClusterNode node, String param, String value) {
    actionWrapper.doInScope(CONFIG_SET, () -> connection.setConfig(node, param, value));
  }

    @Override
  public void resetConfigStats(RedisClusterNode node) {
    actionWrapper.doInScope(CONFIG_RESETSTAT, () -> connection.resetConfigStats(node));
  }

    @Override
  public Long time(RedisClusterNode node) {
    return actionWrapper.doInScope(TIME, () -> connection.time(node));
  }

    @Override
  public List<RedisClientInfo> getClientList(RedisClusterNode node) {
    return actionWrapper.doInScope(CLIENT_LIST, () -> connection.getClientList(node));
  }
}
