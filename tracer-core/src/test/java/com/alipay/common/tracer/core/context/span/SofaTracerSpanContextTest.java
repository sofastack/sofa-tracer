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
package com.alipay.common.tracer.core.context.span;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.utils.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * SofaTracerSpanContext Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>七月 8, 2017</pre>
 */
public class SofaTracerSpanContextTest {

    private SofaTracerSpanContext sofaTracerSpanContext;

    @Before
    public void before() throws Exception {
        sofaTracerSpanContext = new SofaTracerSpanContext("traceId", "spanId", "parentId", false);
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testClone() throws Exception {
        SofaTracerSpanContext context = new SofaTracerSpanContext("traceId111", "spanId111",
            "parentId111", false);
        Map<String, String> bizBaggage = new HashMap<String, String>();
        bizBaggage.put("biz", "value");
        bizBaggage.put("biz1", "value1");
        context.addBizBaggage(bizBaggage);
        Map<String, String> sysBaggage = new HashMap<String, String>();
        sysBaggage.put("sys", "value");
        sysBaggage.put("sys1", "value1");
        context.addSysBaggage(sysBaggage);
        SofaTracerSpanContext cloneContext = context.cloneInstance();

        assertEquals(context, cloneContext);
        assertEquals(bizBaggage, cloneContext.getBizBaggage());
        assertEquals(sysBaggage, cloneContext.getSysBaggage());
        assertEquals(context.isSampled(), cloneContext.isSampled());
        assertSame(context.getChildContextIndex(), cloneContext.getChildContextIndex());
    }

    @Test
    public void testSpanContextConstruct() throws Exception {
        String traceId = "1234455";
        String spanId = "0.1.1";
        SofaTracerSpanContext spanContext = new SofaTracerSpanContext(traceId, spanId);
        assertEquals(traceId, spanContext.getTraceId());
        assertEquals(spanId, spanContext.getSpanId());
        assertEquals(spanId.substring(0, spanId.lastIndexOf(".")), spanContext.getParentId());
        //
        String traceId1 = "1234455";
        String spanId1 = "0";
        SofaTracerSpanContext spanContext1 = new SofaTracerSpanContext(traceId1, spanId1);
        assertEquals(traceId1, spanContext1.getTraceId());
        assertEquals(spanId1, spanContext1.getSpanId());
        assertEquals("", spanContext1.getParentId());
    }

    /**
     * Method: serializeSpanContext()
     */
    @Test
    public void testSerializeSpanContext() throws Exception {

        SofaTracerSpanContext sofaTracerSpanContext1 = new SofaTracerSpanContext("traceId",
            "spanId", "parentId", false);
        String serialized = sofaTracerSpanContext1.serializeSpanContext();
        SofaTracerSpanContext deserialized = SofaTracerSpanContext
            .deserializeFromString(serialized);
        assertEquals("traceId", deserialized.getTraceId());
        assertEquals("spanId", deserialized.getSpanId());
        assertEquals("parentId", deserialized.getParentId());
        assertFalse(deserialized.isSampled());
    }

    /**
     * Method: serializeSpanContext() sysBaggage bizBaggage
     */
    @Test
    public void testSerializeSpanContextBizSysBaggage() throws Exception {

        Map<String, String> bizBaggage = new HashMap<String, String>();
        bizBaggage.put("key", "value");
        bizBaggage.put("key1", "value1");
        Map<String, String> sysBaggage = new HashMap<String, String>();
        sysBaggage.put("sys1", "value1");
        sysBaggage.put("sys2", "value2");
        SofaTracerSpanContext sofaTracerSpanContext = new SofaTracerSpanContext("traceId",
            "spanId", "parentId", false);
        sofaTracerSpanContext.addBizBaggage(bizBaggage);
        sofaTracerSpanContext.addSysBaggage(sysBaggage);
        //
        String serialized = sofaTracerSpanContext.serializeSpanContext();
        SofaTracerSpanContext deserialized = SofaTracerSpanContext
            .deserializeFromString(serialized);
        assertEquals("traceId", deserialized.getTraceId());
        assertEquals("spanId", deserialized.getSpanId());
        assertEquals("parentId", deserialized.getParentId());
        assertFalse(deserialized.isSampled());
        //biz
        assertEquals(bizBaggage, deserialized.getBizBaggage());
        //sys
        assertEquals(sysBaggage, deserialized.getSysBaggage());
    }

    /**
     * Method: serializeSpanContext()
     */
    @Test
    public void testSerializeSpanContextWithBaggage() throws Exception {
        SofaTracerSpanContext sofaTracerSpanContext1 = new SofaTracerSpanContext("traceId",
            "spanId", "parentId", false);
        Map<String, String> baggage = new HashMap<String, String>();
        baggage.put("key", "value");
        baggage.put("key1", "value1");
        baggage.put("key2", "value2");
        baggage.put("key3", "value3");
        baggage.put("key4", "value4");
        sofaTracerSpanContext1.addBizBaggage(baggage);
        String serialized = sofaTracerSpanContext1.serializeSpanContext();
        //de
        SofaTracerSpanContext deSofaTracerSpanContext = SofaTracerSpanContext
            .deserializeFromString(serialized);
        //traceid spanId parentId
        assertTrue("\nserialized : " + serialized,
            deSofaTracerSpanContext.equals(sofaTracerSpanContext1));
        assertTrue(deSofaTracerSpanContext.isSampled() == sofaTracerSpanContext1.isSampled());
        //
        assertEquals(sofaTracerSpanContext1.getBizBaggage(),
            deSofaTracerSpanContext.getBizBaggage());
    }

    /**
     * Method: getBizSerializedBaggage()
     */
    @Test
    public void testGetSerializedBaggage() {

        SofaTracerSpanContext sofaTracerSpanContext = new SofaTracerSpanContext("traceId",
            "spanId", "parentId", false);
        Map<String, String> baggage = new HashMap<String, String>();
        baggage.put("key", "value");
        baggage.put("key1", "value1");
        baggage.put("key2", "value2");
        baggage.put("key3", "value3");
        baggage.put("key4", "value4");
        sofaTracerSpanContext.addBizBaggage(baggage);

        String stringBaggage = sofaTracerSpanContext.getBizSerializedBaggage();
        SofaTracerSpanContext sofaTracerSpanContext1 = new SofaTracerSpanContext("11", "11", "11",
            false);
        sofaTracerSpanContext1.deserializeBizBaggage(stringBaggage);
        assertEquals("\n" + stringBaggage, baggage, sofaTracerSpanContext1.getBizBaggage());
    }

    /**
     * Method: getSysSerializedBaggage()
     */
    @Test
    public void testGetSysSerializedBaggage() {

        SofaTracerSpanContext sofaTracerSpanContext = new SofaTracerSpanContext("traceId",
            "spanId", "parentId", false);
        Map<String, String> sysBaggage = new HashMap<String, String>();
        sysBaggage.put("key", "value");
        sysBaggage.put("key1", "value1");
        sysBaggage.put("key2", "value2");
        sysBaggage.put("key3", "value3");
        sysBaggage.put("key4", "value4");
        sofaTracerSpanContext.addSysBaggage(sysBaggage);

        String stringBaggage = sofaTracerSpanContext.getSysSerializedBaggage();

        SofaTracerSpanContext sofaTracerSpanContext1 = new SofaTracerSpanContext("11", "11", "11",
            false);
        sofaTracerSpanContext1.deserializeSysBaggage(stringBaggage);
        assertEquals("\n" + stringBaggage, sysBaggage, sofaTracerSpanContext1.getSysBaggage());
    }

    /**
     * Method: addBizBaggage(Map<String, String> newBaggage)
     */
    @Test
    public void testAddBaggage() throws Exception {
        SofaTracerSpanContext sofaTracerSpanContext1 = new SofaTracerSpanContext("traceId",
            "spanId", "parentId", false);
        sofaTracerSpanContext1.setBizBaggageItem("baggage1", "value1");
        sofaTracerSpanContext1.setBizBaggageItem("baggage2", "value2");
        //
        assertEquals("parentId", sofaTracerSpanContext1.getParentId());
        Map<String, String> baggage = sofaTracerSpanContext1.getBizBaggage();
        assertTrue(baggage.size() == 2);
        assertTrue(baggage.containsKey("baggage1") && baggage.containsValue("value1"));
        assertTrue(baggage.containsKey("baggage2") && baggage.containsValue("value2"));
    }

    /**
     * Method: addBizBaggage(Map<String, String> newBaggage)
     */
    @Test
    public void testAddSysBaggage() throws Exception {
        SofaTracerSpanContext sofaTracerSpanContext1 = new SofaTracerSpanContext("traceId",
            "spanId", "parentId", false);
        sofaTracerSpanContext1.setSysBaggageItem("baggage1", "value1");
        sofaTracerSpanContext1.setSysBaggageItem("baggage2", "value2");
        //
        assertEquals("parentId", sofaTracerSpanContext1.getParentId());
        Map<String, String> baggage = sofaTracerSpanContext1.getSysBaggage();
        assertTrue(baggage.size() == 2);
        assertTrue(baggage.containsKey("baggage1") && baggage.containsValue("value1"));
        assertTrue(baggage.containsKey("baggage2") && baggage.containsValue("value2"));
    }

    /**
     * Method: baggageItems() biz sys
     */
    @Test
    public void testBaggageItems() throws Exception {
        SofaTracerSpanContext sofaTracerSpanContext1 = new SofaTracerSpanContext("traceId",
            "spanId", "parentId", false);
        Map<String, String> expected = new HashMap<String, String>();
        sofaTracerSpanContext1.setBizBaggageItem("baggage1", "value1");
        sofaTracerSpanContext1.setBizBaggageItem("baggage2", "value2");
        sofaTracerSpanContext1.setSysBaggageItem("sys1", "sysvalue1");
        sofaTracerSpanContext1.setSysBaggageItem("sys2", "sysvalue2");
        expected.put("baggage1", "value1");
        expected.put("baggage2", "value2");
        expected.put("sys1", "sysvalue1");
        expected.put("sys2", "sysvalue2");

        Iterable<Map.Entry<String, String>> entrySet = sofaTracerSpanContext1.baggageItems();
        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : entrySet) {
            map.put(entry.getKey(), entry.getValue());
        }
        assertEquals(4, map.size());
        assertEquals(expected, map);
    }

    /**
     * Method: rootStart()
     */
    @Test
    public void testRootStart() throws Exception {
        SofaTracerSpanContext sofaTracerSpanContext = SofaTracerSpanContext.rootStart();
        assertEquals(SofaTracer.ROOT_SPAN_ID, sofaTracerSpanContext.getSpanId());
        assertTrue(StringUtils.isBlank(sofaTracerSpanContext.getParentId()));
        assertTrue(!sofaTracerSpanContext.isSampled());
    }

    /**
     * Method: getBizBaggageItem(String key)
     */
    @Test
    public void testGetBaggageItem() throws Exception {
        SofaTracerSpanContext sofaTracerSpanContext1 = new SofaTracerSpanContext("traceId",
            "spanId", "parentId", false);
        sofaTracerSpanContext1.setBizBaggageItem("key", "value");
        String value = sofaTracerSpanContext1.getBizBaggageItem("key");
        assertEquals("value", value);
    }

    /**
     * Method: nextChildContextId()
     */
    @Test
    public void testNextChildContextId() throws Exception {
        SofaTracerSpanContext sofaTracerSpanContext = SofaTracerSpanContext.rootStart();
        String childSpanId = sofaTracerSpanContext.nextChildContextId();
        String childSpanId1 = sofaTracerSpanContext.nextChildContextId();
        String childSpanId2 = sofaTracerSpanContext.nextChildContextId();
        String childSpanId3 = sofaTracerSpanContext.nextChildContextId();

        assertEquals(SofaTracer.ROOT_SPAN_ID, sofaTracerSpanContext.getSpanId());
        //
        assertEquals(2, childSpanId.split("\\.").length);
        assertEquals(2, childSpanId1.split("\\.").length);
        assertEquals(2, childSpanId2.split("\\.").length);
        assertEquals(2, childSpanId3.split("\\.").length);
        String[] childs = childSpanId.split("\\.");
        String[] childs1 = childSpanId1.split("\\.");
        String[] childs2 = childSpanId2.split("\\.");
        String[] childs3 = childSpanId3.split("\\.");
        assertTrue(SofaTracer.ROOT_SPAN_ID.equals(childs[0]));
        assertTrue(SofaTracer.ROOT_SPAN_ID.equals(childs1[0]));
        assertTrue(SofaTracer.ROOT_SPAN_ID.equals(childs2[0]));
        assertTrue(SofaTracer.ROOT_SPAN_ID.equals(childs3[0]));
        //
        assertTrue("Children : " + childs[1] + "," + childs1[1] + "," + childs2[1] + ","
                   + childs3[1], sofaTracerSpanContext.lastChildContextId().equals("0.4"));
    }

    /**
     * Method: toString()
     */
    @Test
    public void testToString() throws Exception {
        SofaTracerSpanContext sofaTracerSpanContext = SofaTracerSpanContext.rootStart();
        String str = sofaTracerSpanContext.toString();
        assertTrue(str, StringUtils.isNotBlank(str));
    }
}
