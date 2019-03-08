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

import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.samplers.Sampler;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.dubbo.tracer.DubboConsumerSofaTracer;
import com.alipay.sofa.tracer.plugins.dubbo.tracer.DubboProviderSofaTracer;
import io.opentracing.tag.Tags;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.remoting.TimeoutException;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.support.RpcUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/2/26 2:02 PM
 * @since: 2.3.4
 **/
@Activate(group = { Constants.PROVIDER, Constants.CONSUMER }, value = "dubboSofaTracerFilter", order = 1)
public class DubboSofaTracerFilter implements Filter {

    private String                             appName         = StringUtils.EMPTY_STRING;

    private static final String                BLANK           = StringUtils.EMPTY_STRING;

    private static final String                SUCCESS_CODE    = "00";

    private static final String                FAILED_CODE     = "99";

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
            result = doServerFilter(rpcContext, invoker, invocation);
        } else {
            result = doClientFilter(rpcContext, invoker, invocation);
        }
        return result;
    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        String spanKey = getTracerSpanMapKey(invoker);
        try {
            // 只有异步才进行回调打印
            boolean isAsync = RpcUtils.isAsync(invoker.getUrl(), invocation);
            if (!isAsync) {
                return result;
            }
            if (TracerSpanMap.containsKey(spanKey)) {
                SofaTracerSpan sofaTracerSpan = TracerSpanMap.get(spanKey);
                // to build tracer instance
                if (dubboConsumerSofaTracer == null) {
                    this.dubboConsumerSofaTracer = DubboConsumerSofaTracer
                        .getDubboConsumerSofaTracerSingleton();
                }
                String resultCode = SUCCESS_CODE;
                if (result.hasException()) {
                    if (result.getException() instanceof RpcException) {
                        resultCode = Integer.toString(((RpcException) result.getException())
                            .getCode());
                        sofaTracerSpan.setTag(CommonSpanTags.RESULT_CODE, resultCode);
                    } else {
                        resultCode = FAILED_CODE;
                    }
                }
                // add elapsed time
                appendElapsedTimeTags(invocation, sofaTracerSpan, result);
                dubboConsumerSofaTracer.clientReceiveTagFinish(sofaTracerSpan, resultCode);
            }
        } finally {
            if (TracerSpanMap.containsKey(spanKey)) {
                TracerSpanMap.remove(spanKey);
            }
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
        // check invoke type
        boolean isAsync = RpcUtils.isAsync(invoker.getUrl(), invocation);
        boolean isOneWay = false;
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
        String resultCode = SUCCESS_CODE;
        try {
            // do invoke
            result = invoker.invoke(invocation);
            // check result
            if (result == null) {
                // isOneWay, we think that the current request is successful
                if (isOneWay) {
                    sofaTracerSpan.setTag(CommonSpanTags.RESP_SIZE, 0);
                }
            } else {
                // add elapsed time
                appendElapsedTimeTags(invocation, sofaTracerSpan, result);
            }
        } catch (RpcException e) {
            exception = e;
            throw e;
        } catch (Throwable t) {
            exception = t;
            throw new RpcException(t);
        } finally {
            if (exception != null) {
                if (exception instanceof RpcException) {
                    RpcException rpcException = (RpcException) exception;
                    resultCode = String.valueOf(rpcException.getCode());
                } else {
                    resultCode = FAILED_CODE;
                }
            }

            if (!isAsync) {
                dubboConsumerSofaTracer.clientReceiveTagFinish(sofaTracerSpan, resultCode);
            } else {
                TracerSpanMap.put(getTracerSpanMapKey(invoker), sofaTracerSpan);
                CompletableFuture<Object> future = (CompletableFuture<Object>) RpcContext.getContext().getFuture();
                future.whenComplete((object, throwable)-> {
                    if (throwable != null && throwable instanceof TimeoutException) {
                        dubboConsumerSofaTracer.clientReceiveTagFinish(sofaTracerSpan, "03");
                    }
                });
            }
        }
        return result;
    }

    private void appendElapsedTimeTags(Invocation invocation, SofaTracerSpan sofaTracerSpan,
                                       Result result) {
        if (sofaTracerSpan == null) {
            return;
        }
        String elapsed = invocation.getAttachment(CommonSpanTags.CLIENT_SERIALIZE_TIME);
        //客户端请求序列化耗时
        if (StringUtils.isNotBlank(elapsed)) {
            sofaTracerSpan.setTag(CommonSpanTags.CLIENT_SERIALIZE_TIME, Integer.parseInt(elapsed));
        }
        String deElapsed = invocation.getAttachment(CommonSpanTags.CLIENT_DESERIALIZE_TIME);
        //客户端接受响应反序列化耗时
        if (StringUtils.isNotBlank(deElapsed)) {
            sofaTracerSpan.setTag(CommonSpanTags.CLIENT_DESERIALIZE_TIME,
                Integer.parseInt(deElapsed));
        }
        String reqSize = invocation.getAttachment(Constants.INPUT_KEY);
        if (StringUtils.isNotBlank(reqSize)) {
            sofaTracerSpan.setTag(CommonSpanTags.REQ_SIZE, Integer.parseInt(reqSize));
        }
        String respSize = result.getAttachment(Constants.OUTPUT_KEY);
        if (StringUtils.isNotBlank(respSize)) {
            sofaTracerSpan.setTag(CommonSpanTags.RESP_SIZE, Integer.parseInt(respSize));
        }
    }

    /**
     * rpc client handler
     * @param rpcContext
     * @param invoker
     * @param invocation
     * @return
     */
    private Result doServerFilter(RpcContext rpcContext, Invoker<?> invoker, Invocation invocation) {
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
            // 处理返回结果
            if (result == null) {
                return null;
            } else {
                String elapsed = invocation.getAttachment(CommonSpanTags.SERVER_SERIALIZE_TIME);
                if (StringUtils.isNotBlank(elapsed)) {
                    sofaTracerSpan.setTag(CommonSpanTags.SERVER_SERIALIZE_TIME,
                        Integer.parseInt(elapsed));
                }

                String deElapsed = invocation.getAttachment(CommonSpanTags.SERVER_DESERIALIZE_TIME);
                if (StringUtils.isNotBlank(deElapsed)) {
                    sofaTracerSpan.setTag(CommonSpanTags.SERVER_DESERIALIZE_TIME,
                        Integer.parseInt(deElapsed));
                }

                String reqSize = invocation.getAttachment(Constants.INPUT_KEY);
                if (StringUtils.isNotBlank(reqSize)) {
                    sofaTracerSpan.setTag(CommonSpanTags.REQ_SIZE, Integer.parseInt(reqSize));
                }
                String respSize = result.getAttachment(Constants.OUTPUT_KEY);
                if (StringUtils.isNotBlank(respSize)) {
                    sofaTracerSpan.setTag(CommonSpanTags.RESP_SIZE, Integer.parseInt(respSize));
                }
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
            String resultCode = SUCCESS_CODE;
            if (exception != null) {
                if (exception instanceof RpcException) {
                    RpcException rpcException = (RpcException) exception;
                    if (rpcException.isBiz()) {
                        resultCode = String.valueOf(rpcException.getCode());
                    }
                } else {
                    resultCode = FAILED_CODE;
                }
            }
            dubboProviderSofaTracer.serverSend(resultCode);
        }
    }

    private SofaTracerSpan serverReceived(Invocation invocation) {
        Map<String, String> tags = new HashMap<String, String>();
        //server tags 必须设置
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
        //放到线程上下文
        sofaTraceContext.push(serverSpan);
        return serverSpan;
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

    private String getTracerSpanMapKey(Invoker<?> invoker) {
        return SPAN_INVOKE_KEY + "." + invoker.hashCode();
    }
}
