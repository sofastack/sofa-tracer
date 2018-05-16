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
package com.alipay.common.tracer.core.tracer;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.reporter.digest.DiskReporterImpl;
import com.alipay.common.tracer.core.reporter.facade.Reporter;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import io.opentracing.tag.Tags;

import java.util.Map;

/**
 * AbstractTracer
 *
 * @author yangguanchao
 * @since 2018/04/30
 */
public abstract class AbstractTracer {

    protected SofaTracer sofaTracer;

    public AbstractTracer(String tracerType) {
        this(tracerType, true, true);
    }

    public AbstractTracer(String tracerType, boolean clientTracer, boolean serverTracer) {
        SofaTracer.Builder builder = new SofaTracer.Builder(tracerType);
        //构造 client 的日志打印实例
        if (clientTracer) {
            Reporter clientReporter = this.generateReporter(this.generateClientStatReporter(),
                this.getClientDigestReporterLogName(), this.getClientDigestReporterRollingKey(),
                this.getClientDigestReporterLogNameKey(), this.getClientDigestEncoder());
            if (clientReporter != null) {
                builder.withClientReporter(clientReporter);
            }
        }
        //构造 server 的日志打印实例
        if (serverTracer) {
            Reporter serverReporter = this.generateReporter(this.generateServerStatReporter(),
                this.getServerDigestReporterLogName(), this.getServerDigestReporterRollingKey(),
                this.getServerDigestReporterLogNameKey(), this.getServerDigestEncoder());
            if (serverReporter != null) {
                builder.withServerReporter(serverReporter);
            }
        }
        //build
        this.sofaTracer = builder.build();
    }

    protected Reporter generateReporter(AbstractSofaTracerStatisticReporter statReporter,
                                        String logName, String logRollingKey, String logNameKey,
                                        SpanEncoder<SofaTracerSpan> spanEncoder) {
        //构造摘要实例
        String digestRollingPolicy = SofaTracerConfiguration.getRollingPolicy(logRollingKey);
        String digestLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(logNameKey);
        //构造实例
        DiskReporterImpl reporter = new DiskReporterImpl(logName, digestRollingPolicy,
            digestLogReserveConfig, spanEncoder, statReporter, logNameKey);
        return reporter;
    }

    protected abstract String getClientDigestReporterLogName();

    protected abstract String getClientDigestReporterRollingKey();

    protected abstract String getClientDigestReporterLogNameKey();

    protected abstract SpanEncoder<SofaTracerSpan> getClientDigestEncoder();

    protected abstract AbstractSofaTracerStatisticReporter generateClientStatReporter();

    protected abstract String getServerDigestReporterLogName();

    protected abstract String getServerDigestReporterRollingKey();

    protected abstract String getServerDigestReporterLogNameKey();

    protected abstract SpanEncoder<SofaTracerSpan> getServerDigestEncoder();

    protected abstract AbstractSofaTracerStatisticReporter generateServerStatReporter();

    /**
     * 注意:生成的 Span 未放入线程上下文中
     *
     * 在发生一次网络调用之前。创建一个新的上下文
     *
     * @param operationName 操作名称
     * @return 新的 span 上下文,不设置到线程上下文中
     */
    public SofaTracerSpan clientSend(String operationName) {
        //客户端的启动
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan serverSpan = sofaTraceContext.pop();
        SofaTracerSpan clientSpan = null;
        try {
            clientSpan = (SofaTracerSpan) this.sofaTracer.buildSpan(operationName)
                .asChildOf(serverSpan).start();
            //需要主动缓存自己的 serverSpan,原因是:asChildOf 关注的是 spanContext
            clientSpan.setParentSofaTracerSpan(serverSpan);
            return clientSpan;
        } catch (Throwable throwable) {
            SelfLog.errorWithTraceId("Client Send Error And Restart by Root Span", throwable);
            SelfLog.flush();
            Map<String, String> bizBaggage = null;
            Map<String, String> sysBaggage = null;
            if (serverSpan != null) {
                bizBaggage = serverSpan.getSofaTracerSpanContext().getBizBaggage();
                sysBaggage = serverSpan.getSofaTracerSpanContext().getSysBaggage();
            }
            clientSpan = this.errorRecover(bizBaggage, sysBaggage);
        } finally {
            if (clientSpan != null) {
                clientSpan.setTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
                clientSpan.setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread()
                    .getName());
                //log
                clientSpan.log(LogData.CLIENT_SEND_EVENT_VALUE);
                //放入线程上下文
                sofaTraceContext.push(clientSpan);
            }
        }
        return clientSpan;
    }

    /***
     * 客户端接收响应
     * @param resultCode 结果码
     */
    public void clientReceive(String resultCode) {
        //客户端
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan clientSpan = sofaTraceContext.pop();
        if (clientSpan == null) {
            return;
        }
        //log
        clientSpan.log(LogData.CLIENT_RECV_EVENT_VALUE);
        clientSpan.setTag(CommonSpanTags.RESULT_CODE, resultCode);
        //finish client
        clientSpan.finish();
        //client span
        if (clientSpan.getParentSofaTracerSpan() != null) {
            //restore parent
            sofaTraceContext.push(clientSpan.getParentSofaTracerSpan());
        }
    }

    /**
     * 收到请求
     * @return SofaTracerSpan
     */
    public SofaTracerSpan serverReceive() {
        return this.serverReceive(null);
    }

    /**
     * 收到请求
     * @param sofaTracerSpanContext 要恢复的上下文
     * @return SofaTracerSpan
     */
    public SofaTracerSpan serverReceive(SofaTracerSpanContext sofaTracerSpanContext) {
        SofaTracerSpan sofaTracerSpanServer = null;
        //pop LogContext
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan serverSpan = sofaTraceContext.pop();
        try {
            if (serverSpan == null) {
                //root 开始或者复用
                if (sofaTracerSpanContext == null) {
                    sofaTracerSpanContext = SofaTracerSpanContext.rootStart();
                }
                sofaTracerSpanServer = this.genSeverSpanInstance(System.currentTimeMillis(),
                    StringUtils.EMPTY_STRING, sofaTracerSpanContext, null);
            } else {
                //没有 setLogContextAndPush 操作,会 span == null,所以不会抛出 cast 异常
                sofaTracerSpanServer = serverSpan;
            }
            //push
            sofaTraceContext.push(sofaTracerSpanServer);
        } catch (Throwable throwable) {
            SelfLog.errorWithTraceId("Middleware server received and restart root span", throwable);
            SelfLog.flush();
            Map<String, String> bizBaggage = null;
            Map<String, String> sysBaggage = null;
            if (serverSpan != null) {
                bizBaggage = serverSpan.getSofaTracerSpanContext().getBizBaggage();
                sysBaggage = serverSpan.getSofaTracerSpanContext().getSysBaggage();
            }
            sofaTracerSpanServer = this.errorRecover(bizBaggage, sysBaggage);
            sofaTraceContext.push(sofaTracerSpanServer);
        } finally {
            if (sofaTracerSpanServer != null) {
                //log
                sofaTracerSpanServer.log(LogData.SERVER_RECV_EVENT_VALUE);
                //server tags 必须设置
                sofaTracerSpanServer.setTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
                sofaTracerSpanServer.setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread
                    .currentThread().getName());
            }
        }
        return sofaTracerSpanServer;
    }

    /**
     * 请求处理完成
     * @param resultCode 结果码
     */
    public void serverSend(String resultCode) {
        try {
            SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
            SofaTracerSpan serverSpan = sofaTraceContext.pop();
            if (serverSpan == null) {
                return;
            }
            //log
            serverSpan.log(LogData.SERVER_SEND_EVENT_VALUE);
            // 结果码
            serverSpan.setTag(CommonSpanTags.RESULT_CODE, resultCode);
            serverSpan.finish();
        } finally {
            //记得处理完成要清空 TL
            this.clearTreadLocalContext();
        }
    }

    protected SofaTracerSpan genSeverSpanInstance(long startTime, String operationName,
                                                  SofaTracerSpanContext sofaTracerSpanContext,
                                                  Map<String, ?> tags) {
        return new SofaTracerSpan(this.sofaTracer, startTime, null, operationName,
            sofaTracerSpanContext, tags);
    }

    /**
     * 清理全部调用上下文信息:注意服务端接收完毕才可以清理;而客户端是没有合适的时机进行清理的(只能判断 size <= 1)
     */
    private void clearTreadLocalContext() {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        sofaTraceContext.clear();
    }

    /***
     * 当发生错误进行补救,从根节点重新计数开始
     * @param bizBaggage 业务透传
     * @param sysBaggage 系统透传
     * @return 从根节点开始的上下文
     */
    protected SofaTracerSpan errorRecover(Map<String, String> bizBaggage,
                                          Map<String, String> sysBaggage) {
        SofaTracerSpanContext spanContext = SofaTracerSpanContext.rootStart();
        spanContext.addBizBaggage(bizBaggage);
        spanContext.addSysBaggage(sysBaggage);
        SofaTracerSpan span = this.genSeverSpanInstance(System.currentTimeMillis(),
            StringUtils.EMPTY_STRING, spanContext, null);
        return span;
    }

    public SofaTracer getSofaTracer() {
        return sofaTracer;
    }

}