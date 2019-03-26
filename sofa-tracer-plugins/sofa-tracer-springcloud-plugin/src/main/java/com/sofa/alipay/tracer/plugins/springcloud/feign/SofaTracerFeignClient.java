package com.sofa.alipay.tracer.plugins.springcloud.feign;

import feign.Client;
import feign.Request;
import feign.Response;

import java.io.IOException;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 10:50 AM
 * @since:
 **/
public class SofaTracerFeignClient implements Client {

    private FeignClientTracer tracer;
    private Client delegate;

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        return null;
    }

}
