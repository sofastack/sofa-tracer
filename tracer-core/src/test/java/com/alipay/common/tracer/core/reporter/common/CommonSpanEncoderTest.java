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
package com.alipay.common.tracer.core.reporter.common;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.TestUtil;
import com.alipay.common.tracer.core.base.AbstractTestBase;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.reporter.digest.DiskReporterImpl;
import com.alipay.common.tracer.core.reporter.type.TracerSystemLogEnum;
import com.alipay.common.tracer.core.samplers.SofaTracerPercentageBasedSampler;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tags.SpanTags;
import com.alipay.common.tracer.core.tracertest.encoder.ClientSpanEncoder;
import com.alipay.common.tracer.core.utils.StringUtils;
import io.opentracing.tag.Tags;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alipay.common.tracer.core.span.SofaTracerSpan.ARRAY_SEPARATOR;
import static org.junit.Assert.assertTrue;

/**
 * CommonSpanEncoder Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>七月 15, 2017</pre>
 */
public class CommonSpanEncoderTest extends AbstractTestBase {

    private SofaTracer sofaTracer;

    private String     clientLogType = "clientLog.log";

    private String     appName       = "appName";

    @Before
    public void setup() throws Exception {
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.SAMPLER_STRATEGY_NAME_KEY,
            SofaTracerPercentageBasedSampler.TYPE);
        SofaTracerConfiguration.setProperty(
            SofaTracerConfiguration.SAMPLER_STRATEGY_PERCENTAGE_KEY, "100");
        DiskReporterImpl clientDigestReporter = new DiskReporterImpl(clientLogType,
            new ClientSpanEncoder());
        sofaTracer = new SofaTracer.Builder("commonProfileTracerType")
            .withTag("tracer", "tracerTest").withClientReporter(clientDigestReporter).build();
    }

    /**
     * Method: encode(SofaTracerSpan span)
     */
    @Test
    public void testEncode() throws Exception {
        SofaTracerSpan sofaTracerSpan = (SofaTracerSpan) this.sofaTracer
            .buildSpan("spanOperationName").withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
            .withTag(SpanTags.CURR_APP_TAG.getKey(), appName).start();

        SofaTracerSpanContext sofaTracerSpanContext = sofaTracerSpan.getSofaTracerSpanContext();
        Exception exception = new RuntimeException("CommonSpanEncoderTest");
        String errorType = "timeout_error";
        String[] errorSources = new String[] { "trade", "rpc" };

        Map<String, String> context = new HashMap<String, String>();
        context.put("serviceName", "service");
        context.put("methodName", "methodCall");

        sofaTracerSpan.setBaggageItem("baggage", "value");
        sofaTracerSpan.setBaggageItem("baggage1", "value1");
        sofaTracerSpan.setBaggageItem("baggage2", "value2");

        //记录一条错误日志
        sofaTracerSpan.reportError(errorType, context, exception, appName, errorSources);
        //记录一条client 日志
        sofaTracerSpan.finish();

        TestUtil.waitForAsyncLog();

        //检查客户端日志
        //client digest
        List<String> clientDigestContents = FileUtils.readLines(new File(logDirectoryPath
                                                                         + File.separator
                                                                         + clientLogType));
        assertTrue(clientDigestContents.size() == 1);
        assertTrue(clientDigestContents.get(0).contains(sofaTracerSpanContext.getTraceId()));

        //error log
        List<String> errorContents = FileUtils
            .readLines(customFileLog(TracerSystemLogEnum.MIDDLEWARE_ERROR.getDefaultLogName()));
        //错误堆栈
        assertTrue(errorContents.size() > 1);
        assertTrue(errorContents.get(0).contains(sofaTracerSpanContext.getTraceId()));
        //去掉头
        String fileContent = errorContents.get(0).substring(errorContents.get(0).indexOf(",") + 1);
        //去掉尾巴
        fileContent = fileContent.substring(0, fileContent.lastIndexOf(","));
        //构造结果
        List<String> params = new ArrayList<String>();
        params.add(sofaTracerSpan.getTagsWithStr().get(SpanTags.CURR_APP_TAG.getKey()));
        params.add(sofaTracerSpanContext.getTraceId());
        params.add(sofaTracerSpanContext.getSpanId());
        params.add(Thread.currentThread().getName());
        params.add(errorType);
        params.add(StringUtils.arrayToString(errorSources, ARRAY_SEPARATOR, "", ""));
        params.add(MAP_PREFIX + StringUtils.mapToString(context));
        params.add(MAP_PREFIX + StringUtils.mapToString(sofaTracerSpanContext.getBizBaggage()));

        Assert.assertTrue("Content Checkout error", checkResult(params, fileContent));
    }

}
