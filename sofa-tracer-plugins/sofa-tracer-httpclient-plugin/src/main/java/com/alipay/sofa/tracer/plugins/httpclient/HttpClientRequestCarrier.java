package com.alipay.sofa.tracer.plugins.httpclient;

import io.opentracing.propagation.TextMap;
import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHeader;

import java.util.Iterator;
import java.util.Map;

/**
 * HttpClientRequestCarrier
 *
 * @author yangguanchao
 * @since 2018/08/07
 */
public class HttpClientRequestCarrier implements TextMap {

    private final HttpRequest request;

    public HttpClientRequestCarrier(HttpRequest request) {
        this.request = request;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        // no operation
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(String key, String value) {
        request.addHeader(new BasicHeader(key, value));
    }
}
