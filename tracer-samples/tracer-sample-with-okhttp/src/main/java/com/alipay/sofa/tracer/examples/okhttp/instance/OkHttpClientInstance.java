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
package com.alipay.sofa.tracer.examples.okhttp.instance;

import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.okhttp.SofaTracerOkHttpBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/4/12 1:29 PM
 * @since:
 **/
public class OkHttpClientInstance {

    private OkHttpClient okHttpClient;

    public OkHttpClientInstance() {
        this.okHttpClient = getOkHttpClient();
    }

    private OkHttpClient getOkHttpClient() {
        if (okHttpClient != null) {
            return okHttpClient;
        } else {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            return SofaTracerOkHttpBuilder.clientBuilder(builder).build();
        }
    }

    public String executeGet(String url) throws Exception {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        if (!url.startsWith("http://")) {
            url = "http://" + url;
        }
        Request request = new Request.Builder().url(url).build();
        return okHttpClient.newCall(request).execute().body().string();
    }
}
