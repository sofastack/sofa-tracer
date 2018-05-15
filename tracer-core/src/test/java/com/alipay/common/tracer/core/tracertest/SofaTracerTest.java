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
package com.alipay.common.tracer.core.tracertest;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.base.AbstractTestBase;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.reporter.digest.DiskReporterImpl;
import com.alipay.common.tracer.core.reporter.facade.Reporter;
import com.alipay.common.tracer.core.samplers.Sampler;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracertest.encoder.ClientSpanEncoder;
import com.alipay.common.tracer.core.tracertest.encoder.ServerSpanEncoder;
import com.alipay.common.tracer.core.tracertest.type.TracerTestLogEnum;
import com.alipay.common.tracer.core.utils.StringUtils;
import io.opentracing.References;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.StringTag;
import io.opentracing.tag.Tags;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * SofaTracer Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>七月 1, 2017</pre>
 */
public class SofaTracerTest extends AbstractTestBase {

    private final String tracerType           = "TracerTestService";

    private SofaTracer   sofaTracer;

    private final String tracerGlobalTagKey   = "tracerkey";

    private final String tracerGlobalTagValue = "tracervalue";

    @Before
    public void beforeInstance() throws IOException {

        //client
        DiskReporterImpl clientReporter = new DiskReporterImpl(
            TracerTestLogEnum.RPC_CLIENT.getDefaultLogName(), new ClientSpanEncoder());

        //server
        DiskReporterImpl serverReporter = new DiskReporterImpl(
            TracerTestLogEnum.RPC_SERVER.getDefaultLogName(), new ServerSpanEncoder());
        sofaTracer = new SofaTracer.Builder(tracerType).withTag("tracer", "tracertest")
            .withClientReporter(clientReporter).withServerReporter(serverReporter)
            .withTag(tracerGlobalTagKey, tracerGlobalTagValue).build();
    }

    /**
     * Method: buildSpan(String operationName)
     */
    @Test
    public void testBuildSpan() throws Exception {
        String expectedOperation = "operation";
        SofaTracerSpan sofaTracerSpan = (SofaTracerSpan) this.sofaTracer.buildSpan(
            expectedOperation).start();
        assertEquals(expectedOperation, sofaTracerSpan.getOperationName());
    }

    /**
     * Method: inject(SpanContext spanContext, Format<C> format, C carrier)
     */
    @Test
    public void testInject() throws Exception {
        SofaTracerSpan span = (SofaTracerSpan) this.sofaTracer.buildSpan("testInjectSpan").start();
        TextMap carrier = new TextMap() {

            Map map = new HashMap();

            @Override
            public Iterator<Map.Entry<String, String>> iterator() {
                return map.entrySet().iterator();
            }

            @Override
            public void put(String key, String value) {
                map.put(key, value);
            }
        };
        SofaTracerSpanContext originContext = (SofaTracerSpanContext) span.context();
        assertTrue(StringUtils.isBlank(originContext.getParentId()));
        this.sofaTracer.inject(originContext, Format.Builtin.TEXT_MAP, carrier);

        SofaTracerSpanContext extractSpanContext = (SofaTracerSpanContext) this.sofaTracer.extract(
            Format.Builtin.TEXT_MAP, carrier);
        assertTrue("\nOrigin Context : " + originContext.toString(),
            StringUtils.isBlank(extractSpanContext.getParentId()));
        assertTrue("Extract Context : " + extractSpanContext,
            originContext.equals(extractSpanContext));
    }

    /**
     * Method: reportSpan(SofaTracerSpan span)
     */
    @Test
    public void testReportSpan() throws Exception {
        SofaTracerSpan span = (SofaTracerSpan) this.sofaTracer.buildSpan("testInjectSpan")
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();
        //report 不要禁写
        span.finish();
        //验证摘要日志
        TimeUnit.SECONDS.sleep(3);
        List<String> contents = FileUtils.readLines(new File(logDirectoryPath
                                                             + File.separator
                                                             + TracerTestLogEnum.RPC_CLIENT
                                                                 .getDefaultLogName()));
        assertTrue(contents.get(0), contents.size() == 1);
        String contextStr = contents.get(0);
        //测试打印一条只放了一个 tags
        assertTrue(contextStr.contains(Tags.SPAN_KIND.getKey())
                   && contextStr.contains(Tags.SPAN_KIND_CLIENT));
    }

    /**
     * Method: isDisableDigestLog(SofaTracerSpan span)
     */
    @Test
    public void testIsDisableAllDigestLog() throws Exception {
        //全局关闭摘要日志
        SofaTracerConfiguration.setProperty(
            SofaTracerConfiguration.DISABLE_MIDDLEWARE_DIGEST_LOG_KEY, "true");
        SofaTracerSpan span = (SofaTracerSpan) this.sofaTracer.buildSpan("testInjectSpan")
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();
        //report
        span.finish();
        //验证摘要日志
        TimeUnit.SECONDS.sleep(1);

        boolean isException = false;
        try {
            List<String> contents = FileUtils.readLines(new File(logDirectoryPath
                                                                 + File.separator
                                                                 + TracerTestLogEnum.RPC_CLIENT
                                                                     .getDefaultLogName()));
        } catch (FileNotFoundException exception) {
            isException = true;
        }
        assertTrue(isException);
    }

    @Test
    public void testIsDisableClientDigestLog() throws Exception {
        //关闭client摘要日志
        String clientLogTypeName = TracerTestLogEnum.RPC_CLIENT.getDefaultLogName();

        Map<String, String> prop = new HashMap<String, String>();
        prop.put(clientLogTypeName, "true");
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.DISABLE_DIGEST_LOG_KEY, prop);
        //create
        SofaTracerSpan span = (SofaTracerSpan) this.sofaTracer.buildSpan("testInjectSpan")
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();
        //report
        span.finish();
        //验证摘要日志
        TimeUnit.SECONDS.sleep(1);

        boolean isException = false;
        try {
            List<String> contents = FileUtils.readLines(new File(logDirectoryPath + File.separator
                                                                 + clientLogTypeName));
        } catch (FileNotFoundException exception) {
            isException = true;
        }
        assertTrue(isException);
    }

    /**
     * Method: close()
     */
    @Test
    public void testClose() throws Exception {
        //create
        SofaTracerSpan span = (SofaTracerSpan) this.sofaTracer.buildSpan("testClose")
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();
        this.sofaTracer.close();
        //report
        span.finish();
        //验证摘要日志
        TimeUnit.SECONDS.sleep(1);
        //关闭client摘要日志
        String clientLogTypeName = TracerTestLogEnum.RPC_CLIENT.getDefaultLogName();
        boolean isException = false;
        try {
            List<String> contents = FileUtils.readLines(new File(logDirectoryPath + File.separator
                                                                 + clientLogTypeName));
        } catch (FileNotFoundException exception) {
            isException = true;
        }
        assertTrue(isException);
    }

    @Test
    public void testTracerClose() throws Exception {
        Reporter reporter = mock(Reporter.class);
        Sampler sampler = mock(Sampler.class);
        SofaTracer sofaTracer = new SofaTracer.Builder(tracerType).withClientReporter(reporter)
            .withSampler(sampler).build();

        sofaTracer.close();
        //确认被调用
        verify(reporter).close();
        verify(sampler).close();
    }

    /**
     * Method: getTracerType()
     */
    @Test
    public void testGetTracerType() throws Exception {
        String tracerType = this.sofaTracer.getTracerType();
        assertTrue(tracerType.equals(this.tracerType));
    }

    /**
     * Method: getClientSofaTracerDigestReporter()
     */
    @Test
    public void testGetSofaTracerDigestReporter() throws Exception {
        assertTrue(this.sofaTracer.getClientReporter() != null);
    }

    /**
     * Method: getClientSofaTracerStatisticReporter()
     */
    @Test
    public void testGetSofaTracerStatisticReporter() throws Exception {
        assertTrue(this.sofaTracer.getClientReporter() != null);
        assertTrue(this.sofaTracer.getClientReporter() instanceof DiskReporterImpl);
        DiskReporterImpl clientReporter = (DiskReporterImpl) this.sofaTracer.getClientReporter();
        assertTrue(StringUtils.isBlank(clientReporter.getStatReporterType()));
        assertTrue(clientReporter.getStatReporter() == null);
    }

    /**
     * Method: getTracerTags()
     */
    @Test
    public void testGetTracerTags() throws Exception {
        Map<String, Object> tags = this.sofaTracer.getTracerTags();
        assertTrue(tags.keySet().contains(this.tracerGlobalTagKey));
        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (this.tracerGlobalTagKey.equals(key)) {
                assertTrue("tracer tags : key=" + key + ",value = " + value,
                    this.tracerGlobalTagValue.equals(value));
            }
        }
    }

    /**
     * Method: asChildOf(SpanContext parent)
     */
    @Test
    public void testAsChildOfParent() throws Exception {
        //create
        Map<String, String> bizBaggage = new HashMap<String, String>();
        bizBaggage.put("biz", "value");
        bizBaggage.put("biz1", "value1");
        bizBaggage.put("biz2", "value2");
        Map<String, String> sysBaggage = new HashMap<String, String>();
        sysBaggage.put("sys", "value");
        sysBaggage.put("sys1", "value1");
        sysBaggage.put("sys2", "value2");
        SofaTracerSpan spanParent = (SofaTracerSpan) this.sofaTracer.buildSpan("spanParent")
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();
        spanParent.getSofaTracerSpanContext().addBizBaggage(bizBaggage);
        spanParent.getSofaTracerSpanContext().addSysBaggage(sysBaggage);
        String parentTraceId = spanParent.getSofaTracerSpanContext().getTraceId();
        SofaTracerSpanContext parentSpanContext = (SofaTracerSpanContext) spanParent.context();
        assertTrue("\nroot spanId : " + parentSpanContext.getSpanId(), parentSpanContext
            .getSpanId().equals(SofaTracer.ROOT_SPAN_ID));
        //child
        SofaTracerSpan spanChild = (SofaTracerSpan) this.sofaTracer.buildSpan("spanChild")
            .asChildOf(spanParent).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();
        SofaTracerSpanContext childContext = spanChild.getSofaTracerSpanContext();
        String childTraceId = childContext.getTraceId();

        String childSpanId = childContext.getSpanId();
        String[] childArray = childSpanId.split("\\.");
        assertEquals("child spanId : " + childSpanId, 2, childArray.length);
        assertEquals(SofaTracer.ROOT_SPAN_ID, childArray[0]);
        assertEquals("Traceid : " + parentTraceId, parentTraceId, childTraceId);
        //baggage
        assertEquals(bizBaggage, childContext.getBizBaggage());
        assertEquals(
            "Biz : " + childContext.getBizBaggage() + ",Sys : " + childContext.getSysBaggage(),
            sysBaggage, childContext.getSysBaggage());
    }

    /**
     * Method: asChildOf(SpanContext parent)
     */
    @Test
    public void testAsChildOfParentTestBizBaggageAndSysBaggage() throws Exception {
        //create
        SofaTracerSpan spanParent = (SofaTracerSpan) this.sofaTracer.buildSpan("spanParent")
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();
        String parentTraceId = spanParent.getSofaTracerSpanContext().getTraceId();
        SofaTracerSpanContext parentSpanContext = (SofaTracerSpanContext) spanParent.context();
        assertTrue(parentSpanContext.getSpanId().equals(SofaTracer.ROOT_SPAN_ID));
        //child
        SofaTracerSpan spanChild = (SofaTracerSpan) this.sofaTracer.buildSpan("spanChild")
            .asChildOf(spanParent).withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();
        String childTraceId = spanChild.getSofaTracerSpanContext().getTraceId();
        String childSpanId = spanChild.getSofaTracerSpanContext().getSpanId();
        String[] childArray = childSpanId.split("\\.");
        assertEquals("\nroot spanId : " + parentSpanContext.getSpanId(), 2, childArray.length);
        assertEquals("child spanId : " + childSpanId, SofaTracer.ROOT_SPAN_ID, childArray[0]);
        assertEquals("Traceid : " + parentTraceId, parentTraceId, childTraceId);
    }

    /**
     * Method: asChildOf(Span parentSpan)
     * 多个的时候，baggage 复用 只选择第一个父亲
     */
    @Test
    public void testAsChildOfMultiParentSpan() throws Exception {
        //create
        SofaTracerSpan spanParent = (SofaTracerSpan) this.sofaTracer.buildSpan("spanParent")
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();
        String parentTraceID = spanParent.getSofaTracerSpanContext().getTraceId();
        String parentSpanId = spanParent.getSofaTracerSpanContext().getSpanId();
        //follow
        SofaTracerSpan spanFollow = (SofaTracerSpan) this.sofaTracer.buildSpan("spanFollow")
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();
        String followTraceId = spanFollow.getSofaTracerSpanContext().getTraceId();
        String followSpanId = spanFollow.getSofaTracerSpanContext().getSpanId();
        //parent1
        SofaTracerSpan spanParent1 = (SofaTracerSpan) this.sofaTracer.buildSpan("spanParent1")
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();
        String p1TraceId = spanParent1.getSofaTracerSpanContext().getTraceId();
        String p1SpanId = spanParent1.getSofaTracerSpanContext().getSpanId();
        //assert
        assertTrue("Parent1 --> TraceId : " + p1TraceId + ", spanId : " + p1SpanId,
            SofaTracer.ROOT_SPAN_ID.equals(parentSpanId) && parentSpanId.equals(followSpanId)
                    && followSpanId.equals(p1SpanId));
        assertFalse(parentTraceID.equals(followTraceId));
        assertFalse(followTraceId.equals(p1TraceId));
        //child
        SofaTracerSpan childSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("childFollow")
            //parent
            .addReference(References.CHILD_OF, spanParent.context())
            //follow
            .addReference(References.FOLLOWS_FROM, spanFollow.context())
            //parent1
            .addReference(References.CHILD_OF, spanParent1.context())
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();
        String childTraceid = childSpan.getSofaTracerSpanContext().getTraceId();
        String childSpanId = childSpan.getSofaTracerSpanContext().getSpanId();
        assertFalse("Child --> TraceId : " + childTraceid + ", spanId :" + childSpanId,
            p1TraceId.equals(childTraceid));
        //child context
        SofaTracerSpanContext sofaTracerSpanContext = childSpan.getSofaTracerSpanContext();
        assertTrue(childTraceid.equals(parentTraceID));
        //grandson
        SofaTracerSpan grandsonSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("grandson")
            //parent
            .addReference(References.CHILD_OF, childSpan.context())
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();
        String grandsonTraceid = grandsonSpan.getSofaTracerSpanContext().getTraceId();
        String grandsonSpanId = grandsonSpan.getSofaTracerSpanContext().getSpanId();
        //check traceId
        assertTrue("Grandson --> TraceId : " + grandsonTraceid + ", Grandson spanId :"
                   + grandsonSpanId, childTraceid.equals(parentTraceID));
        assertTrue(childTraceid.equals(grandsonTraceid));
        //check spanId
        assertTrue(grandsonSpan.getSofaTracerSpanContext().getParentId()
            .equals(childSpan.getSofaTracerSpanContext().getSpanId()));
        assertTrue(childSpan.getSofaTracerSpanContext().getParentId()
            .equals(spanParent.getSofaTracerSpanContext().getSpanId()));
    }

    /**
     * Method: addReference(String referenceType, SpanContext referencedContext)
     */
    @Test
    public void testAddReferenceForReferenceTypeReferencedContextAndBaggageMultipleReferences()
                                                                                               throws Exception {
        //create
        SofaTracerSpan spanParent = (SofaTracerSpan) this.sofaTracer
            .buildSpan(
                "testAddReferenceForReferenceTypeReferencedContextAndBaggageMultipleReferences")
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();
        //baggage
        String parBagKey = "parBagKey";
        String parBagValue = "parBagValue";
        spanParent.setBaggageItem(parBagKey, parBagValue);

        String parentTraceID = spanParent.getSofaTracerSpanContext().getTraceId();
        String parentSpanId = spanParent.getSofaTracerSpanContext().getSpanId();
        //follow
        SofaTracerSpan spanFollow1 = (SofaTracerSpan) this.sofaTracer.buildSpan("spanFollow1")
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();
        //baggage
        String fol1BagKey = "fol1BagKey";
        String fol1BagValue = "fol1BagValue";
        spanFollow1.setBaggageItem(fol1BagKey, fol1BagValue);

        String followTraceId1 = spanFollow1.getSofaTracerSpanContext().getTraceId();
        String followSpanId1 = spanFollow1.getSofaTracerSpanContext().getSpanId();
        //follow1
        SofaTracerSpan spanFollow2 = (SofaTracerSpan) this.sofaTracer.buildSpan("spanFollow2")
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();
        //baggage
        String fol2BagKey = "fol2BagKey";
        String fol2BagValue = "fol2BagValue";
        spanFollow2.setBaggageItem(fol2BagKey, fol2BagValue);
        String followTraceId2 = spanFollow2.getSofaTracerSpanContext().getTraceId();
        String followSpanId2 = spanFollow2.getSofaTracerSpanContext().getSpanId();
        //child
        SofaTracerSpan childSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("childSpan")
            //parent
            .addReference(References.CHILD_OF, spanParent.context())
            //follow1
            .addReference(References.FOLLOWS_FROM, spanFollow1.context())
            //follow2
            .addReference(References.FOLLOWS_FROM, spanFollow2.context())
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT).start();
        //check traceId
        assertTrue("Follow --> TraceId : " + followTraceId2 + ", spanId : " + followSpanId2,
            parentTraceID.equals(childSpan.getSofaTracerSpanContext().getTraceId()));
        //check spanId
        assertTrue(childSpan.getSofaTracerSpanContext().getParentId()
            .equals(spanParent.getSofaTracerSpanContext().getSpanId()));
        //baggage
        assertEquals("Child Baggage : " + childSpan.getSofaTracerSpanContext().getBizBaggage(),
            parBagValue, childSpan.getBaggageItem(parBagKey));
        assertEquals(fol1BagValue, childSpan.getBaggageItem(fol1BagKey));
        assertEquals(fol2BagValue, childSpan.getBaggageItem(fol2BagKey));
    }

    /**
     * Method: withTag(String key, String value)
     */
    @Test
    public void testWithTagForKeyValue() throws Exception {
        //create
        SofaTracerSpan spanParent = (SofaTracerSpan) this.sofaTracer
            .buildSpan("testWithTagForKeyValue")
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();
        //tags
        StringTag stringTag = new StringTag("tagkey");
        stringTag.set(spanParent, "tagvalue");
        //tags
        spanParent.setTag("tag1", "value");

        Map<String, String> tagsStr = spanParent.getTagsWithStr();
        //string
        assertTrue("tagsStr : " + tagsStr, tagsStr.keySet().contains("tagkey")
                                           && tagsStr.values().contains("tagvalue"));
        assertTrue(tagsStr.keySet().contains("tag1") && tagsStr.values().contains("value"));
        //bool
        spanParent.setTag("bool", Boolean.TRUE);
        spanParent.setTag("bool1", Boolean.FALSE);
        assertTrue(spanParent.getTagsWithBool().get("bool").equals(Boolean.TRUE));
        assertTrue(spanParent.getTagsWithBool().get("bool1").equals(Boolean.FALSE));
        //number
        spanParent.setTag("num1", new Integer(10));
        spanParent.setTag("num2", 20);
        spanParent.setTag("num3", 2.22);
        assertTrue(spanParent.getTagsWithNumber().get("num1").equals(10));
        assertTrue(spanParent.getTagsWithNumber().get("num2").equals(20));
        assertTrue(spanParent.getTagsWithNumber().get("num3").equals(2.22));
    }

    /**
     * Method: withStartTimestamp(long microseconds)
     */
    @Test
    public void testWithStartTimestampMicroseconds() throws Exception {
        long startTime = 111;
        //create
        SofaTracerSpan spanParent = (SofaTracerSpan) this.sofaTracer
            .buildSpan("testWithStartTimestampMicroseconds")
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).withStartTimestamp(startTime)
            .start();
        //
        assertEquals(startTime, spanParent.getStartTime());
    }

    /**
     * Method: withClientStatsReporter(SofaTracerDigestReporter sofaTracerDigestReporter)
     */
    @Test
    public void testWithStatsReporterSofaTracerDigestReporter() throws Exception {
        assertTrue(this.sofaTracer.getServerReporter() != null);
    }

    /**
     * Method: withSampler(Sampler sampler)
     */
    @Test
    public void testWithSampler() throws Exception {
        assertTrue(this.sofaTracer.getSampler() == null);
    }

}
