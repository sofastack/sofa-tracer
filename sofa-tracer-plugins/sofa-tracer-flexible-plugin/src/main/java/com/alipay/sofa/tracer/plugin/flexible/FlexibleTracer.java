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
package com.alipay.sofa.tracer.plugin.flexible;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.reporter.digest.DiskReporterImpl;
import com.alipay.common.tracer.core.reporter.facade.Reporter;
import com.alipay.common.tracer.core.samplers.Sampler;
import com.alipay.common.tracer.core.samplers.SamplerFactory;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import io.opentracing.tag.Tags;

import java.util.Map;

/**
 * 认为用户自定义的 Tracer 类型都是 client 类型
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/8/1 9:33 PM
 * @since:
 **/
public class FlexibleTracer extends SofaTracer {

    private final Reporter reporter;

    /**
     * 支持自定义采样和自定义上报器的构造器
     * @param sampler
     * @param reporter
     */
    public FlexibleTracer(Sampler sampler, Reporter reporter) {
        super(ComponentNameConstants.FLEXIBLE, sampler);
        this.reporter = reporter;
    }

    /**
     * 默认提供的支持 manual report 的构造器
     */
    public FlexibleTracer() {
        super(ComponentNameConstants.FLEXIBLE, null, null, initSampler(), null);
        this.reporter = initReporter();

    }

    private static Sampler initSampler() {
        try {
            return SamplerFactory.getSampler();
        } catch (Exception e) {
            SelfLog.error("Failed to get tracer sampler strategy;");
        }
        return null;
    }

    @Override
    public void reportSpan(SofaTracerSpan span) {
        if (span == null) {
            return;
        }
        // //sampler is support &  current span is root span
        if (this.getSampler() != null && span.getParentSofaTracerSpan() == null) {
            span.getSofaTracerSpanContext().setSampled(this.getSampler().sample(span).isSampled());
        }
        //invoke listener
        invokeReportListeners(span);
        if (this.reporter != null) {
            this.reporter.report(span);
        } else {
            SelfLog.warn("No reporter implement in flexible tracer");
        }
    }

    @Override
    public void close() {
        if (reporter != null) {
            reporter.close();
        }
        super.close();
    }

    public Reporter getReporter() {
        return reporter;
    }

    private Reporter initReporter() {
        String logRollingKey = FlexibleLogEnum.FLEXIBLE_DIGEST.getRollingKey();
        String logNameKey = FlexibleLogEnum.FLEXIBLE_DIGEST.getLogNameKey();
        String logName = FlexibleLogEnum.FLEXIBLE_DIGEST.getDefaultLogName();
        String digestRollingPolicy = SofaTracerConfiguration.getRollingPolicy(logRollingKey);
        String digestLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(logNameKey);

        SpanEncoder spanEncoder = new FlexibleDigestJsonEncoder();

        FlexibleStatJsonReporter statReporter = generateFlexibleStatJsonReporter();

        DiskReporterImpl reporter = new DiskReporterImpl(logName, digestRollingPolicy,
            digestLogReserveConfig, spanEncoder, statReporter, logNameKey);

        return reporter;
    }

    private FlexibleStatJsonReporter generateFlexibleStatJsonReporter() {
        FlexibleLogEnum flexibleLogEnum = FlexibleLogEnum.FLEXIBLE_STAT;
        String statLog = flexibleLogEnum.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration.getRollingPolicy(flexibleLogEnum
            .getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(flexibleLogEnum
            .getLogNameKey());
        return new FlexibleStatJsonReporter(statLog, statRollingPolicy, statLogReserveConfig);
    }

    public SofaTracerSpan beforeInvoke(String operationName) {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan serverSpan = sofaTraceContext.pop();
        SofaTracerSpan methodSpan = null;
        try {
            methodSpan = (SofaTracerSpan) this.buildSpan(operationName).asChildOf(serverSpan)
                .start();
            // Need to actively cache your own serverSpan, because: asChildOf is concerned about spanContext
            methodSpan.setParentSofaTracerSpan(serverSpan);
        } catch (Throwable throwable) {
            SelfLog.errorWithTraceId("Client Send Error And Restart by Root Span", throwable);
            SelfLog.flush();
            Map<String, String> bizBaggage = null;
            Map<String, String> sysBaggage = null;
            if (serverSpan != null) {
                bizBaggage = serverSpan.getSofaTracerSpanContext().getBizBaggage();
                sysBaggage = serverSpan.getSofaTracerSpanContext().getSysBaggage();
            }
            methodSpan = this.errorSpan(bizBaggage, sysBaggage);
        } finally {
            if (methodSpan != null) {
                // get appName
                String appName = SofaTracerConfiguration
                    .getProperty(SofaTracerConfiguration.TRACER_APPNAME_KEY);
                methodSpan.setTag(CommonSpanTags.LOCAL_APP, appName);
                methodSpan.setTag(CommonSpanTags.METHOD, operationName);
                // all as client
                methodSpan.setTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
                methodSpan.setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread()
                    .getName());
                methodSpan.log(LogData.CLIENT_SEND_EVENT_VALUE);
                sofaTraceContext.push(methodSpan);
            }
        }

        return methodSpan;
    }

    public void afterInvoke(String error) {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan clientSpan = sofaTraceContext.pop();
        if (clientSpan == null) {
            return;
        }
        // log event
        clientSpan.log(LogData.CLIENT_RECV_EVENT_VALUE);
        // set resultCode
        clientSpan.setTag(Tags.ERROR.getKey(), error);
        // finish client span
        clientSpan.finish();
        // restore parent span
        if (clientSpan.getParentSofaTracerSpan() != null) {
            sofaTraceContext.push(clientSpan.getParentSofaTracerSpan());
        }
    }

    private SofaTracerSpan errorSpan(Map<String, String> bizBaggage, Map<String, String> sysBaggage) {
        SofaTracerSpanContext spanContext = SofaTracerSpanContext.rootStart();
        spanContext.addBizBaggage(bizBaggage);
        spanContext.addSysBaggage(sysBaggage);
        return new SofaTracerSpan(this, System.currentTimeMillis(), null, StringUtils.EMPTY_STRING,
            spanContext, null);
    }
}
