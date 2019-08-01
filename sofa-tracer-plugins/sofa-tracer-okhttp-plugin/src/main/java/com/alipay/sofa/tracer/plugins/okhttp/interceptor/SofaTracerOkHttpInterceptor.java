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
package com.alipay.sofa.tracer.plugins.okhttp.interceptor;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.registry.ExtendFormat;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractTracer;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.okhttp.OkHttpRequestCarrier;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

/**
 * @author xianglong.chen
 * @since  2019/1/16 17:51
 */
public class SofaTracerOkHttpInterceptor implements okhttp3.Interceptor {

    protected String         appName;

    protected String         targetAppName;

    protected AbstractTracer okHttpTracer;

    public SofaTracerOkHttpInterceptor(AbstractTracer okHttpTracer, String appName,
                                       String targetAppName) {
        this.okHttpTracer = okHttpTracer;
        this.appName = appName;
        this.targetAppName = targetAppName;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        SofaTracerSpan sofaTracerSpan = okHttpTracer.clientSend(request.method());
        Response response = chain.proceed(appendOkHttpRequestSpanTags(request, sofaTracerSpan));
        okHttpTracer.clientReceive(String.valueOf(response.code()));
        return response;
    }

    private Request appendOkHttpRequestSpanTags(Request request, SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null) {
            return null;
        }
        //appName
        String appName = SofaTracerConfiguration.getProperty(
            SofaTracerConfiguration.TRACER_APPNAME_KEY, StringUtils.EMPTY_STRING);
        //methodName
        String methodName = request.method();
        sofaTracerSpan.setTag(CommonSpanTags.LOCAL_APP, appName == null ? StringUtils.EMPTY_STRING
            : appName);
        //targetAppName
        sofaTracerSpan.setTag(CommonSpanTags.REMOTE_APP, StringUtils.EMPTY_STRING);
        sofaTracerSpan.setTag(CommonSpanTags.REQUEST_URL, request.url().toString());
        //method
        sofaTracerSpan.setTag(CommonSpanTags.METHOD, methodName);

        Headers headers = request.headers();
        if (headers != null) {
            List<String> contentLengthList = headers.values("Content-Length");
            if (contentLengthList != null && !contentLengthList.isEmpty()) {
                sofaTracerSpan.setTag(CommonSpanTags.REQ_SIZE,
                    Long.valueOf(contentLengthList.get(0)));
            }
        } else {
            sofaTracerSpan.setTag(CommonSpanTags.REQ_SIZE, String.valueOf(-1));
        }

        //carrier
        return this.injectCarrier(request, sofaTracerSpan);
    }

    public Request injectCarrier(Request request, SofaTracerSpan currentSpan) {
        Headers.Builder headerBuilder = request.headers().newBuilder();

        SofaTracer sofaTracer = this.okHttpTracer.getSofaTracer();
        sofaTracer.inject(currentSpan.getSofaTracerSpanContext(),
            ExtendFormat.Builtin.B3_HTTP_HEADERS, new OkHttpRequestCarrier(headerBuilder));

        Request.Builder requestBuilder = request.newBuilder();
        return requestBuilder.headers(headerBuilder.build()).build();
    }
}
