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
package com.alipay.common.tracer.test.base;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.appender.TracerLogRootDaemon;
import com.alipay.common.tracer.core.appender.file.TimedRollingFileAppender;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.generator.TraceIdGenerator;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.reporter.digest.DiskReporterImpl;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.reporter.stat.SofaTracerStatisticReporter;
import com.alipay.common.tracer.core.reporter.stat.model.StatKey;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.common.tracer.core.utils.TracerUtils;

import com.alipay.common.tracer.test.core.sofatracer.encoder.ClientSpanEncoder;
import com.alipay.common.tracer.test.core.sofatracer.encoder.ServerSpanEncoder;
import com.alipay.common.tracer.test.core.sofatracer.type.TracerTestLogEnum;
import io.opentracing.tag.Tags;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * AbstractTestBase
 *
 * @author yangguanchao
 * @since 2017/07/01
 */
public abstract class AbstractTestBase {

    public static String        logDirectoryPath = TracerLogRootDaemon.LOG_FILE_DIR;

    public static File          logDirectory     = new File(logDirectoryPath);

    /***
     * 为了测试方便定义一个常量
     */
    protected static final long duration         = 100;

    protected static SofaTracer tracer;

    static {
        AbstractSofaTracerStatisticReporter statClientReporter = new AbstractSofaTracerStatisticReporter(
            TracerTestLogEnum.RPC_CLIENT_STAT.getDefaultLogName(), 1, 0,
            TimedRollingFileAppender.DAILY_ROLLING_PATTERN, "0D1H") {
            @Override
            public void doReportStat(SofaTracerSpan sofaTracerSpan) {
                StatKey statKey = new StatKey();
                String fromApp = "client";
                String toApp = "server";
                String zone = "targetZone";
                String serviceName = "service";
                String methodName = "method";
                statKey
                    .setKey(buildString(new String[] { fromApp, toApp, serviceName, methodName }));
                statKey.setResult(true ? "Y" : "N");
                //压测
                String mark = StringUtils.isBlank(sofaTracerSpan.getTagsWithStr().get(
                    SofaTracerConstant.LOAD_TEST_TAG)) ? "F" : "T";
                statKey.setEnd(buildString(new String[] { mark, zone }));
                //必须设置，和mark不同，这个只打印mark
                statKey.setLoadTest(TracerUtils.isLoadTest(sofaTracerSpan));

                //次数和耗时，最后一个耗时是单独打印的字段
                long values[] = new long[] { 1, duration };
                addStat(statKey, values);
            }
        };
        //摘要日志
        String clientLogType = TracerTestLogEnum.RPC_CLIENT_DIGEST.getDefaultLogName();
        //client
        DiskReporterImpl clientDigestReporter = new DiskReporterImpl(clientLogType,
            SofaTracerConfiguration.getRollingPolicy(TracerTestLogEnum.RPC_CLIENT_DIGEST
                .getRollingKey()),
            SofaTracerConfiguration.getLogReserveConfig(TracerTestLogEnum.RPC_CLIENT_DIGEST
                .getLogReverseKey()), new ClientSpanEncoder(), statClientReporter);

        SofaTracerStatisticReporter statServerReporter = new AbstractSofaTracerStatisticReporter(
            TracerTestLogEnum.RPC_SERVER_STAT.getDefaultLogName(), 1, 0,
            TimedRollingFileAppender.DAILY_ROLLING_PATTERN, "0D1H") {
            @Override
            public void doReportStat(SofaTracerSpan sofaTracerSpan) {
                StatKey statKey = new StatKey();
                String fromApp = "server";
                String toApp = "client";
                String zone = "targetZone";
                String serviceName = "service";
                String methodName = "method";
                //key 关键字 {@link com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter.print}
                statKey
                    .setKey(buildString(new String[] { fromApp, toApp, serviceName, methodName }));
                statKey.setResult(true ? "Y" : "N");
                //压测
                String mark = StringUtils.isBlank(sofaTracerSpan.getTagsWithStr().get(
                    SofaTracerConstant.LOAD_TEST_TAG)) ? "F" : "T";
                statKey.setEnd(buildString(new String[] { mark, zone }));
                //必须设置，和mark不同，这个只打印mark
                statKey.setLoadTest(TracerUtils.isLoadTest(sofaTracerSpan));

                //次数和耗时，最后一个耗时是单独打印的字段
                long values[] = new long[] { 1, duration };
                addStat(statKey, values);
            }
        };

        String serverLogType = TracerTestLogEnum.RPC_SERVER_DIGEST.getDefaultLogName();
        //server
        DiskReporterImpl serverDigestReporter = new DiskReporterImpl(serverLogType,
            SofaTracerConfiguration.getRollingPolicy(TracerTestLogEnum.RPC_SERVER_DIGEST
                .getRollingKey()),
            SofaTracerConfiguration.getLogReserveConfig(TracerTestLogEnum.RPC_SERVER_DIGEST
                .getLogReverseKey()), new ServerSpanEncoder(), statServerReporter);

        tracer = new SofaTracer.Builder("SofaTracerDemoTest").withTag("tracer", "SofaTracer")
            .withClientReporter(clientDigestReporter).withServerReporter(serverDigestReporter)
            .build();
    }

    @BeforeClass
    public static void before() throws IOException {
        System.setProperty("com.alipay.ldc.zone", "GZ00A");
        String directoryCleaned = System.getProperty("DIRECTORY_CLEANED");
        if (directoryCleaned == null) {
            cleanLogDirectory();
            System.setProperty("DIRECTORY_CLEANED", "true");
        }
    }

    @After
    public void after() throws Exception {
        checkSelfLogContainsError();
        clearConfig();
    }

    public void clearConfig() {
        SofaTracerConfiguration.setProperty(
            SofaTracerConfiguration.DISABLE_MIDDLEWARE_DIGEST_LOG_KEY, "false");
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.DISABLE_DIGEST_LOG_KEY,
            new HashMap<String, String>());
    }

    /**
     * 清理日志文件夹
     *
     * @throws IOException
     */
    public static void cleanLogDirectory() throws IOException {
        if (!logDirectory.exists()) {
            return;
        }

        FileUtils.cleanDirectory(logDirectory);
    }

    /**
     * 检查 Tracer 本身是否含有错误
     */
    protected void checkSelfLogContainsError() throws IOException {
        File tracerSelfLog = new File(logDirectory + File.separator + "tracer-self.log");

        if (!tracerSelfLog.exists()) {
            return;
        }

        String selfLogContent = FileUtils.readFileToString(tracerSelfLog);
        boolean result = (selfLogContent == null || !selfLogContent.contains("ERROR"));
        Assert.assertTrue("Tracer 中包含错误" + selfLogContent, result);
    }

    protected static File customFileLog(String fileName) {
        return new File(logDirectoryPath + File.separator + fileName);
    }

    public SofaTracerSpan recoverServerSpan(String serverSpanId) {
        //假设反序列化回的信息
        //生成 traceId
        String traceId = TraceIdGenerator.generate();
        //默认不采样
        SofaTracerSpanContext spanContext = new SofaTracerSpanContext(traceId, serverSpanId,
            StringUtils.EMPTY_STRING, true);

        String callServiceName = "callServiceName";
        //create server
        SofaTracerSpan serverSpan = new SofaTracerSpan(tracer, System.currentTimeMillis(),
            callServiceName, spanContext, null);
        serverSpan.setTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
        return serverSpan;
    }

    public SofaTracerSpan createClientSpan() {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        //pop
        SofaTracerSpan serverSpan = sofaTraceContext.pop();
        SofaTracer.SofaTracerSpanBuilder sofaTracerSpanBuilder = (SofaTracer.SofaTracerSpanBuilder) tracer
            .buildSpan("callService").asChildOf(serverSpan != null ? serverSpan.context() : null)
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
        SofaTracerSpan clientSpan = (SofaTracerSpan) sofaTracerSpanBuilder.start();
        clientSpan.setParentSofaTracerSpan(serverSpan);
        //push
        sofaTraceContext.push(clientSpan);
        return clientSpan;
    }

}
