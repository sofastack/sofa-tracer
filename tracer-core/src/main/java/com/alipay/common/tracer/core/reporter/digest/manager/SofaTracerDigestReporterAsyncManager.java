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
     * Asynchronous log print, all middleware digest logs share a SofaTracerDigestReporterAsyncManager AsyncAppender to print logs
     */
    private static volatile AsyncCommonDigestAppenderManager asyncCommonDigestAppenderManager;

    /**
     * get singleton instance
     * @return asyncCommonDigestAppenderManager
     */
    public static AsyncCommonDigestAppenderManager getSofaTracerDigestReporterAsyncManager() {
        if (asyncCommonDigestAppenderManager == null) {
            synchronized (SofaTracerDigestReporterAsyncManager.class) {
                if (asyncCommonDigestAppenderManager == null) {
                    AsyncCommonDigestAppenderManager localManager = new AsyncCommonDigestAppenderManager(
                        1024);
                    localManager.start("NetworkAppender");
                    asyncCommonDigestAppenderManager = localManager;
                }
            }
        }
        return asyncCommonDigestAppenderManager;
    }
}
