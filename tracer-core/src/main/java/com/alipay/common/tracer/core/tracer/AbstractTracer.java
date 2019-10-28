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
        Reporter reporter = this.generateReporter(this.generateStatReporter(),
            this.getDigestReporterLogName(), this.getDigestReporterRollingKey(),
            this.getDigestReporterLogNameKey(), this.getDigestEncoder());

        if (clientTracer && reporter != null) {
            builder.withClientReporter(reporter);
        }

        if (serverTracer && reporter != null) {
            builder.withServerReporter(reporter);
        }

        //build
        this.sofaTracer = builder.build();
    }

    protected Reporter generateReporter(AbstractSofaTracerStatisticReporter statReporter,
                                        String logName, String logRollingKey, String logNameKey,
                                        SpanEncoder<SofaTracerSpan> spanEncoder) {
        String digestRollingPolicy = SofaTracerConfiguration.getRollingPolicy(logRollingKey);
        String digestLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(logNameKey);
        DiskReporterImpl reporter = new DiskReporterImpl(logName, digestRollingPolicy,
            digestLogReserveConfig, spanEncoder, statReporter, logNameKey);
        return reporter;
    }

    /**
     * get digest reporter log name
     * @return
     */
    protected abstract String getDigestReporterLogName();

    /**
     * get digest reporter rolling key
     * @return
     */
    protected abstract String getDigestReporterRollingKey();

    /**
     * get digest reporter log name key
     * @return
     */
    protected abstract String getDigestReporterLogNameKey();

    /**
     * get digest encoder
     * @return
     */
    protected abstract SpanEncoder<SofaTracerSpan> getDigestEncoder();

    /**
     * get StatReporter
     * @return
     */
    protected abstract AbstractSofaTracerStatisticReporter generateStatReporter();

    /**
     * Stage CS , This stage will produce a new span
     * If there is a span in the current sofaTraceContext, it is the parent of the current Span
     *
     * @param operationName as span name
     * @return              a new spam
     */
    public SofaTracerSpan clientSend(String operationName) {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan serverSpan = sofaTraceContext.pop();
        SofaTracerSpan clientSpan = null;
        try {
            clientSpan = (SofaTracerSpan) this.sofaTracer.buildSpan(operationName)
                .asChildOf(serverSpan).start();
            // Need to actively cache your own serverSpan, because: asChildOf is concerned about spanContext
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
                // log
                clientSpan.log(LogData.CLIENT_SEND_EVENT_VALUE);
                // Put into the thread context
                sofaTraceContext.push(clientSpan);
            }
        }
        return clientSpan;
    }

    /**
     *
     * Stage CR, This stage will end a span
     *
     * @param resultCode resultCode to mark success or fail
     */
    public void clientReceive(String resultCode) {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan clientSpan = sofaTraceContext.pop();
        if (clientSpan == null) {
            return;
        }
        // finish and to report
        this.clientReceiveTagFinish(clientSpan, resultCode);
        // restore parent span
        if (clientSpan.getParentSofaTracerSpan() != null) {
            sofaTraceContext.push(clientSpan.getParentSofaTracerSpan());
        }
    }

    /**
     * Span finished and append tags
     * @param clientSpan current finished span
     * @param resultCode result status code
     */
    public void clientReceiveTagFinish(SofaTracerSpan clientSpan, String resultCode) {
        if (clientSpan != null) {
            // log event
            clientSpan.log(LogData.CLIENT_RECV_EVENT_VALUE);
            // set resultCode
            clientSpan.setTag(CommonSpanTags.RESULT_CODE, resultCode);
            // finish client span
            clientSpan.finish();
        }
    }

    /**
     * Stage SR , This stage will produce a new span.
     *
     * For example, the SpringMVC component accepts a network request,
     * we need to create an mvc span to record related information.
     *
     * we do not care SofaTracerSpanContext, just as root span
     *
     * @return SofaTracerSpan
     */
    public SofaTracerSpan serverReceive() {
        return this.serverReceive(null);
    }

    /**
     * server receive request
     * @param sofaTracerSpanContext The context to restore
     * @return SofaTracerSpan
     */
    public SofaTracerSpan serverReceive(SofaTracerSpanContext sofaTracerSpanContext) {
        SofaTracerSpan newSpan = null;
        // pop LogContext
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan serverSpan = sofaTraceContext.pop();
        boolean isCalculateSampled = false;
        try {
            if (serverSpan == null) {
                if (sofaTracerSpanContext == null) {
                    sofaTracerSpanContext = SofaTracerSpanContext.rootStart();
                    isCalculateSampled = true;
                } else {
                    sofaTracerSpanContext.setSpanId(sofaTracerSpanContext.nextChildContextId());
                }
                newSpan = this.genSeverSpanInstance(System.currentTimeMillis(),
                    StringUtils.EMPTY_STRING, sofaTracerSpanContext, null);
                // calculate sampled
                if (isCalculateSampled) {
                    sofaTracerSpanContext.setSampled(this.sofaTracer.getSampler().sample(newSpan)
                        .isSampled());
                }
            } else {
                // Without the setLogContextAndPush operation, span == null, so cast exception will not be thrown
                newSpan = serverSpan;
            }
        } catch (Throwable throwable) {
            SelfLog.errorWithTraceId("Middleware server received and restart root span", throwable);
            SelfLog.flush();
            Map<String, String> bizBaggage = null;
            Map<String, String> sysBaggage = null;
            if (serverSpan != null) {
                bizBaggage = serverSpan.getSofaTracerSpanContext().getBizBaggage();
                sysBaggage = serverSpan.getSofaTracerSpanContext().getSysBaggage();
            }
            newSpan = this.errorRecover(bizBaggage, sysBaggage);
        } finally {
            if (newSpan != null) {
                // log
                newSpan.log(LogData.SERVER_RECV_EVENT_VALUE);
                // server tags
                newSpan.setTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
                newSpan
                    .setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread().getName());
                // push to sofaTraceContext
                sofaTraceContext.push(newSpan);
            }
        }
        return newSpan;
    }

    /**
     * Stage SS, This stage will end a span
     *
     * @param resultCode
     */
    public void serverSend(String resultCode) {
        try {
            SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
            SofaTracerSpan serverSpan = sofaTraceContext.pop();
            if (serverSpan == null) {
                return;
            }
            // log
            serverSpan.log(LogData.SERVER_SEND_EVENT_VALUE);
            // resultCode
            serverSpan.setTag(CommonSpanTags.RESULT_CODE, resultCode);
            serverSpan.finish();
        } finally {
            // clear TreadLocalContext
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
     * Clean up all call context information: Note that the server can be cleaned up after receiving it;
     * the client does not have the right time to clean up (can only judge size <= 1)
     */
    private void clearTreadLocalContext() {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        sofaTraceContext.clear();
    }

    /**
     *
     * When an error occurs to remedy, start counting from the root node
     *
     * @param bizBaggage Business transparent transmission
     * @param sysBaggage System transparent transmission
     * @return root span
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