package com.alipay.sofa.tracer.examples.okhttp.instance;

import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.okhttp.SofaTracerOkHttpBuilder;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/4/12 1:29 PM
 * @since:
 **/
public class OkHttpClientInstance {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

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
