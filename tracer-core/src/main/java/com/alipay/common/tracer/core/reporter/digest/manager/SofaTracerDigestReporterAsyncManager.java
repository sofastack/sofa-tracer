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
package com.alipay.common.tracer.core.reporter.digest.manager;

import com.alipay.common.tracer.core.appender.manager.AsyncCommonDigestAppenderManager;

/**
 * SofaTracerDigestReporterAsyncManager
 *
 * @author yangguanchao
 * @since  2017/06/20
 */
public final class SofaTracerDigestReporterAsyncManager {

    /**
     * 异步日志打印，所有的中间件 摘要日志公用一个 SofaTracerDigestReporterAsyncManager AsyncAppender 来打印日志
     */
    private static volatile AsyncCommonDigestAppenderManager asyncCommonDigestAppenderManager;

    /***
     *
     * 异步摘要日志打印，所有的中间件公用一个 AsyncAppender 来打印日志
     * @return 全局唯一的日志打印器
     */
    public static AsyncCommonDigestAppenderManager getSofaTracerDigestReporterAsyncManager() {
        if (asyncCommonDigestAppenderManager == null) {
            synchronized (SofaTracerDigestReporterAsyncManager.class) {
                if (asyncCommonDigestAppenderManager == null) {
                    asyncCommonDigestAppenderManager = new AsyncCommonDigestAppenderManager(1024);
                    asyncCommonDigestAppenderManager.start("NetworkAppender");
                }
            }
        }
        return asyncCommonDigestAppenderManager;
    }
}
