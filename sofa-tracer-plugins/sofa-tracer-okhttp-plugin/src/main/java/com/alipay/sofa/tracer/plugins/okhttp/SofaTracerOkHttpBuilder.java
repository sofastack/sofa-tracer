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
package com.alipay.sofa.tracer.plugins.okhttp;

import com.alipay.common.tracer.core.tracer.AbstractTracer;
import com.alipay.sofa.tracer.plugins.okhttp.interceptor.SofaTracerOkHttpInterceptor;
import okhttp3.OkHttpClient;

/**
 * @author xianglong.chen
 * @since 2019/1/17 13:29
 */
public class SofaTracerOkHttpBuilder {

    public static OkHttpClient.Builder clientBuilder(OkHttpClient.Builder clientBuilder) {
        return clientBuilder(clientBuilder, null, null);
    }

    public static OkHttpClient.Builder clientBuilder(OkHttpClient.Builder clientBuilder,
                                                     String currentApp, String targetApp) {
        SofaTracerOkHttpInterceptor interceptor = new SofaTracerOkHttpInterceptor(
            getOkHttpTracer(), currentApp, targetApp);
        return clientBuilder.addInterceptor(interceptor);
    }

    public static AbstractTracer getOkHttpTracer() {
        return OkHttpTracer.getOkHttpTracerSingleton();
    }
}
