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
 * 一些通用的 SpanTags
 * @author luoguimu123
 * @version $Id: CommonSpanTags.java, v 0.1 2018年01月29日 下午12:10 luoguimu123 Exp $
 */
public class CommonSpanTags {

    //************** String 类型 **************
    /***
     * 当前应用名称,注意和 RPC 保持一致 com.alipay.sofa.rpc.tracer.log.tags.RpcSpanTags#LOCAL_APP
     */
    public static final String LOCAL_APP           = "local.app";

    /**
     * 结果码, 具体含义根据实际每个中间件的约定不同而不同
     */
    public static final String RESULT_CODE         = "result.code";

    /***
     * 当前线程名字
     */
    public static final String CURRENT_THREAD_NAME = "current.thread.name";

    /***
     * 请求 url
     */
    public static final String REQUEST_URL         = "request.url";

    /***
     * 方法名称
     */
    public static final String METHOD              = "method";

    //************** Number 类型 **************

    /***
     * 请求大小
     */
    public static final String REQ_SIZE            = "req.size.bytes";

    /***
     * 响应大小
     */
    public static final String RESP_SIZE           = "resp.size.bytes";

}