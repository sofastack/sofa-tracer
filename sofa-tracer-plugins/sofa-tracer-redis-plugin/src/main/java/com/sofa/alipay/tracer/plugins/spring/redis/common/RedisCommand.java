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
package com.sofa.alipay.tracer.plugins.spring.redis.common;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/11/18 8:54 PM
 * @since:
 **/
public final class RedisCommand {

    public static final String APPEND                        = "APPEND";
    public static final String AUTH                          = "AUTH";
    public static final String BGREWRITEAOF                  = "BGREWRITEAOF";
    public static final String BGSAVE                        = "BGSAVE";
    public static final String BGWRITEAOF                    = "BGWRITEAOF";
    public static final String BITCOUNT                      = "BITCOUNT";
    public static final String BITFIELD                      = "BITFIELD";
    public static final String BITOP                         = "BITOP";
    public static final String BITPOS                        = "BITPOS";
    public static final String BLPOP                         = "BLPOP";
    public static final String BRPOP                         = "BRPOP";
    public static final String BRPOPLPUSH                    = "BRPOPLPUSH";
    public static final String BZPOPMIN                      = "BZPOPMIN";
    public static final String BZPOPMAX                      = "BZPOPMAX";
    public static final String CLIENT_KILL                   = "CLIENT KILL";
    public static final String CLIENT_LIST                   = "CLIENT LIST";
    public static final String CLIENT_GETNAME                = "CLIENT GETNAME";
    public static final String CLIENT_PAUSE                  = "CLIENT PAUSE";
    public static final String CLIENT_REPLY                  = "CLIENT REPLY";
    public static final String CLIENT_SETNAME                = "CLIENT SETNAME";
    public static final String CLUSTER_ADDSLOTS              = "CLUSTER ADDSLOTS";
    public static final String CLUSTER_COUNT_FAILURE_REPORTS = "CLUSTER COUNT-FAILURE-REPORTS";
    public static final String CLUSTER_COUNTKEYSINSLOT       = "CLUSTER COUNTKEYSINSLOT";
    public static final String CLUSTER_DELSLOTS              = "CLUSTER DELSLOTS";
    public static final String CLUSTER_FAILOVER              = "CLUSTER FAILOVER";
    public static final String CLUSTER_FORGET                = "CLUSTER FORGET";
    public static final String CLUSTER_GETKEYSINSLOT         = "CLUSTER GETKEYSINSLOT";
    public static final String CLUSTER_INFO                  = "CLUSTER INFO";
    public static final String CLUSTER_KEYSLOT               = "CLUSTER KEYSLOT";
    public static final String CLUSTER_MASTER_SLAVE_MAP      = "CLUSTER MASTER-SLAVE-MAP";
    public static final String CLUSTER_MEET                  = "CLUSTER MEET";
    public static final String CLUSTER_NODES                 = "CLUSTER NODES";
    public static final String CLUSTER_NODE_FOR_KEY          = "CLUSTER_NODE_FOR_KEY";
    public static final String CLUSTER_NODE_FOR_SLOT         = "CLUSTER_NODE_FOR_SLOT";
    public static final String CLUSTER_REPLICATE             = "CLUSTER REPLICATE";
    public static final String CLUSTER_RESET                 = "CLUSTER RESET";
    public static final String CLUSTER_SAVECONFIG            = "CLUSTER SAVECONFIG";
    public static final String CLUSTER_SET_CONFIG_EPOCH      = "CLUSTER SET-CONFIG-EPOCH";
    public static final String CLUSTER_SETSLOT               = "CLUSTER SETSLOT";
    public static final String CLUSTER_SLAVES                = "CLUSTER SLAVES";
    public static final String CLUSTER_REPLICAS              = "CLUSTER REPLICAS";
    public static final String CLUSTER_SLOTS                 = "CLUSTER SLOTS";
    public static final String COMMAND                       = "COMMAND";
    public static final String COMMAND_COUNT                 = "COMMAND COUNT";
    public static final String COMMAND_GETKEYS               = "COMMAND GETKEYS";
    public static final String COMMAND_INFO                  = "COMMAND INFO";
    public static final String CONFIG_GET                    = "CONFIG GET";
    public static final String CONFIG_REWRITE                = "CONFIG REWRITE";
    public static final String CONFIG_SET                    = "CONFIG SET";
    public static final String CONFIG_RESETSTAT              = "CONFIG RESETSTAT";
    public static final String COPY                          = "COPY";
    public static final String DBSIZE                        = "DBSIZE";
    public static final String DEBUG_OBJECT                  = "DEBUG OBJECT";
    public static final String DEBUG_SEGFAULT                = "DEBUG SEGFAULT";
    public static final String DECR                          = "DECR";
    public static final String DECRBY                        = "DECRBY";
    public static final String DEL                           = "DEL";
    public static final String DISCARD                       = "DISCARD";
    public static final String DUMP                          = "DUMP";
    public static final String ECHO                          = "ECHO";
    public static final String EVAL                          = "EVAL";
    public static final String EVALSHA                       = "EVALSHA";
    public static final String EXEC                          = "EXEC";
    public static final String EXISTS                        = "EXISTS";
    public static final String EXPIRE                        = "EXPIRE";
    public static final String EXPIREAT                      = "EXPIREAT";
    public static final String FLUSHALL                      = "FLUSHALL";
    public static final String FLUSHDB                       = "FLUSHDB";
    public static final String GEOADD                        = "GEOADD";
    public static final String GEOHASH                       = "GEOHASH";
    public static final String GEOPOS                        = "GEOPOS";
    public static final String GEODIST                       = "GEODIST";
    public static final String GEORADIUS                     = "GEORADIUS";
    public static final String GEORADIUSBYMEMBER             = "GEORADIUSBYMEMBER";
    public static final String GEOREMOVE                     = "GEOREMOVE";
    public static final String GET                           = "GET";
    public static final String GETDEL                        = "GETDEL";
    public static final String GETBIT                        = "GETBIT";
    public static final String GETRANGE                      = "GETRANGE";
    public static final String GETSET                        = "GETSET";
    public static final String HDEL                          = "HDEL";
    public static final String HEXISTS                       = "HEXISTS";
    public static final String HGET                          = "HGET";
    public static final String HGETALL                       = "HGETALL";
    public static final String HINCRBY                       = "HINCRBY";
    public static final String HINCRBYFLOAT                  = "HINCRBYFLOAT";
    public static final String HKEYS                         = "HKEYS";
    public static final String HLEN                          = "HLEN";
    public static final String HMGET                         = "HMGET";
    public static final String HMSET                         = "HMSET";
    public static final String HSET                          = "HSET";
    public static final String HSETNX                        = "HSETNX";
    public static final String HSTRLEN                       = "HSTRLEN";
    public static final String HVALS                         = "HVALS";
    public static final String INCR                          = "INCR";
    public static final String INCRBY                        = "INCRBY";
    public static final String INCRBYFLOAT                   = "INCRBYFLOAT";
    public static final String INFO                          = "INFO";
    public static final String KEYS                          = "KEYS";
    public static final String LASTSAVE                      = "LASTSAVE";
    public static final String LINDEX                        = "LINDEX";
    public static final String LINSERT                       = "LINSERT";
    public static final String LLEN                          = "LLEN";
    public static final String LPOP                          = "LPOP";
    public static final String LPUSH                         = "LPUSH";
    public static final String LPUSHX                        = "LPUSHX";
    public static final String LRANGE                        = "LRANGE";
    public static final String LREM                          = "LREM";
    public static final String LSET                          = "LSET";
    public static final String LTRIM                         = "LTRIM";
    public static final String MEMORY_DOCTOR                 = "MEMORY DOCTOR";
    public static final String MEMORY_HELP                   = "MEMORY HELP";
    public static final String MEMORY_MALLOC_STATS           = "MEMORY MALLOC-STATS";
    public static final String MEMORY_PURGE                  = "MEMORY PURGE";
    public static final String MEMORY_STATS                  = "MEMORY STATS";
    public static final String MEMORY_USAGE                  = "MEMORY USAGE";
    public static final String MGET                          = "MGET";
    public static final String MIGRATE                       = "MIGRATE";
    public static final String MONITOR                       = "MONITOR";
    public static final String MOVE                          = "MOVE";
    public static final String MSET                          = "MSET";
    public static final String MSETNX                        = "MSETNX";
    public static final String MULTI                         = "MULTI";
    public static final String OBJECT                        = "OBJECT";
    public static final String PERSIST                       = "PERSIST";
    public static final String PEXPIRE                       = "PEXPIRE";
    public static final String PEXPIREAT                     = "PEXPIREAT";
    public static final String PFADD                         = "PFADD";
    public static final String PFCOUNT                       = "PFCOUNT";
    public static final String PFMERGE                       = "PFMERGE";
    public static final String PING                          = "PING";
    public static final String PSETEX                        = "PSETEX";
    public static final String PSUBSCRIBE                    = "PSUBSCRIBE";
    public static final String PUBSUB                        = "PUBSUB";
    public static final String PTTL                          = "PTTL";
    public static final String PUBLISH                       = "PUBLISH";
    public static final String PUNSUBSCRIBE                  = "PUNSUBSCRIBE";
    public static final String QUIT                          = "QUIT";
    public static final String RANDOMKEY                     = "RANDOMKEY";
    public static final String READONLY                      = "READONLY";
    public static final String READWRITE                     = "READWRITE";
    public static final String RENAME                        = "RENAME";
    public static final String RENAMENX                      = "RENAMENX";
    public static final String RESTORE                       = "RESTORE";
    public static final String ROLE                          = "ROLE";
    public static final String RPOP                          = "RPOP";
    public static final String RPOPLPUSH                     = "RPOPLPUSH";
    public static final String RPUSH                         = "RPUSH";
    public static final String RPUSHX                        = "RPUSHX";
    public static final String SADD                          = "SADD";
    public static final String SAVE                          = "SAVE";
    public static final String SCARD                         = "SCARD";
    public static final String SCRIPT_DEBUG                  = "SCRIPT DEBUG";
    public static final String SCRIPT_EXISTS                 = "SCRIPT EXISTS";
    public static final String SCRIPT_FLUSH                  = "SCRIPT FLUSH";
    public static final String SCRIPT_KILL                   = "SCRIPT KILL";
    public static final String SCRIPT_LOAD                   = "SCRIPT LOAD";
    public static final String SDIFF                         = "SDIFF";
    public static final String SDIFFSTORE                    = "SDIFFSTORE";
    public static final String SELECT                        = "SELECT";
    public static final String SET                           = "SET";
    public static final String SETBIT                        = "SETBIT";
    public static final String SETEX                         = "SETEX";
    public static final String SETNX                         = "SETNX";
    public static final String SETRANGE                      = "SETRANGE";
    public static final String SHUTDOWN                      = "SHUTDOWN";
    public static final String SINTER                        = "SINTER";
    public static final String SINTERSTORE                   = "SINTERSTORE";
    public static final String SISMEMBER                     = "SISMEMBER";
    public static final String SLAVEOF                       = "SLAVEOF";
    public static final String SLAVEOFNOONE                  = "SLAVEOFNOONE";
    public static final String REPLICAOF                     = "REPLICAOF";
    public static final String SLOWLOG                       = "SLOWLOG";
    public static final String SMEMBERS                      = "SMEMBERS";
    public static final String SMOVE                         = "SMOVE";
    public static final String SORT                          = "SORT";
    public static final String SPOP                          = "SPOP";
    public static final String SRANDMEMBER                   = "SRANDMEMBER";
    public static final String SREM                          = "SREM";
    public static final String STRLEN                        = "STRLEN";
    public static final String SUBSCRIBE                     = "SUBSCRIBE";
    public static final String SUNION                        = "SUNION";
    public static final String SUNIONSTORE                   = "SUNIONSTORE";
    public static final String SWAPDB                        = "SWAPDB";
    public static final String SYNC                          = "SYNC";
    public static final String TIME                          = "TIME";
    public static final String TOUCH                         = "TOUCH";
    public static final String TTL                           = "TTL";
    public static final String TYPE                          = "TYPE";
    public static final String UNSUBSCRIBE                   = "UNSUBSCRIBE";
    public static final String UNLINK                        = "UNLINK";
    public static final String UNWATCH                       = "UNWATCH";
    public static final String WAIT                          = "WAIT";
    public static final String WATCH                         = "WATCH";
    public static final String ZADD                          = "ZADD";
    public static final String ZCARD                         = "ZCARD";
    public static final String ZCOUNT                        = "ZCOUNT";
    public static final String ZINCRBY                       = "ZINCRBY";
    public static final String ZINTERSTORE                   = "ZINTERSTORE";
    public static final String ZLEXCOUNT                     = "ZLEXCOUNT";
    public static final String ZPOPMAX                       = "ZPOPMAX";
    public static final String ZPOPMIN                       = "ZPOPMIN";
    public static final String ZRANGE                        = "ZRANGE";
    public static final String ZRANGE_WITHSCORES             = "ZRANGE WITHSCORES";
    public static final String ZRANGEBYLEX                   = "ZRANGEBYLEX";
    public static final String ZREVRANGEBYLEX                = "ZREVRANGEBYLEX";
    public static final String ZRANGEBYSCORE                 = "ZRANGEBYSCORE";
    public static final String ZRANGEBYSCORE_WITHSCORES      = "ZRANGEBYSCORE WITHSCORES";
    public static final String ZRANK                         = "ZRANK";
    public static final String ZREM                          = "ZREM";
    public static final String ZREMRANGE                     = "ZREMRANGE";
    public static final String ZREMRANGEBYLEX                = "ZREMRANGEBYLEX";
    public static final String ZREMRANGEBYRANK               = "ZREMRANGEBYRANK";
    public static final String ZREMRANGEBYSCORE              = "ZREMRANGEBYSCORE";
    public static final String ZREVRANGE                     = "ZREVRANGE";
    public static final String ZREVRANGE_WITHSCORES          = "ZREVRANGE WITHSCORES";
    public static final String ZREVRANGEBYSCORE              = "ZREVRANGEBYSCORE";
    public static final String ZREVRANGEBYSCORE_WITHSCORES   = "ZREVRANGEBYSCORE WITHSCORES";
    public static final String ZREVRANK                      = "ZREVRANK";
    public static final String ZSCORE                        = "ZSCORE";
    public static final String ZUNIONSTORE                   = "ZUNIONSTORE";
    public static final String SCAN                          = "SCAN";
    public static final String SSCAN                         = "SSCAN";
    public static final String HSCAN                         = "HSCAN";
    public static final String ZSCAN                         = "ZSCAN";
    public static final String XADD                          = "XADD";
    public static final String XRANGE                        = "XRANGE";
    public static final String XREVRANGE                     = "XREVRANGE";
    public static final String XLEN                          = "XLEN";
    public static final String XREAD                         = "XREAD";
    public static final String XREADGROUP                    = "XREADGROUP";
    public static final String XPENDING                      = "XPENDING";
    public static final String ENCODING                      = "ENCODING";
    public static final String IDLETIME                      = "IDLETIME";
    public static final String REFCOUNT                      = "REFCOUNT";
    public static final String EXECUTE                       = "EXECUTE";

    private RedisCommand() {
    }

}
