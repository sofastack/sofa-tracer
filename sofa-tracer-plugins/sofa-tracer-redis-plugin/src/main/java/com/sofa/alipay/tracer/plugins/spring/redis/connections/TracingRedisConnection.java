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
import com.sofa.alipay.tracer.plugins.spring.redis.common.RedisCommand;
import org.springframework.dao.DataAccessException;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metric;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.connection.RedisHyperLogLogCommands;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPipelineException;
import org.springframework.data.redis.connection.RedisScriptingCommands;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.connection.RedisSetCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.connection.Subscription;
import org.springframework.data.redis.connection.ValueEncoding;
import org.springframework.data.redis.connection.stream.ByteRecord;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.core.types.RedisClientInfo;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * OpenTracing instrumentation of a {@link RedisConnection}.
 *
 * @author Daniel del Castillo
 */
public class TracingRedisConnection implements RedisConnection {
    private final RedisConnection          connection;
    private final RedisActionWrapperHelper actionWrapper;

    public TracingRedisConnection(RedisConnection connection, RedisActionWrapperHelper actionWrapper) {
        this.connection = connection;
        this.actionWrapper = actionWrapper;
    }

    @Override
    public RedisGeoCommands geoCommands() {
        return connection.geoCommands();
    }

    @Override
    public RedisHashCommands hashCommands() {
        return connection.hashCommands();
    }

    @Override
    public RedisHyperLogLogCommands hyperLogLogCommands() {
        return connection.hyperLogLogCommands();
    }

    @Override
    public RedisKeyCommands keyCommands() {
        return connection.keyCommands();
    }

    @Override
    public RedisListCommands listCommands() {
        return connection.listCommands();
    }

    @Override
    public RedisSetCommands setCommands() {
        return connection.setCommands();
    }

    @Override
    public RedisScriptingCommands scriptingCommands() {
        return connection.scriptingCommands();
    }

    @Override
    public RedisServerCommands serverCommands() {
        return connection.serverCommands();
    }

    @Override
    public RedisStringCommands stringCommands() {
        return connection.stringCommands();
    }

    @Override
    public RedisZSetCommands zSetCommands() {
        return connection.zSetCommands();
    }

    @Override
    public void close() throws DataAccessException {
        connection.close();
    }

    @Override
    public boolean isClosed() {
        return connection.isClosed();
    }

    @Override
    public Object getNativeConnection() {
        return connection.getNativeConnection();
    }

    @Override
    public boolean isQueueing() {
        return connection.isQueueing();
    }

    @Override
    public boolean isPipelined() {
        return connection.isPipelined();
    }

    @Override
    public void openPipeline() {
        connection.openPipeline();
    }

    @Override
    public List<Object> closePipeline() throws RedisPipelineException {
        return connection.closePipeline();
    }

    @Override
    public boolean isSubscribed() {
        return connection.isSubscribed();
    }

    @Override
    public Subscription getSubscription() {
        return connection.getSubscription();
    }

    @Override
    public RedisSentinelConnection getSentinelConnection() {
        return new TracingRedisSentinelConnection(connection.getSentinelConnection(), actionWrapper);
    }

    @Override
  public Object execute(String command, byte[]... args) {
    return actionWrapper.doInScope(command, () -> connection.execute(command, args));
  }

    @Override
  public Boolean exists(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.EXISTS, key, () -> connection.exists(key));
  }

    @Override
  public Long exists(byte[]... keys) {
    return actionWrapper.doInScope(RedisCommand.EXISTS, keys, connection::exists);
  }

    @Override
  public Long del(byte[]... keys) {
    return actionWrapper.doInScope(RedisCommand.DEL, keys, () -> connection.del(keys));
  }

    @Override
  public Long unlink(byte[]... keys) {
    return actionWrapper.doInScope(RedisCommand.UNLINK, keys, connection::unlink);
  }

    @Override
  public DataType type(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.TYPE, key, () -> connection.type(key));
  }

    @Override
  public Long touch(byte[]... keys) {
    return actionWrapper.doInScope(RedisCommand.TOUCH, keys, connection::touch);
  }

    @Override
  public Set<byte[]> keys(byte[] pattern) {
    return actionWrapper.doInScope(RedisCommand.KEYS, () -> connection.keys(pattern));
  }

    @Override
  public Cursor<byte[]> scan(ScanOptions options) {
    return actionWrapper.doInScope(RedisCommand.SCAN, () -> connection.scan(options));
  }

    @Override
  public byte[] randomKey() {
    return actionWrapper.doInScope(RedisCommand.RANDOMKEY, () -> connection.randomKey());
  }

    @Override
  public void rename(byte[] sourceKey, byte[] targetKey) {
    actionWrapper.doInScope(RedisCommand.RENAME, sourceKey, () -> connection.rename(sourceKey, targetKey));
  }

    @Override
  public Boolean renameNX(byte[] sourceKey, byte[] targetKey) {
    return actionWrapper.doInScope(RedisCommand.RENAMENX, sourceKey,
        () -> connection.renameNX(sourceKey, targetKey));
  }

    @Override
  public Boolean expire(byte[] key, long seconds) {
    return actionWrapper.doInScope(RedisCommand.EXPIRE, key, () -> connection.expire(key, seconds));
  }

    @Override
  public Boolean pExpire(byte[] key, long millis) {
    return actionWrapper.doInScope(RedisCommand.PEXPIRE, key, () -> connection.pExpire(key, millis));
  }

    @Override
  public Boolean expireAt(byte[] key, long unixTime) {
    return actionWrapper.doInScope(RedisCommand.EXPIREAT, key, () -> connection.expireAt(key, unixTime));
  }

    @Override
  public Boolean pExpireAt(byte[] key, long unixTimeInMillis) {
    return actionWrapper
        .doInScope(RedisCommand.PEXPIREAT, key, () -> connection.pExpireAt(key, unixTimeInMillis));
  }

    @Override
  public Boolean persist(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.PERSIST, key, () -> connection.persist(key));
  }

    @Override
  public Boolean move(byte[] key, int dbIndex) {
    return actionWrapper.doInScope(RedisCommand.MOVE, key, () -> connection.move(key, dbIndex));
  }

    @Override
  public Long ttl(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.TTL, key, () -> connection.ttl(key));
  }

    @Override
  public Long ttl(byte[] key, TimeUnit timeUnit) {
    return actionWrapper.doInScope(RedisCommand.TTL, key, () -> connection.ttl(key, timeUnit));
  }

    @Override
  public Long pTtl(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.PTTL, key, () -> connection.pTtl(key));
  }

    @Override
  public Long pTtl(byte[] key, TimeUnit timeUnit) {
    return actionWrapper.doInScope(RedisCommand.PTTL, key, () -> connection.pTtl(key, timeUnit));
  }

    @Override
  public List<byte[]> sort(byte[] key, SortParameters params) {
    return actionWrapper.doInScope(RedisCommand.SORT, key, () -> connection.sort(key, params));
  }

    @Override
  public Long sort(byte[] key, SortParameters params, byte[] storeKey) {
    return actionWrapper.doInScope(RedisCommand.SORT, key, () -> connection.sort(key, params, storeKey));
  }

    @Override
  public byte[] dump(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.DUMP, key, () -> connection.dump(key));
  }

    @Override
  public void restore(byte[] key, long ttlInMillis, byte[] serializedValue) {
    actionWrapper.doInScope(RedisCommand.RESTORE, key,
        () -> connection.restore(key, ttlInMillis, serializedValue));
  }

    @Override
  public void restore(byte[] key, long ttlInMillis, byte[] serializedValue, boolean replace) {
    actionWrapper.doInScope(RedisCommand.RESTORE, key,
        () -> connection.restore(key, ttlInMillis, serializedValue, replace));
  }

    @Override
  public ValueEncoding encodingOf(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.ENCODING, key, () -> connection.encodingOf(key));
  }

    @Override
  public Duration idletime(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.IDLETIME, key, () -> connection.idletime(key));
  }

    @Override
  public Long refcount(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.REFCOUNT, key, () -> connection.refcount(key));
  }

    @Override
  public byte[] get(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.GET, key, () -> connection.get(key));
  }

    @Override
  public byte[] getSet(byte[] key, byte[] value) {
    return actionWrapper.doInScope(RedisCommand.GETSET, key, () -> connection.getSet(key, value));
  }

    @Override
  public List<byte[]> mGet(byte[]... keys) {
    return actionWrapper.doInScope(RedisCommand.MGET, keys, () -> connection.mGet(keys));
  }

    @Override
  public Boolean set(byte[] key, byte[] value) {
    return actionWrapper.doInScope(RedisCommand.SET, key, () -> connection.set(key, value));
  }

    @Override
  public Boolean set(byte[] key, byte[] value, Expiration expiration, SetOption option) {
    return actionWrapper
        .doInScope(RedisCommand.SET, key, () -> connection.set(key, value, expiration, option));
  }

    @Override
  public Boolean setNX(byte[] key, byte[] value) {
    return actionWrapper.doInScope(RedisCommand.SETNX, key, () -> connection.setNX(key, value));
  }

    @Override
  public Boolean setEx(byte[] key, long seconds, byte[] value) {
    return actionWrapper.doInScope(RedisCommand.SETEX, key, () -> connection.setEx(key, seconds, value));
  }

    @Override
  public Boolean pSetEx(byte[] key, long milliseconds, byte[] value) {
    return actionWrapper
        .doInScope(RedisCommand.PSETEX, key, () -> connection.pSetEx(key, milliseconds, value));
  }

    @Override
  public Boolean mSet(Map<byte[], byte[]> tuple) {
    return actionWrapper.doInScope(RedisCommand.MSET, () -> connection.mSet(tuple));
  }

    @Override
  public Boolean mSetNX(Map<byte[], byte[]> tuple) {
    return actionWrapper.doInScope(RedisCommand.MSETNX, () -> connection.mSetNX(tuple));
  }

    @Override
  public Long incr(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.INCR, key, () -> connection.incr(key));
  }

    @Override
  public Long incrBy(byte[] key, long value) {
    return actionWrapper.doInScope(RedisCommand.INCRBY, key, () -> connection.incrBy(key, value));
  }

    @Override
  public Double incrBy(byte[] key, double value) {
    return actionWrapper.doInScope(RedisCommand.INCRBY, key, () -> connection.incrBy(key, value));
  }

    @Override
  public Long decr(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.DECR, key, () -> connection.decr(key));
  }

    @Override
  public Long decrBy(byte[] key, long value) {
    return actionWrapper.doInScope(RedisCommand.DECRBY, key, () -> connection.decrBy(key, value));
  }

    @Override
  public Long append(byte[] key, byte[] value) {
    return actionWrapper.doInScope(RedisCommand.APPEND, key, () -> connection.append(key, value));
  }

    @Override
  public byte[] getRange(byte[] key, long begin, long end) {
    return actionWrapper.doInScope(RedisCommand.GETRANGE, key, () -> connection.getRange(key, begin, end));
  }

    @Override
  public void setRange(byte[] key, byte[] value, long offset) {
    actionWrapper.doInScope(RedisCommand.SETRANGE, key, () -> connection.setRange(key, value, offset));
  }

    @Override
  public Boolean getBit(byte[] key, long offset) {
    return actionWrapper.doInScope(RedisCommand.GETBIT, key, () -> connection.getBit(key, offset));
  }

    @Override
  public Boolean setBit(byte[] key, long offset, boolean value) {
    return actionWrapper.doInScope(RedisCommand.SETBIT, key, () -> connection.setBit(key, offset, value));
  }

    @Override
  public Long bitCount(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.BITCOUNT, key, () -> connection.bitCount(key));
  }

    @Override
  public Long bitCount(byte[] key, long begin, long end) {
    return actionWrapper.doInScope(RedisCommand.BITCOUNT, key, () -> connection.bitCount(key, begin, end));
  }

    @Override
  public List<Long> bitField(byte[] key, BitFieldSubCommands subCommands) {
    return actionWrapper
        .doInScope(RedisCommand.BITFIELD, key, () -> connection.bitField(key, subCommands));
  }

    @Override
  public Long bitOp(BitOperation op, byte[] destination, byte[]... keys) {
    return actionWrapper
        .doInScope(RedisCommand.BITOP, keys, () -> connection.bitOp(op, destination, keys));
  }

    @Override
  public Long bitPos(byte[] key, boolean bit, org.springframework.data.domain.Range<Long> range) {
    return actionWrapper.doInScope(RedisCommand.BITPOS, key, () -> connection.bitPos(key, bit, range));
  }

    @Override
  public Long strLen(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.STRLEN, key, () -> connection.strLen(key));
  }

    @Override
  public Long rPush(byte[] key, byte[]... values) {
    return actionWrapper.doInScope(RedisCommand.RPUSH, key, () -> connection.rPush(key, values));
  }

    @Override
    public List<Long> lPos(byte[] key, byte[] element, Integer rank, Integer count) {
        return actionWrapper.doInScope(RedisCommand.LPOS, key, () -> connection.lPos(key, element, rank, count));
    }

    @Override
  public Long lPush(byte[] key, byte[]... values) {
    return actionWrapper.doInScope(RedisCommand.LPUSH, key, () -> connection.lPush(key, values));
  }

    @Override
  public Long rPushX(byte[] key, byte[] value) {
    return actionWrapper.doInScope(RedisCommand.RPUSHX, key, () -> connection.rPushX(key, value));
  }

    @Override
  public Long lPushX(byte[] key, byte[] value) {
    return actionWrapper.doInScope(RedisCommand.LPUSHX, key, () -> connection.lPushX(key, value));
  }

    @Override
  public Long lLen(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.LLEN, key, () -> connection.lLen(key));
  }

    @Override
  public List<byte[]> lRange(byte[] key, long start, long end) {
    return actionWrapper.doInScope(RedisCommand.LRANGE, key, () -> connection.lRange(key, start, end));
  }

    @Override
  public void lTrim(byte[] key, long start, long end) {
    actionWrapper.doInScope(RedisCommand.LTRIM, key, () -> connection.lTrim(key, start, end));
  }

    @Override
  public byte[] lIndex(byte[] key, long index) {
    return actionWrapper.doInScope(RedisCommand.LINDEX, key, () -> connection.lIndex(key, index));
  }

    @Override
  public Long lInsert(byte[] key, Position where, byte[] pivot, byte[] value) {
    return actionWrapper
        .doInScope(RedisCommand.LINSERT, key, () -> connection.lInsert(key, where, pivot, value));
  }

    @Override
  public void lSet(byte[] key, long index, byte[] value) {
    actionWrapper.doInScope(RedisCommand.LSET, key, () -> connection.lSet(key, index, value));
  }

    @Override
  public Long lRem(byte[] key, long count, byte[] value) {
    return actionWrapper.doInScope(RedisCommand.LREM, key, () -> connection.lRem(key, count, value));
  }

    @Override
  public byte[] lPop(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.LPOP, key, () -> connection.lPop(key));
  }

    @Override
  public byte[] rPop(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.RPOP, key, () -> connection.rPop(key));
  }

    @Override
  public List<byte[]> bLPop(int timeout, byte[]... keys) {
    return actionWrapper.doInScope(RedisCommand.BLPOP, keys, () -> connection.bLPop(timeout, keys));
  }

    @Override
  public List<byte[]> bRPop(int timeout, byte[]... keys) {
    return actionWrapper.doInScope(RedisCommand.BRPOP, keys, () -> connection.bRPop(timeout, keys));
  }

    @Override
  public byte[] rPopLPush(byte[] srcKey, byte[] dstKey) {
    return actionWrapper
        .doInScope(RedisCommand.RPOPLPUSH, srcKey, () -> connection.rPopLPush(srcKey, dstKey));
  }

    @Override
  public byte[] bRPopLPush(int timeout, byte[] srcKey, byte[] dstKey) {
    return actionWrapper.doInScope(RedisCommand.BRPOPLPUSH, srcKey,
        () -> connection.bRPopLPush(timeout, srcKey, dstKey));
  }

    @Override
  public Long sAdd(byte[] key, byte[]... values) {
    return actionWrapper.doInScope(RedisCommand.SADD, key, () -> connection.sAdd(key, values));
  }

    @Override
  public Long sRem(byte[] key, byte[]... values) {
    return actionWrapper.doInScope(RedisCommand.SREM, key, () -> connection.sRem(key, values));
  }

    @Override
  public byte[] sPop(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.SPOP, key, () -> connection.sPop(key));
  }

    @Override
  public List<byte[]> sPop(byte[] key, long count) {
    return actionWrapper.doInScope(RedisCommand.SPOP, key, () -> connection.sPop(key, count));
  }

    @Override
  public Boolean sMove(byte[] srcKey, byte[] destKey, byte[] value) {
    return actionWrapper
        .doInScope(RedisCommand.SMOVE, srcKey, () -> connection.sMove(srcKey, destKey, value));
  }

    @Override
  public Long sCard(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.SCARD, key, () -> connection.sCard(key));
  }

    @Override
  public Boolean sIsMember(byte[] key, byte[] value) {
    return actionWrapper.doInScope(RedisCommand.SISMEMBER, key, () -> connection.sIsMember(key, value));
  }

    @Override
  public Set<byte[]> sInter(byte[]... keys) {
    return actionWrapper.doInScope(RedisCommand.SINTER, keys, () -> connection.sInter(keys));
  }

    @Override
  public Long sInterStore(byte[] destKey, byte[]... keys) {
    return actionWrapper
        .doInScope(RedisCommand.SINTERSTORE, keys, () -> connection.sInterStore(destKey, keys));
  }

    @Override
  public Set<byte[]> sUnion(byte[]... keys) {
    return actionWrapper.doInScope(RedisCommand.SUNION, keys, () -> connection.sUnion(keys));
  }

    @Override
  public Long sUnionStore(byte[] destKey, byte[]... keys) {
    return actionWrapper
        .doInScope(RedisCommand.SUNIONSTORE, keys, () -> connection.sUnionStore(destKey, keys));
  }

    @Override
  public Set<byte[]> sDiff(byte[]... keys) {
    return actionWrapper.doInScope(RedisCommand.SDIFF, keys, () -> connection.sDiff(keys));
  }

    @Override
  public Long sDiffStore(byte[] destKey, byte[]... keys) {
    return actionWrapper
        .doInScope(RedisCommand.SDIFFSTORE, keys, () -> connection.sDiffStore(destKey, keys));
  }

    @Override
  public Set<byte[]> sMembers(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.SMEMBERS, key, () -> connection.sMembers(key));
  }

    @Override
  public byte[] sRandMember(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.SRANDMEMBER, key, () -> connection.sRandMember(key));
  }

    @Override
  public List<byte[]> sRandMember(byte[] key, long count) {
    return actionWrapper
        .doInScope(RedisCommand.SRANDMEMBER, key, () -> connection.sRandMember(key, count));
  }

    @Override
  public Cursor<byte[]> sScan(byte[] key, ScanOptions options) {
    return actionWrapper.doInScope(RedisCommand.SSCAN, key, () -> connection.sScan(key, options));
  }

    @Override
  public Boolean zAdd(byte[] key, double score, byte[] value) {
    return actionWrapper.doInScope(RedisCommand.ZADD, key, () -> connection.zAdd(key, score, value));
  }

    @Override
  public Long zAdd(byte[] key, Set<Tuple> tuples) {
    return actionWrapper.doInScope(RedisCommand.ZADD, key, () -> connection.zAdd(key, tuples));
  }

    @Override
  public Long zRem(byte[] key, byte[]... values) {
    return actionWrapper.doInScope(RedisCommand.ZREM, key, () -> connection.zRem(key, values));
  }

    @Override
  public Double zIncrBy(byte[] key, double increment, byte[] value) {
    return actionWrapper
        .doInScope(RedisCommand.ZINCRBY, key, () -> connection.zIncrBy(key, increment, value));
  }

    @Override
  public Long zRank(byte[] key, byte[] value) {
    return actionWrapper.doInScope(RedisCommand.ZRANK, key, () -> connection.zRank(key, value));
  }

    @Override
  public Long zRevRank(byte[] key, byte[] value) {
    return actionWrapper.doInScope(RedisCommand.ZREVRANK, key, () -> connection.zRevRank(key, value));
  }

    @Override
  public Set<byte[]> zRange(byte[] key, long start, long end) {
    return actionWrapper.doInScope(RedisCommand.ZRANGE, key, () -> connection.zRange(key, start, end));
  }

    @Override
  public Set<byte[]> zRevRange(byte[] key, long start, long end) {
    return actionWrapper
        .doInScope(RedisCommand.ZREVRANGE, key, () -> connection.zRevRange(key, start, end));
  }

    @Override
  public Set<Tuple> zRevRangeWithScores(byte[] key, long start, long end) {
    return actionWrapper.doInScope(RedisCommand.ZREVRANGE_WITHSCORES,
        key, () -> connection.zRevRangeWithScores(key, start, end));
  }

    @Override
  public Set<Tuple> zRangeWithScores(byte[] key, long start, long end) {
    return actionWrapper.doInScope(RedisCommand.ZRANGE_WITHSCORES,
        key, () -> connection.zRangeWithScores(key, start, end));
  }

    @Override
  public Set<byte[]> zRangeByScore(byte[] key, double min, double max) {
    return actionWrapper
        .doInScope(RedisCommand.ZRANGEBYSCORE, key, () -> connection.zRangeByScore(key, min, max));
  }

    @Override
  public Set<byte[]> zRangeByScore(byte[] key, double min, double max, long offset, long count) {
    return actionWrapper.doInScope(RedisCommand.ZRANGEBYSCORE,
        key, () -> connection.zRangeByScore(key, min, max, offset, count));
  }

    @Override
  public Set<byte[]> zRangeByScore(byte[] key, String min, String max) {
    return actionWrapper
        .doInScope(RedisCommand.ZRANGEBYSCORE, key, () -> connection.zRangeByScore(key, min, max));
  }

    @Override
  public Set<byte[]> zRangeByScore(byte[] key, Range range) {
    return actionWrapper
        .doInScope(RedisCommand.ZRANGEBYSCORE, key, () -> connection.zRangeByScore(key, range));
  }

    @Override
  public Set<byte[]> zRangeByScore(byte[] key, String min, String max, long offset, long count) {
    return actionWrapper.doInScope(RedisCommand.ZRANGEBYSCORE,
        key, () -> connection.zRangeByScore(key, min, max, offset, count));
  }

    @Override
  public Set<byte[]> zRangeByScore(byte[] key, Range range, Limit limit) {
    return actionWrapper.doInScope(RedisCommand.ZRANGEBYSCORE, key,
        () -> connection.zRangeByScore(key, range, limit));
  }

    @Override
  public Set<Tuple> zRangeByScoreWithScores(byte[] key, Range range) {
    return actionWrapper.doInScope(RedisCommand.ZRANGEBYSCORE_WITHSCORES,
        key, () -> connection.zRangeByScoreWithScores(key, range));
  }

    @Override
  public Set<Tuple> zRangeByScoreWithScores(byte[] key, double min, double max) {
    return actionWrapper.doInScope(RedisCommand.ZRANGEBYSCORE_WITHSCORES,
        key, () -> connection.zRangeByScoreWithScores(key, min, max));
  }

    @Override
  public Set<Tuple> zRangeByScoreWithScores(byte[] key, double min, double max, long offset,
      long count) {
    return actionWrapper.doInScope(RedisCommand.ZRANGEBYSCORE_WITHSCORES,
        key, () -> connection.zRangeByScoreWithScores(key, min, max, offset, count));
  }

    @Override
  public Set<Tuple> zRangeByScoreWithScores(byte[] key, Range range, Limit limit) {
    return actionWrapper.doInScope(RedisCommand.ZRANGEBYSCORE_WITHSCORES,
        key, () -> connection.zRangeByScoreWithScores(key, range, limit));
  }

    @Override
  public Set<byte[]> zRevRangeByScore(byte[] key, double min, double max) {
    return actionWrapper.doInScope(RedisCommand.ZREVRANGEBYSCORE,
        key, () -> connection.zRevRangeByScore(key, min, max));
  }

    @Override
  public Set<byte[]> zRevRangeByScore(byte[] key, Range range) {
    return actionWrapper.doInScope(RedisCommand.ZREVRANGEBYSCORE, key,
        () -> connection.zRevRangeByScore(key, range));
  }

    @Override
  public Set<byte[]> zRevRangeByScore(byte[] key, double min, double max, long offset, long count) {
    return actionWrapper.doInScope(RedisCommand.ZREVRANGEBYSCORE,
        key, () -> connection.zRevRangeByScore(key, min, max, offset, count));
  }

    @Override
  public Set<byte[]> zRevRangeByScore(byte[] key, Range range, Limit limit) {
    return actionWrapper.doInScope(RedisCommand.ZREVRANGEBYSCORE,
        key, () -> connection.zRevRangeByScore(key, range, limit));
  }

    @Override
  public Set<Tuple> zRevRangeByScoreWithScores(byte[] key, double min, double max) {
    return actionWrapper.doInScope(RedisCommand.ZREVRANGEBYSCORE_WITHSCORES,
        key, () -> connection.zRevRangeByScoreWithScores(key, min, max));
  }

    @Override
  public Set<Tuple> zRevRangeByScoreWithScores(byte[] key, double min, double max, long offset,
      long count) {
    return actionWrapper.doInScope(RedisCommand.ZREVRANGEBYSCORE_WITHSCORES,
        key, () -> connection.zRevRangeByScoreWithScores(key, min, max, offset, count));
  }

    @Override
  public Set<Tuple> zRevRangeByScoreWithScores(byte[] key, Range range) {
    return actionWrapper.doInScope(RedisCommand.ZREVRANGEBYSCORE_WITHSCORES,
        key, () -> connection.zRevRangeByScoreWithScores(key, range));
  }

    @Override
  public Set<Tuple> zRevRangeByScoreWithScores(byte[] key, Range range, Limit limit) {
    return actionWrapper.doInScope(RedisCommand.ZREVRANGEBYSCORE_WITHSCORES,
        key, () -> connection.zRevRangeByScoreWithScores(key, range, limit));
  }

    @Override
  public Long zCount(byte[] key, double min, double max) {
    return actionWrapper.doInScope(RedisCommand.ZCOUNT, key, () -> connection.zCount(key, min, max));
  }

    @Override
  public Long zCount(byte[] key, Range range) {
    return actionWrapper.doInScope(RedisCommand.ZCOUNT, key, () -> connection.zCount(key, range));
  }

    @Override
    public Long zLexCount(byte[] key, Range range) {
        return actionWrapper.doInScope(RedisCommand.ZLEXCOUNT, key, () -> connection.zLexCount(key, range));
    }

    @Override
  public Long zCard(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.ZCARD, key, () -> connection.zCard(key));
  }

    @Override
  public Double zScore(byte[] key, byte[] value) {
    return actionWrapper.doInScope(RedisCommand.ZSCORE, key, () -> connection.zScore(key, value));
  }

    @Override
  public Long zRemRange(byte[] key, long start, long end) {
    return actionWrapper
        .doInScope(RedisCommand.ZREMRANGE, key, () -> connection.zRemRange(key, start, end));
  }

    @Override
  public Long zRemRangeByScore(byte[] key, double min, double max) {
    return actionWrapper.doInScope(RedisCommand.ZREMRANGEBYSCORE,
        key, () -> connection.zRemRangeByScore(key, min, max));
  }

    @Override
  public Long zRemRangeByScore(byte[] key, Range range) {
    return actionWrapper.doInScope(RedisCommand.ZREMRANGEBYSCORE, key,
        () -> connection.zRemRangeByScore(key, range));
  }

    @Override
  public Long zUnionStore(byte[] destKey, byte[]... sets) {
    return actionWrapper.doInScope(RedisCommand.ZUNIONSTORE, () -> connection.zUnionStore(destKey, sets));
  }

    @Override
  public Long zUnionStore(byte[] destKey, Aggregate aggregate, int[] weights, byte[]... sets) {
    return actionWrapper.doInScope(RedisCommand.ZUNIONSTORE,
        destKey, () -> connection.zUnionStore(destKey, aggregate, weights, sets));
  }

    @Override
  public Long zUnionStore(byte[] destKey, Aggregate aggregate, Weights weights, byte[]... sets) {
    return actionWrapper.doInScope(RedisCommand.ZUNIONSTORE,
        destKey, () -> connection.zUnionStore(destKey, aggregate, weights, sets));
  }

    @Override
  public Long zInterStore(byte[] destKey, byte[]... sets) {
    return actionWrapper
        .doInScope(RedisCommand.ZINTERSTORE, destKey, () -> connection.zInterStore(destKey, sets));
  }

    @Override
  public Long zInterStore(byte[] destKey, Aggregate aggregate, int[] weights, byte[]... sets) {
    return actionWrapper.doInScope(RedisCommand.ZINTERSTORE,
        destKey, () -> connection.zInterStore(destKey, aggregate, weights, sets));
  }

    @Override
  public Long zInterStore(byte[] destKey, Aggregate aggregate, Weights weights, byte[]... sets) {
    return actionWrapper.doInScope(RedisCommand.ZINTERSTORE,
        destKey, () -> connection.zInterStore(destKey, aggregate, weights, sets));
  }

    @Override
  public Cursor<Tuple> zScan(byte[] key, ScanOptions options) {
    return actionWrapper.doInScope(RedisCommand.ZSCAN, key, () -> connection.zScan(key, options));
  }

    @Override
  public Set<byte[]> zRangeByLex(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.ZRANGEBYLEX, key, () -> connection.zRangeByLex(key));
  }

    @Override
  public Set<byte[]> zRangeByLex(byte[] key, Range range) {
    return actionWrapper
        .doInScope(RedisCommand.ZRANGEBYLEX, key, () -> connection.zRangeByLex(key, range));
  }

    @Override
  public Set<byte[]> zRangeByLex(byte[] key, Range range, Limit limit) {
    return actionWrapper
        .doInScope(RedisCommand.ZRANGEBYLEX, key, () -> connection.zRangeByLex(key, range, limit));
  }

    @Override
    public Set<byte[]> zRevRangeByLex(byte[] key, Range range, Limit limit) {
        return actionWrapper
                .doInScope(RedisCommand.ZREVRANGEBYLEX, key, () -> connection.zRevRangeByLex(key, range, limit));
    }

    @Override
  public Boolean hSet(byte[] key, byte[] field, byte[] value) {
    return actionWrapper.doInScope(RedisCommand.HSET, key, () -> connection.hSet(key, field, value));
  }

    @Override
  public Boolean hSetNX(byte[] key, byte[] field, byte[] value) {
    return actionWrapper.doInScope(RedisCommand.HSETNX, key, () -> connection.hSetNX(key, field, value));
  }

    @Override
  public byte[] hGet(byte[] key, byte[] field) {
    return actionWrapper.doInScope(RedisCommand.HGET, key, () -> connection.hGet(key, field));
  }

    @Override
  public List<byte[]> hMGet(byte[] key, byte[]... fields) {
    return actionWrapper.doInScope(RedisCommand.HMGET, key, () -> connection.hMGet(key, fields));
  }

    @Override
  public void hMSet(byte[] key, Map<byte[], byte[]> hashes) {
    actionWrapper.doInScope(RedisCommand.HMSET, key, () -> connection.hMSet(key, hashes));
  }

    @Override
  public Long hIncrBy(byte[] key, byte[] field, long delta) {
    return actionWrapper.doInScope(RedisCommand.HINCRBY, key, () -> connection.hIncrBy(key, field, delta));
  }

    @Override
  public Double hIncrBy(byte[] key, byte[] field, double delta) {
    return actionWrapper.doInScope(RedisCommand.HINCRBY, key, () -> connection.hIncrBy(key, field, delta));
  }

    @Override
  public Boolean hExists(byte[] key, byte[] field) {
    return actionWrapper.doInScope(RedisCommand.HEXISTS, key, () -> connection.hExists(key, field));
  }

    @Override
  public Long hDel(byte[] key, byte[]... fields) {
    return actionWrapper.doInScope(RedisCommand.HDEL, key, () -> connection.hDel(key, fields));
  }

    @Override
  public Long hLen(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.HLEN, key, () -> connection.hLen(key));
  }

    @Override
  public Set<byte[]> hKeys(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.HKEYS, key, () -> connection.hKeys(key));
  }

    @Override
  public List<byte[]> hVals(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.HVALS, key, () -> connection.hVals(key));
  }

    @Override
  public Map<byte[], byte[]> hGetAll(byte[] key) {
    return actionWrapper.doInScope(RedisCommand.HGETALL, key, () -> connection.hGetAll(key));
  }

    @Override
  public Cursor<Entry<byte[], byte[]>> hScan(byte[] key, ScanOptions options) {
    return actionWrapper.doInScope(RedisCommand.HSCAN, key, () -> connection.hScan(key, options));
  }

    @Override
  public Long hStrLen(byte[] key, byte[] field) {
    return actionWrapper.doInScope(RedisCommand.HSTRLEN, key, () -> connection.hStrLen(key, field));
  }

    @Override
  public void multi() {
    actionWrapper.doInScope(RedisCommand.MULTI, () -> connection.multi());
  }

    @Override
  public List<Object> exec() {
    return actionWrapper.doInScope(RedisCommand.EXEC, () -> connection.exec());
  }

    @Override
  public void discard() {
    actionWrapper.doInScope(RedisCommand.DISCARD, () -> connection.discard());
  }

    @Override
  public void watch(byte[]... keys) {
    actionWrapper.doInScope(RedisCommand.WATCH, () -> connection.watch(keys));
  }

    @Override
  public void unwatch() {
    actionWrapper.doInScope(RedisCommand.UNWATCH, () -> connection.unwatch());
  }

    @Override
  public Long publish(byte[] channel, byte[] message) {
    return actionWrapper.doInScope(RedisCommand.PUBLISH, () -> connection.publish(channel, message));
  }

    @Override
  public void subscribe(MessageListener listener, byte[]... channels) {
    actionWrapper.doInScope(RedisCommand.SUBSCRIBE, () -> connection.subscribe(listener, channels));
  }

    @Override
  public void pSubscribe(MessageListener listener, byte[]... patterns) {
    actionWrapper.doInScope(RedisCommand.PSUBSCRIBE, () -> connection.pSubscribe(listener, patterns));
  }

    @Override
  public void select(int dbIndex) {
    actionWrapper.doInScope(RedisCommand.SELECT, () -> connection.select(dbIndex));
  }

    @Override
  public byte[] echo(byte[] message) {
    return actionWrapper.doInScope(RedisCommand.ECHO, () -> connection.echo(message));
  }

    @Override
  public String ping() {
    return actionWrapper.doInScope(RedisCommand.PING, () -> connection.ping());
  }

    @Override
  public void bgWriteAof() {
    actionWrapper.doInScope(RedisCommand.BGWRITEAOF, () -> connection.bgWriteAof());
  }

    @Override
  public void bgReWriteAof() {
    actionWrapper.doInScope(RedisCommand.BGREWRITEAOF, () -> connection.bgReWriteAof());
  }

    @Override
  public void bgSave() {
    actionWrapper.doInScope(RedisCommand.BGSAVE, () -> connection.bgSave());
  }

    @Override
  public Long lastSave() {
    return actionWrapper.doInScope(RedisCommand.LASTSAVE, () -> connection.lastSave());
  }

    @Override
  public void save() {
    actionWrapper.doInScope(RedisCommand.SAVE, () -> connection.save());
  }

    @Override
  public Long dbSize() {
    return actionWrapper.doInScope(RedisCommand.DBSIZE, () -> connection.dbSize());
  }

    @Override
  public void flushDb() {
    actionWrapper.doInScope(RedisCommand.FLUSHDB, () -> connection.flushDb());
  }

    @Override
  public void flushAll() {
    actionWrapper.doInScope(RedisCommand.FLUSHALL, () -> connection.flushAll());
  }

    @Override
  public Properties info() {
    return actionWrapper.doInScope(RedisCommand.INFO, () -> connection.info());
  }

    @Override
  public Properties info(String section) {
    return actionWrapper.doInScope(RedisCommand.INFO, () -> connection.info(section));
  }

    @Override
  public void shutdown() {
    actionWrapper.doInScope(RedisCommand.SHUTDOWN, () -> connection.shutdown());
  }

    @Override
  public void shutdown(ShutdownOption option) {
    actionWrapper.doInScope(RedisCommand.SHUTDOWN, () -> connection.shutdown(option));
  }

    @Override
  public Properties getConfig(String pattern) {
    return actionWrapper.doInScope(RedisCommand.CONFIG_GET, () -> connection.getConfig(pattern));
  }

    @Override
  public void setConfig(String param, String value) {
    actionWrapper.doInScope(RedisCommand.CONFIG_SET, () -> connection.setConfig(param, value));
  }

    @Override
  public void resetConfigStats() {
    actionWrapper.doInScope(RedisCommand.CONFIG_RESETSTAT, () -> connection.resetConfigStats());
  }

    @Override
  public Long time() {
    return actionWrapper.doInScope(RedisCommand.TIME, () -> connection.time());
  }

    @Override
  public void killClient(String host, int port) {
    actionWrapper.doInScope(RedisCommand.CLIENT_KILL, () -> connection.killClient(host, port));
  }

    @Override
  public void setClientName(byte[] name) {
    actionWrapper.doInScope(RedisCommand.CLIENT_SETNAME, () -> connection.setClientName(name));
  }

    @Override
  public String getClientName() {
    return actionWrapper.doInScope(RedisCommand.CLIENT_GETNAME, () -> connection.getClientName());
  }

    @Override
  public List<RedisClientInfo> getClientList() {
    return actionWrapper.doInScope(RedisCommand.CLIENT_LIST, () -> connection.getClientList());
  }

    @Override
  public void slaveOf(String host, int port) {
    actionWrapper.doInScope(RedisCommand.SLAVEOF, () -> connection.slaveOf(host, port));
  }

    @Override
  public void slaveOfNoOne() {
    actionWrapper.doInScope(RedisCommand.SLAVEOFNOONE, () -> connection.slaveOfNoOne());
  }

    @Override
  public void migrate(byte[] key, RedisNode target, int dbIndex, MigrateOption option) {
    actionWrapper.doInScope(RedisCommand.MIGRATE, key,
        () -> connection.migrate(key, target, dbIndex, option));
  }

    @Override
  public void migrate(byte[] key, RedisNode target, int dbIndex, MigrateOption option,
      long timeout) {
    actionWrapper.doInScope(RedisCommand.MIGRATE,
        key, () -> connection.migrate(key, target, dbIndex, option, timeout));
  }

    @Override
  public void scriptFlush() {
    actionWrapper.doInScope(RedisCommand.SCRIPT_FLUSH, () -> connection.scriptFlush());
  }

    @Override
  public void scriptKill() {
    actionWrapper.doInScope(RedisCommand.SCRIPT_KILL, () -> connection.scriptKill());
  }

    @Override
  public String scriptLoad(byte[] script) {
    return actionWrapper.doInScope(RedisCommand.SCRIPT_LOAD, () -> connection.scriptLoad(script));
  }

    @Override
  public List<Boolean> scriptExists(String... scriptShas) {
    return actionWrapper.doInScope(RedisCommand.SCRIPT_EXISTS, () -> connection.scriptExists(scriptShas));
  }

    @Override
  public <T> T eval(byte[] script, ReturnType returnType, int numKeys, byte[]... keysAndArgs) {
    return actionWrapper.doInScope(RedisCommand.EVAL,
        () -> connection.eval(script, returnType, numKeys, keysAndArgs));
  }

    @Override
  public <T> T evalSha(String scriptSha, ReturnType returnType, int numKeys,
      byte[]... keysAndArgs) {
    return actionWrapper.doInScope(RedisCommand.EVALSHA,
        () -> connection.evalSha(scriptSha, returnType, numKeys, keysAndArgs));
  }

    @Override
  public <T> T evalSha(byte[] scriptSha, ReturnType returnType, int numKeys,
      byte[]... keysAndArgs) {
    return actionWrapper.doInScope(RedisCommand.EVALSHA,
        () -> connection.evalSha(scriptSha, returnType, numKeys, keysAndArgs));
  }

    @Override
  public Long geoAdd(byte[] key, Point point, byte[] member) {
    return actionWrapper.doInScope(RedisCommand.GEOADD, key, () -> connection.geoAdd(key, point, member));
  }

    @Override
  public Long geoAdd(byte[] key, GeoLocation<byte[]> location) {
    return actionWrapper.doInScope(RedisCommand.GEOADD, key, () -> connection.geoAdd(key, location));
  }

    @Override
  public Long geoAdd(byte[] key, Map<byte[], Point> memberCoordinateMap) {
    return actionWrapper
        .doInScope(RedisCommand.GEOADD, key, () -> connection.geoAdd(key, memberCoordinateMap));
  }

    @Override
  public Long geoAdd(byte[] key, Iterable<GeoLocation<byte[]>> locations) {
    return actionWrapper.doInScope(RedisCommand.GEOADD, key, () -> connection.geoAdd(key, locations));
  }

    @Override
  public Distance geoDist(byte[] key, byte[] member1, byte[] member2) {
    return actionWrapper
        .doInScope(RedisCommand.GEODIST, key, () -> connection.geoDist(key, member1, member2));
  }

    @Override
  public Distance geoDist(byte[] key, byte[] member1, byte[] member2, Metric metric) {
    return actionWrapper.doInScope(RedisCommand.GEODIST, key,
        () -> connection.geoDist(key, member1, member2, metric));
  }

    @Override
  public List<String> geoHash(byte[] key, byte[]... members) {
    return actionWrapper.doInScope(RedisCommand.GEOHASH, key, () -> connection.geoHash(key, members));
  }

    @Override
  public List<Point> geoPos(byte[] key, byte[]... members) {
    return actionWrapper.doInScope(RedisCommand.GEOPOS, key, () -> connection.geoPos(key, members));
  }

    @Override
  public GeoResults<GeoLocation<byte[]>> geoRadius(byte[] key, Circle within) {
    return actionWrapper.doInScope(RedisCommand.GEORADIUS, key, () -> connection.geoRadius(key, within));
  }

    @Override
  public GeoResults<GeoLocation<byte[]>> geoRadius(byte[] key, Circle within,
      GeoRadiusCommandArgs args) {
    return actionWrapper
        .doInScope(RedisCommand.GEORADIUS, key, () -> connection.geoRadius(key, within, args));
  }

    @Override
  public GeoResults<GeoLocation<byte[]>> geoRadiusByMember(byte[] key, byte[] member,
      double radius) {
    return actionWrapper.doInScope(RedisCommand.GEORADIUSBYMEMBER,
        key, () -> connection.geoRadiusByMember(key, member, radius));
  }

    @Override
  public GeoResults<GeoLocation<byte[]>> geoRadiusByMember(byte[] key, byte[] member,
      Distance radius) {
    return actionWrapper.doInScope(RedisCommand.GEORADIUSBYMEMBER,
        key, () -> connection.geoRadiusByMember(key, member, radius));
  }

    @Override
  public GeoResults<GeoLocation<byte[]>> geoRadiusByMember(byte[] key, byte[] member,
      Distance radius, GeoRadiusCommandArgs args) {
    return actionWrapper.doInScope(RedisCommand.GEORADIUSBYMEMBER,
        key, () -> connection.geoRadiusByMember(key, member, radius, args));
  }

    @Override
  public Long geoRemove(byte[] key, byte[]... members) {
    return actionWrapper.doInScope(RedisCommand.GEOREMOVE, key, () -> connection.geoRemove(key, members));
  }

    @Override
  public Long pfAdd(byte[] key, byte[]... values) {
    return actionWrapper.doInScope(RedisCommand.PFADD, key, () -> connection.pfAdd(key, values));
  }

    @Override
  public Long pfCount(byte[]... keys) {
    return actionWrapper.doInScope(RedisCommand.PFCOUNT, keys, () -> connection.pfCount(keys));
  }

    @Override
  public void pfMerge(byte[] destinationKey, byte[]... sourceKeys) {
    actionWrapper.doInScope(RedisCommand.PFMERGE,
        () -> connection.pfMerge(destinationKey, sourceKeys));
  }

    @Override
    public Long xAck(byte[] key, String group, RecordId... recordIds) {
        return actionWrapper.doInScope(RedisCommand.XACK, key, () -> connection.xAck(key, group, recordIds));
    }

    @Override
    public RecordId xAdd(MapRecord<byte[], byte[], byte[]> record, XAddOptions options) {
        return actionWrapper.doInScope(RedisCommand.XADD, () -> connection.xAdd(record, options));
    }

    @Override
    public List<RecordId> xClaimJustId(byte[] key, String group, String newOwner, XClaimOptions options) {
        return actionWrapper.doInScope(RedisCommand.XCLAIMJUSTID, key,
                () -> connection.xClaimJustId(key, group, newOwner, options));
    }

    @Override
    public List<ByteRecord> xClaim(byte[] key, String group, String newOwner, XClaimOptions options) {
        return actionWrapper.doInScope(RedisCommand.XCLAIM, key,
                () -> connection.xClaim(key, group, newOwner, options));
    }

    @Override
    public Long xDel(byte[] key, RecordId... recordIds) {
        return actionWrapper.doInScope(RedisCommand.XDEL, key, () -> connection.xDel(key, recordIds));
    }

    @Override
    public String xGroupCreate(byte[] key, String groupName, ReadOffset readOffset) {
        return actionWrapper.doInScope(RedisCommand.XGROUPCREATE, key,
                () -> connection.xGroupCreate(key, groupName, readOffset));
    }

    @Override
    public String xGroupCreate(byte[] key, String groupName, ReadOffset readOffset, boolean mkStream) {
        return actionWrapper.doInScope(RedisCommand.XGROUPCREATE, key,
                () -> connection.xGroupCreate(key, groupName, readOffset, mkStream));
    }

    @Override
    public Boolean xGroupDelConsumer(byte[] key, Consumer consumer) {
        return actionWrapper.doInScope(RedisCommand.XGROUPDELCONSUMER, key,
                () -> connection.xGroupDelConsumer(key, consumer));
    }

    @Override
    public Boolean xGroupDestroy(byte[] key, String groupName) {
        return actionWrapper.doInScope(RedisCommand.XGROUPDESTROY, key,
                () -> connection.xGroupDestroy(key, groupName));
    }

    @Override
    public StreamInfo.XInfoStream xInfo(byte[] key) {
        return actionWrapper.doInScope(RedisCommand.XINFO, key,
                () -> connection.xInfo(key));
    }

    @Override
    public StreamInfo.XInfoGroups xInfoGroups(byte[] key) {
        return actionWrapper.doInScope(RedisCommand.XINFOGROUPS, key,
                () -> connection.xInfoGroups(key));
    }

    @Override
    public StreamInfo.XInfoConsumers xInfoConsumers(byte[] key, String groupName) {
        return actionWrapper.doInScope(RedisCommand.XINFOCONSUMERS, key,
                () -> connection.xInfoConsumers(key, groupName));
    }

    @Override
    public Long xLen(byte[] key) {
        return actionWrapper.doInScope(RedisCommand.XLEN, key, () -> connection.xLen(key));
    }

    @Override
    public PendingMessagesSummary xPending(byte[] key, String groupName) {
        return actionWrapper.doInScope(RedisCommand.XPENDING, key, () -> connection.xPending(key, groupName));
    }

    @Override
    public PendingMessages xPending(byte[] key, String groupName, XPendingOptions options) {
        return actionWrapper.doInScope(RedisCommand.XPENDING, key,
                () -> connection.xPending(key, groupName, options));
    }

    @Override
    public List<ByteRecord> xRange(byte[] key, org.springframework.data.domain.Range<String> range, Limit limit) {
        return actionWrapper.doInScope(RedisCommand.XRANGE, key,
                () -> connection.xRange(key, range, limit));
    }

    @Override
    public List<ByteRecord> xRead(StreamReadOptions readOptions, StreamOffset<byte[]>... streams) {
        return actionWrapper.doInScope(RedisCommand.XREAD, () -> connection.xRead(readOptions, streams));
    }

    @Override
    public List<ByteRecord> xReadGroup(Consumer consumer, StreamReadOptions readOptions, StreamOffset<byte[]>... streams) {
        return actionWrapper.doInScope(RedisCommand.XREADGROUP, () -> connection.xReadGroup(consumer, readOptions, streams));
    }

    @Override
    public List<ByteRecord> xRevRange(byte[] key, org.springframework.data.domain.Range<String> range, Limit limit) {
        return actionWrapper.doInScope(RedisCommand.XREVRANGE, key, () -> connection.xRevRange(key, range, limit));
    }

    @Override
    public Long xTrim(byte[] key, long count) {
        return actionWrapper.doInScope(RedisCommand.XTRIM, key, () -> connection.xTrim(key, count));
    }

    @Override
    public Long xTrim(byte[] key, long count, boolean approximateTrimming) {
        return actionWrapper.doInScope(RedisCommand.XTRIM, key, () -> connection.xTrim(key, count, approximateTrimming));
    }
}
