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
package com.alipay.sofa.tracer.examples.httpclient.instance;

import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.okhttp.SofaTracerOkHttpBuilder;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 *
 * @description
 * @author xianglong.chen
 * @time 2019/1/17 13:33
 */
public class OkHttpClientInstance {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient          okHttpClient;

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

    public String executePost(String url, String content) throws Exception {
        //https://www.baeldung.com/httpclient-post-http-request
        if (!url.startsWith("http://")) {
            url = "http://" + url;
        }
        RequestBody body = RequestBody.create(JSON, content);
        Request request = new Request.Builder().url(url).post(body).build();
        return okHttpClient.newCall(request).execute().body().string();
    }
}
