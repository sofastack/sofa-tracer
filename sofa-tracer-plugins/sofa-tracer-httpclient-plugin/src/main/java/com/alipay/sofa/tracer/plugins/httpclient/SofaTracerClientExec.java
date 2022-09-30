package com.alipay.sofa.tracer.plugins.httpclient;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.registry.ExtendFormat;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractTracer;
import io.opentracing.Scope;
import org.apache.http.HttpException;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpExecutionAware;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.execchain.ClientExecChain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SofaTracerClientExec implements ClientExecChain {

    private final AbstractTracer tracer;
    private final ClientExecChain requestExecutor;

    private List<SofaTracerClientSpanDecorator> spanDecorators;

    public SofaTracerClientExec(ClientExecChain clientExecChain,
                                AbstractTracer tracer,
                                List<SofaTracerClientSpanDecorator> spanDecorators){
        this.tracer = tracer;
        this.requestExecutor = clientExecChain;
        //this.redirectStrategy = redirectStrategy;
        this.spanDecorators = new ArrayList<>(spanDecorators);
    }



    @Override
    public CloseableHttpResponse execute(HttpRoute httpRoute,
                                         HttpRequestWrapper httpRequestWrapper,
                                         HttpClientContext httpClientContext,
                                         HttpExecutionAware httpExecutionAware) throws IOException, HttpException {
        RequestLine requestLine = httpRequestWrapper.getRequestLine();
        String methodName = requestLine.getMethod();
        //create a new span.
        SofaTracerSpan httpClientSpan = this.tracer.clientSend(methodName);
        CloseableHttpResponse response = null;
        try{
            return (response = handleNetworkProcessing(httpClientSpan, httpRoute, httpRequestWrapper, httpClientContext,  httpExecutionAware));
        }catch (Exception e){
            httpClientSpan.finish();
            throw e;
        }

    }

    public CloseableHttpResponse handleNetworkProcessing(SofaTracerSpan sofaTracerSpan,
                                                         HttpRoute route,
                                                         HttpRequestWrapper request,
                                                         HttpClientContext clientContext,
                                                         HttpExecutionAware execAware)
            throws HttpException, IOException {
        SofaTracer sofaTracer =  this.tracer.getSofaTracer();
        sofaTracer.activateSpan(sofaTracerSpan);
        sofaTracer.scopeManager().activate(sofaTracerSpan);

        sofaTracer.inject(sofaTracerSpan.getSofaTracerSpanContext(), ExtendFormat.Builtin.B3_HTTP_HEADERS, new HttpClientRequestCarrier(request));

        try (Scope redirectScope = sofaTracer.activateSpan(sofaTracerSpan)){
            for (SofaTracerClientSpanDecorator decorator : spanDecorators) {
                decorator.onRequest(request, clientContext, sofaTracerSpan);
            }
            CloseableHttpResponse response = requestExecutor.execute(route, request, clientContext, execAware);
            for (SofaTracerClientSpanDecorator decorator : spanDecorators) {
                decorator.onResponse(response, clientContext, sofaTracerSpan);
            }
            return response;
        } catch (IOException | HttpException | RuntimeException e) {
            for (SofaTracerClientSpanDecorator decorator: spanDecorators) {
                decorator.onError(request, clientContext, e, sofaTracerSpan);
            }
            throw e;
        } finally {
            sofaTracerSpan.finish();
        }

    }

}
