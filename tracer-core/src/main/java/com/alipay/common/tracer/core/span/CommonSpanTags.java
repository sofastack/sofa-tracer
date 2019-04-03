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
    public static final String RESP_SIZE                        = "resp.size.bytes";

    /**
     * PROTOCOL records the request protocol type.
     */
    public static final String PROTOCOL                         = "protocol";

    /**
     * SERVICE records the rpc service interface.
     */
    public static final String SERVICE                          = "service";

    /**
     * REMOTE_HOST records the rpc target host.
     */
    public static final String REMOTE_HOST                      = "remote.host";
    /**
     * REMOTE_PORT records the rpc target port.
     */
    public static final String REMOTE_PORT                      = "remote.port";

    /**
     * LOCAL_HOST records the local host.
     */
    public static final String LOCAL_HOST                       = "local.host";

    /**
     * LOCAL_PORT records the local port.
     */
    public static final String LOCAL_PORT                       = "local.port";

    /**
     * INVOKE_TYPE records the invoke type(oneway/sync/async).
     */
    public static final String INVOKE_TYPE                      = "invoke.type";

    /**
     * RPC_TRACE_NAME constants key for dubbo rpc transfer.
     */
    public static final String RPC_TRACE_NAME                   = "dubbo.rpc.sofa.tracer";

    /**
     * CLIENT_SERIALIZE_TIME records the rpc client serializes the request body time
     */
    public static final String CLIENT_SERIALIZE_TIME            = "client.serialize.time";

    /**
     * SERVER_SERIALIZE_TIME records the rpc server serializes the response body time
     */
    public static final String SERVER_SERIALIZE_TIME            = "server.serialize.time";

    /**
     * CLIENT_DESERIALIZE_TIME records the rpc client deserialize the response body time
     */
    public static final String CLIENT_DESERIALIZE_TIME          = "client.deserialize.time";

    /**
     * SERVER_DESERIALIZE_TIME records the rpc server deserialize the request body time
     */
    public static final String SERVER_DESERIALIZE_TIME          = "server.deserialize.time";

    public static final String COMPONENT_CLIENT                 = "component.client.impl";
    /**
     * HYSTRIX_COMMAND_KEY records the hystrix commandKey.
     */
    public static final String HYSTRIX_COMMAND_KEY              = "hystrix.command.key";
    /**
     * HYSTRIX_COMMAND_GROUP_KEY records the hystrix commandGroup.
     */
    public static final String HYSTRIX_COMMAND_GROUP_KEY        = "hystrix.command.group";
    /**
     * HYSTRIX_THREAD_POOL_KEY records the hystrix threadPoolKey.
     */
    public static final String HYSTRIX_THREAD_POOL_KEY          = "hystrix.threadpool.key";
    /**
     * HYSTRIX_FALLBACK_METHOD_NAME_KEY records the hystrix fallbackMethodName.
     */
    public static final String HYSTRIX_FALLBACK_METHOD_NAME_KEY = "hystrix.fallback.methodname";
}