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
package com.alipay.sofa.tracer.plugins.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.samplers.Sampler;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.dubbo.constants.AttachmentKeyConstants;
import com.alipay.sofa.tracer.plugins.dubbo.tracer.DubboConsumerSofaTracer;
import com.alipay.sofa.tracer.plugins.dubbo.tracer.DubboProviderSofaTracer;
import io.opentracing.tag.Tags;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/2/26 2:02 PM
 * @since: 2.3.4
 **/
@Activate(group = { Constants.PROVIDER, Constants.CONSUMER }, value = "dubboSofaTracerFilter")
public class DubboSofaTracerFilter implements Filter {

    private String                             appName         = StringUtils.EMPTY_STRING;

    private static final String                BLANK           = StringUtils.EMPTY_STRING;

    private static final String                SPAN_INVOKE_KEY = "sofa.current.span.key";

    private DubboConsumerSofaTracer            dubboConsumerSofaTracer;

    private DubboProviderSofaTracer            dubboProviderSofaTracer;

    private static Map<String, SofaTracerSpan> TracerSpanMap   = new ConcurrentHashMap<String, SofaTracerSpan>();

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // do not record
        if ("$echo".equals(invocation.getMethodName())) {
            return invoker.invoke(invocation);
        }

        RpcContext rpcContext = RpcContext.getContext();
        // get appName
        if (StringUtils.isBlank(this.appName)) {
            this.appName = SofaTracerConfiguration
                .getProperty(SofaTracerConfiguration.TRACER_APPNAME_KEY);
        }
        // get span kind by rpc request type
        String spanKind = spanKind(rpcContext);
        Result result;
        if (spanKind.equals(Tags.SPAN_KIND_SERVER)) {
            result = doServerFilter(invoker, invocation);
        } else {
            result = doClientFilter(rpcContext, invoker, invocation);
        }
        return result;
    }

    /**
     * rpc client handler
     * @param rpcContext
     * @param invoker
     * @param invocation
     * @return
     */
    private Result doClientFilter(RpcContext rpcContext, Invoker<?> invoker, Invocation invocation) {
        // to build tracer instance
        if (dubboConsumerSofaTracer == null) {
            this.dubboConsumerSofaTracer = DubboConsumerSofaTracer
                .getDubboConsumerSofaTracerSingleton();
        }
        // get methodName
        String methodName = rpcContext.getMethodName();
        // get service interface
        String service = invoker.getInterface().getSimpleName();
        // build a dubbo rpc span
        SofaTracerSpan sofaTracerSpan = dubboConsumerSofaTracer.clientSend(service + "#"
                                                                           + methodName);
        // set tags to span
        appendRpcClientSpanTags(invoker, sofaTracerSpan);
        // do serialized and then transparent transmission to the rpc server
        String serializedSpanContext = sofaTracerSpan.getSofaTracerSpanContext()
            .serializeSpanContext();
        //put into attachments
        invocation.getAttachments().put(CommonSpanTags.RPC_TRACE_NAME, serializedSpanContext);

        boolean isOneWay = false, deferFinish = false;

        // check invoke type
        boolean isAsync = RpcUtils.isAsync(invoker.getUrl(), invocation);

        // set invoke type tag
        if (isAsync) {
            sofaTracerSpan.setTag(CommonSpanTags.INVOKE_TYPE, "future");
        } else {
            isOneWay = RpcUtils.isOneway(invoker.getUrl(), invocation);
            if (isOneWay) {
                sofaTracerSpan.setTag(CommonSpanTags.INVOKE_TYPE, "oneway");
            } else {
                sofaTracerSpan.setTag(CommonSpanTags.INVOKE_TYPE, "sync");
            }
        }

        Result result;
        Throwable exception = null;
        String resultCode = SofaTracerConstant.RESULT_CODE_SUCCESS;
        try {
            // do invoke
            result = invoker.invoke(invocation);
            if (result.hasException()) {
                exception = result.getException();
            }
            // the case on async client invocation
            Future<Object> future = rpcContext.getFuture();
            if (future instanceof FutureAdapter) {
                deferFinish = true;
                ResponseFuture responseFuture = ((FutureAdapter<Object>) future).getFuture();
                ResponseFuture responseFutureDelegate = new AsyncResponseFutureDelegate(invocation,
                    invoker, responseFuture);
                RpcContext.getContext().setFuture(new FutureAdapter<>(responseFutureDelegate));
            }
            return result;
        } catch (RpcException e) {
            exception = e;
            throw e;
        } catch (Throwable t) {
            exception = t;
            throw new RpcException(t);
        } finally {
            if (exception != null) {
                // finish span on exception
                handleError(exception, sofaTracerSpan);
            } else {
                // sync invoke
                if (isOneWay || !deferFinish) {
                    dubboConsumerSofaTracer.clientReceive(resultCode);
                } else {
                    // to clean SofaTraceContext
                    SofaTraceContext sofaTraceContext = SofaTraceContextHolder
                        .getSofaTraceContext();
                    SofaTracerSpan clientSpan = sofaTraceContext.pop();
                    if (clientSpan != null) {
                        // Record client send event
                        sofaTracerSpan.log(LogData.CLIENT_SEND_EVENT_VALUE);
                    }
                    // cache the current span
                    TracerSpanMap.put(getTracerSpanMapKey(invoker), sofaTracerSpan);
                    if (clientSpan != null && clientSpan.getParentSofaTracerSpan() != null) {
                        //restore parent
                        sofaTraceContext.push(clientSpan.getParentSofaTracerSpan());
                    }
                }
            }
        }
    }

    static void handleError(Throwable error, SofaTracerSpan span) {
        String errorCode;
        if (error instanceof RpcException) {
            errorCode = Integer.toString(((RpcException) error).getCode());
        } else {
            errorCode = SofaTracerConstant.RESULT_CODE_ERROR;
        }
        span.setTag(Tags.ERROR.getKey(), error.getMessage());
        DubboConsumerSofaTracer.getDubboConsumerSofaTracerSingleton().clientReceiveTagFinish(span,
            errorCode);
    }

    /**
     * TracerFinishResponseCallback
     */
    static class TracerFinishResponseCallback implements ResponseCallback {

        private final Invocation       invocation;
        private final Invoker<?>       invoker;
        private final ResponseCallback responseCallback;

        TracerFinishResponseCallback(Invocation invocation, Invoker<?> invoker,
                                     ResponseCallback responseCallback) {
            this.invocation = invocation;
            this.invoker = invoker;
            this.responseCallback = responseCallback;
        }

        @Override
        public void done(Object response) {
            if (response instanceof RpcResult) {
                // add elapsed time
                doFinishTracerUnderAsync((RpcResult) response, invoker, invocation);
            } else {
                SelfLog.warn("Dubbo async invoke call back response type is " + response);
            }
            responseCallback.done(response);
        }

        @Override
        public void caught(Throwable throwable) {
            String spanKey = getTracerSpanMapKey(invoker);
            try {
                if (TracerSpanMap.containsKey(spanKey)) {
                    SofaTracerSpan sofaTracerSpan = TracerSpanMap.get(spanKey);
                    handleError(throwable, sofaTracerSpan);
                }
            } finally {
                TracerSpanMap.remove(spanKey);
            }
            responseCallback.caught(throwable);
        }
    }

    /**
     * ResponseFuture Delegate Class to Resolve ResponseCallBack are covered
     */
    public class AsyncResponseFutureDelegate implements ResponseFuture {

        private final ResponseFuture responseFuture;
        private final Invocation     invocation;
        private final Invoker<?>     invoker;

        public AsyncResponseFutureDelegate(Invocation invocation, Invoker<?> invoker,
                                           ResponseFuture responseFuture) {
            this.responseFuture = responseFuture;
            this.invocation = invocation;
            this.invoker = invoker;
        }

        @Override
        public Object get() throws RemotingException {
            return responseFuture.get();
        }

        @Override
        public Object get(int timeoutInMillis) throws RemotingException {
            return responseFuture.get(timeoutInMillis);
        }

        @Override
        public void setCallback(ResponseCallback callback) {
            responseFuture.setCallback(new TracerFinishResponseCallback(invocation, invoker,
                callback));
        }

        @Override
        public boolean isDone() {
            return responseFuture.isDone();
        }
    }

    /**
     * finish tracer under async
     * @param result
     * @param invoker
     * @param invocation
     */
    public static void doFinishTracerUnderAsync(Result result, Invoker<?> invoker,
                                                Invocation invocation) {
        String spanKey = getTracerSpanMapKey(invoker);
        DubboConsumerSofaTracer dubboConsumerSofaTracer = DubboConsumerSofaTracer
            .getDubboConsumerSofaTracerSingleton();
        try {
            if (TracerSpanMap.containsKey(spanKey)) {
                SofaTracerSpan sofaTracerSpan = TracerSpanMap.get(spanKey);
                // to build tracer instance
                String resultCode = SofaTracerConstant.RESULT_CODE_SUCCESS;
                if (result.hasException()) {
                    if (result.getException() instanceof RpcException) {
                        resultCode = Integer.toString(((RpcException) result.getException())
                            .getCode());
                        sofaTracerSpan.setTag(CommonSpanTags.RESULT_CODE, resultCode);
                    } else {
                        resultCode = SofaTracerConstant.RESULT_CODE_ERROR;
                    }
                }
                // add elapsed time
                appendElapsedTimeTags(invocation, sofaTracerSpan, result, true);
                dubboConsumerSofaTracer.clientReceiveTagFinish(sofaTracerSpan, resultCode);
            }
        } finally {
            if (TracerSpanMap.containsKey(spanKey)) {
                TracerSpanMap.remove(spanKey);
            }
        }
    }

    /**
     * rpc client handler
     * @param invoker
     * @param invocation
     * @return
     */
    private Result doServerFilter(Invoker<?> invoker, Invocation invocation) {
        if (dubboProviderSofaTracer == null) {
            this.dubboProviderSofaTracer = DubboProviderSofaTracer
                .getDubboProviderSofaTracerSingleton();
        }
        SofaTracerSpan sofaTracerSpan = serverReceived(invocation);
        appendRpcServerSpanTags(invoker, sofaTracerSpan);
        Result result;
        Throwable exception = null;
        try {
            result = invoker.invoke(invocation);
            if (result == null) {
                return null;
            } else {
                appendElapsedTimeTags(invocation, sofaTracerSpan, result, false);
            }
            if (result.hasException()) {
                exception = result.getException();
            }
            return result;
        } catch (RpcException e) {
            exception = e;
            throw e;
        } catch (Throwable t) {
            exception = t;
            throw new RpcException(t);
        } finally {
            String resultCode = SofaTracerConstant.RESULT_CODE_SUCCESS;
            if (exception != null) {
                if (exception instanceof RpcException) {
                    sofaTracerSpan.setTag(Tags.ERROR.getKey(), exception.getMessage());
                    RpcException rpcException = (RpcException) exception;
                    if (rpcException.isBiz()) {
                        resultCode = String.valueOf(rpcException.getCode());
                    }
                } else {
                    resultCode = SofaTracerConstant.RESULT_CODE_ERROR;
                }
            }
            dubboProviderSofaTracer.serverSend(resultCode);
        }
    }

    /**
     * dubbo server receive request
     * @param invocation
     * @return
     */
    private SofaTracerSpan serverReceived(Invocation invocation) {
        Map<String, String> tags = new HashMap<>();
        tags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
        String serializeSpanContext = invocation.getAttachments()
            .get(CommonSpanTags.RPC_TRACE_NAME);
        SofaTracerSpanContext sofaTracerSpanContext = SofaTracerSpanContext
            .deserializeFromString(serializeSpanContext);
        boolean isCalculateSampler = false;
        boolean isSampled = true;
        if (sofaTracerSpanContext == null) {
            SelfLog
                .error("SpanContext created error when server received and root SpanContext created.");
            sofaTracerSpanContext = SofaTracerSpanContext.rootStart();
            isCalculateSampler = true;
        }
        String simpleName = invocation.getInvoker().getInterface().getSimpleName();
        SofaTracerSpan serverSpan = new SofaTracerSpan(dubboProviderSofaTracer.getSofaTracer(),
            System.currentTimeMillis(), simpleName, sofaTracerSpanContext, tags);
        // calculate sampler
        if (isCalculateSampler) {
            Sampler sampler = dubboProviderSofaTracer.getSofaTracer().getSampler();
            if (sampler != null) {
                isSampled = sampler.sample(serverSpan).isSampled();
            }
            sofaTracerSpanContext.setSampled(isSampled);
        }
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        // Record server receive event
        serverSpan.log(LogData.SERVER_RECV_EVENT_VALUE);
        sofaTraceContext.push(serverSpan);
        return serverSpan;
    }

    /**
     * append tag
     * @param invocation
     * @param sofaTracerSpan
     * @param result
     * @param isClient
     */
    private static void appendElapsedTimeTags(Invocation invocation, SofaTracerSpan sofaTracerSpan,
                                              Result result, boolean isClient) {
        if (sofaTracerSpan == null) {
            return;
        }
        String reqSize;
        String respSize;
        String elapsed;
        String deElapsed;
        if (isClient) {
            reqSize = invocation.getAttachment(AttachmentKeyConstants.CLIENT_SERIALIZE_SIZE);
            elapsed = invocation.getAttachment(AttachmentKeyConstants.CLIENT_SERIALIZE_TIME);
            respSize = result.getAttachment(AttachmentKeyConstants.CLIENT_DESERIALIZE_SIZE);
            deElapsed = result.getAttachment(AttachmentKeyConstants.CLIENT_DESERIALIZE_TIME);
            sofaTracerSpan.setTag(AttachmentKeyConstants.CLIENT_SERIALIZE_TIME,
                parseAttachment(elapsed, 0));
            sofaTracerSpan.setTag(AttachmentKeyConstants.CLIENT_DESERIALIZE_TIME,
                parseAttachment(deElapsed, 0));
            sofaTracerSpan.setTag(AttachmentKeyConstants.CLIENT_SERIALIZE_SIZE,
                parseAttachment(reqSize, 0));
            sofaTracerSpan.setTag(AttachmentKeyConstants.CLIENT_DESERIALIZE_SIZE,
                parseAttachment(respSize, 0));
        } else {
            reqSize = invocation.getAttachment(AttachmentKeyConstants.SERVER_DESERIALIZE_SIZE);
            deElapsed = invocation.getAttachment(AttachmentKeyConstants.SERVER_DESERIALIZE_TIME);
            respSize = result.getAttachment(AttachmentKeyConstants.SERVER_SERIALIZE_SIZE);
            elapsed = result.getAttachment(AttachmentKeyConstants.SERVER_SERIALIZE_TIME);
            sofaTracerSpan.setTag(AttachmentKeyConstants.SERVER_DESERIALIZE_SIZE,
                parseAttachment(reqSize, 0));
            sofaTracerSpan.setTag(AttachmentKeyConstants.SERVER_DESERIALIZE_TIME,
                parseAttachment(deElapsed, 0));
            sofaTracerSpan.setTag(AttachmentKeyConstants.SERVER_SERIALIZE_SIZE,
                parseAttachment(respSize, 0));
            sofaTracerSpan.setTag(AttachmentKeyConstants.SERVER_SERIALIZE_TIME,
                parseAttachment(elapsed, 0));
        }

    }

    /**
     * parse dubbo attachment
     * @param value
     * @param defaultVal
     * @return
     */
    private static int parseAttachment(String value, int defaultVal) {
        try {
            if (StringUtils.isNotBlank(value)) {
                defaultVal = Integer.parseInt(value);
            }
        } catch (Exception e) {
            SelfLog.error("Failed to parse Dubbo plugin params.", e);
        }
        return defaultVal;
    }

    /**
     * set rpc server span tags
     * @param invoker
     * @param sofaTracerSpan
     */
    private void appendRpcServerSpanTags(Invoker<?> invoker, SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null) {
            return;
        }
        RpcContext rpcContext = RpcContext.getContext();
        Map<String, String> tagsStr = sofaTracerSpan.getTagsWithStr();
        tagsStr.put(Tags.SPAN_KIND.getKey(), spanKind(rpcContext));
        String service = invoker.getInterface().getName();
        tagsStr.put(CommonSpanTags.SERVICE, service == null ? BLANK : service);
        String methodName = rpcContext.getMethodName();
        tagsStr.put(CommonSpanTags.METHOD, methodName == null ? BLANK : methodName);
        String app = rpcContext.getUrl().getParameter(Constants.APPLICATION_KEY);
        tagsStr.put(CommonSpanTags.REMOTE_HOST, rpcContext.getRemoteHost());
        tagsStr.put(CommonSpanTags.LOCAL_APP, app == null ? BLANK : app);
        tagsStr.put(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread().getName());
        String protocol = rpcContext.getUrl().getProtocol();
        tagsStr.put(CommonSpanTags.PROTOCOL, protocol == null ? BLANK : protocol);
        tagsStr.put(CommonSpanTags.LOCAL_HOST, rpcContext.getRemoteHost());
        tagsStr.put(CommonSpanTags.LOCAL_PORT, String.valueOf(rpcContext.getRemotePort()));
    }

    /**
     * set rpc client span tags
     * @param invoker
     * @param sofaTracerSpan
     */
    private void appendRpcClientSpanTags(Invoker<?> invoker, SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null) {
            return;
        }
        RpcContext rpcContext = RpcContext.getContext();
        Map<String, String> tagsStr = sofaTracerSpan.getTagsWithStr();
        tagsStr.put(Tags.SPAN_KIND.getKey(), spanKind(rpcContext));
        String protocol = rpcContext.getUrl().getProtocol();
        tagsStr.put(CommonSpanTags.PROTOCOL, protocol == null ? BLANK : protocol);
        String service = invoker.getInterface().getName();
        tagsStr.put(CommonSpanTags.SERVICE, service == null ? BLANK : service);
        String methodName = rpcContext.getMethodName();
        tagsStr.put(CommonSpanTags.METHOD, methodName == null ? BLANK : methodName);
        tagsStr.put(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread().getName());
        String app = rpcContext.getUrl().getParameter(Constants.APPLICATION_KEY);
        tagsStr.put(CommonSpanTags.LOCAL_APP, app == null ? BLANK : app);
        tagsStr.put(CommonSpanTags.REMOTE_HOST, rpcContext.getRemoteHost());
        tagsStr.put(CommonSpanTags.REMOTE_PORT, String.valueOf(rpcContext.getRemotePort()));
        tagsStr.put(CommonSpanTags.LOCAL_HOST, rpcContext.getLocalHost());
    }

    private String spanKind(RpcContext rpcContext) {
        return rpcContext.isConsumerSide() ? Tags.SPAN_KIND_CLIENT : Tags.SPAN_KIND_SERVER;
    }

    private static String getTracerSpanMapKey(Invoker<?> invoker) {
        return SPAN_INVOKE_KEY + "." + invoker.hashCode();
    }
}
