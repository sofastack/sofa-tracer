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
package com.alipay.common.tracer.core.span;

/**
 * some common SpanTags
 * @author luoguimu123
 * @author guolei.sgl
 * @version : v 0.1
 */
public class CommonSpanTags {

    /**
     * LOCAL_APP records the currnt app name
     */
    public static final String LOCAL_APP               = "local.app";

    /**
     * REMOTE_APP records the target app name
     */
    public static final String REMOTE_APP              = "remote.app";

    /**
     * CURRENT_THREAD_NAME records handler result
     */
    public static final String RESULT_CODE             = "result.code";

    /**
     * CURRENT_THREAD_NAME records current thread name
     */
    public static final String CURRENT_THREAD_NAME     = "current.thread.name";

    /**
     * REQUEST_URL records the url of the incoming request.
     */
    public static final String REQUEST_URL             = "request.url";

    /**
     * METHOD records the request method name,(rpc method or http method).
     */
    public static final String METHOD                  = "method";

    /**
     * REQ_SIZE records the request body size.
     */
    public static final String REQ_SIZE                = "req.size.bytes";

    /**
     * RESP_SIZE records the response body size.
     */
    public static final String RESP_SIZE               = "resp.size.bytes";

    /**
     * PROTOCOL records the request protocol type.
     */
    public static final String PROTOCOL                = "protocol";

    /**
     * SERVICE records the rpc service interface.
     */
    public static final String SERVICE                 = "service";

    /**
     * REMOTE_HOST records the rpc target host.
     */
    public static final String REMOTE_HOST             = "remote.host";
    /**
     * REMOTE_PORT records the rpc target port.
     */
    public static final String REMOTE_PORT             = "remote.port";

    /**
     * LOCAL_HOST records the local host.
     */
    public static final String LOCAL_HOST              = "local.host";

    public static final String PEER_HOST               = "peer.host";

    /**
     * LOCAL_PORT records the local port.
     */
    public static final String LOCAL_PORT              = "local.port";

    /**
     * INVOKE_TYPE records the invoke type(oneway/sync/async).
     */
    public static final String INVOKE_TYPE             = "invoke.type";

    /**
     * RPC_TRACE_NAME constants key for dubbo rpc transfer.
     */
    public static final String RPC_TRACE_NAME          = "dubbo.rpc.sofa.tracer";

    /**
     * CLIENT_SERIALIZE_TIME records the rpc client serializes the request body time
     */
    public static final String CLIENT_SERIALIZE_TIME   = "client.serialize.time";

    /**
     * SERVER_SERIALIZE_TIME records the rpc server serializes the response body time
     */
    public static final String SERVER_SERIALIZE_TIME   = "server.serialize.time";

    /**
     * CLIENT_DESERIALIZE_TIME records the rpc client deserialize the response body time
     */
    public static final String CLIENT_DESERIALIZE_TIME = "client.deserialize.time";

    /**
     * SERVER_DESERIALIZE_TIME records the rpc server deserialize the request body time
     */
    public static final String SERVER_DESERIALIZE_TIME = "server.deserialize.time";

    /**
     * SPAN_ID records the current span's id
     */
    public static final String SPAN_ID                 = "spanId";

    /**
     * TRACE_ID records the current span's traceId
     */
    public static final String TRACE_ID                = "traceId";

    /**
     * BAGGAGE records the span's baggage
     */
    public static final String BAGGAGE                 = "baggage";

    public static final String BIZ_BAGGAGE             = "biz.baggage";

    public static final String SYS_BAGGAGE             = "sys.baggage";

    /**
     * TIME records the current span's begin time
     */
    public static final String TIME                    = "time";

    /**
     * TIME_COST_MILLISECONDS records the current span's cost time
     */
    public static final String TIME_COST_MILLISECONDS  = "time.cost.milliseconds";

    /**
     * TOTAL_COST_MILLISECONDS records the span's cost time period
     */
    public static final String TOTAL_COST_MILLISECONDS = "total.cost.milliseconds";

    /**
     * STAT_KEY is the span's stat log key for stat.key
     */
    public static final String STAT_KEY                = "stat.key";

    /**
     * COUNT is the span's stat log key for count
     */
    public static final String COUNT                   = "count";

    /**
     * SUCCESS is the span's stat log key for success
     */
    public static final String SUCCESS                 = "success";

    /**
     * LOAD_TEST is the span's stat log key for load.test
     */
    public static final String LOAD_TEST               = "load.test";

    public static final String MSG_TOPIC               = "msg.topic";
    public static final String MSG_ID                  = "msg.id";
    public static final String MSG_CHANNEL             = "msg.channel";

}