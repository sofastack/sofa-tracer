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
package com.alipay.common.tracer.core.utils;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.appender.config.LogReserveConfig;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * TracerUtils Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>July 2, 2017</pre>
 */
public class TracerUtilsTest {

    /**
     * Test removes the JSessionId_URL in the URL to null
     */
    @Test
    public void JSessionId_URL_is_null() {
        Assert.assertNull(TracerUtils.removeJSessionIdFromUrl(null));
    }

    /**
     * Test removes the JSessionId_URL in the URL to ""
     */
    @Test
    public void JSessionId_URL_EMPTY() {
        Assert.assertEquals(StringUtils.EMPTY_STRING,
            TracerUtils.removeJSessionIdFromUrl(StringUtils.EMPTY_STRING));
    }

    /**
     * The JSessionId_URL in the test removal URL does not contain the JSessionId
     */
    @Test
    public void JSessionId_URL_NO_JSessionId() {
        String url = "http://csmobile.alipay.com/progress/index.htm";
        Assert.assertEquals(url, TracerUtils.removeJSessionIdFromUrl(url));
    }

    /**
     * The JSessionId_URL in the test removal URL contain the JSessionId
     */
    @Test
    public void JSessionId_URL包含JSessionId() {
        String url = "http://csmobile.alipay.com/progress/index.htm";
        String jsessionId = ";jsessionid=1A530637289A03B07199A44E8D531427";
        Assert.assertEquals(url, TracerUtils.removeJSessionIdFromUrl(url + jsessionId));
    }

    /**
     * Method: getPID()
     */
    @Test
    public void testGetPID() throws Exception {
        String pid = TracerUtils.getPID();
        assertTrue(StringUtils.isNotBlank(pid));
    }

    @Test
    public void test_mapToString() {
        Map<String, String> strMap = new TreeMap<String, String>();
        strMap.put("test1", "test=");
        strMap.put("test2", "test&");
        String result = StringUtils.mapToString(strMap);
        Assert.assertEquals("test1=test" + XStringBuilder.EQUAL_SEPARATOR_ESCAPE + "&test2=test"
                            + XStringBuilder.AND_SEPARATOR_ESCAPE + "&", result);
    }

    @Test
    public void test_stringToMap() {
        Map<String, String> strMap = new HashMap<String, String>();
        StringUtils
            .stringToMap("test1=test" + XStringBuilder.EQUAL_SEPARATOR_ESCAPE + "&test2=test"
                         + XStringBuilder.AND_SEPARATOR_ESCAPE + "&", strMap);
        Assert.assertEquals("test=", strMap.get("test1"));
        Assert.assertEquals("test&", strMap.get("test2"));
    }

    /**
     * test_mapToString_stringToMap_contains_percent_sign
     */
    @Test
    public void mapToString_stringToMap_contains_percent_sign() {
        Map<String, String> strMap = new HashMap<>();
        String key = "test1";
        String value = "test" + XStringBuilder.AND_SEPARATOR_ESCAPE;
        strMap.put(key, value);
        String result = StringUtils.mapToString(strMap);
        Map<String, String> decodeMap = new HashMap<>();
        StringUtils.stringToMap(result, decodeMap);
        Assert.assertEquals(value, decodeMap.get(key));
    }

    /**
     * Test parsing log retention time configuration
     */
    @Test
    public void testParseLogRetentionTimeConfig() {
        LogReserveConfig logReserveConfig = TracerUtils.parseLogReserveConfig("0D1H");
        Assert.assertEquals(0, logReserveConfig.getDay());
        Assert.assertEquals(1, logReserveConfig.getHour());
    }

    /**
     * Test purely digital log retention time configuration
     */
    @Test
    public void testParseLogReserveConfigByNumber() {
        LogReserveConfig logReserveConfig = TracerUtils.parseLogReserveConfig("7");
        Assert.assertEquals(7, logReserveConfig.getDay());
        Assert.assertEquals(0, logReserveConfig.getHour());
    }

    @Test
    public void testCheckBaggageLength() {
        SofaTracer sofaTracer = mock(SofaTracer.class);
        SofaTracerSpanContext sofaTracerSpanContext = new SofaTracerSpanContext();
        SofaTracerSpan sofaTracerSpan = new SofaTracerSpan(sofaTracer, System.currentTimeMillis(),
            "mock", sofaTracerSpanContext, new HashMap<>());
        assertTrue(TracerUtils.checkBaggageLength(sofaTracerSpan, "key", "value"));
    }

    @Test
    public void testGetEmptyStringIfNull() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        assertEquals("value", TracerUtils.getEmptyStringIfNull(map, "key"));
        assertTrue(StringUtils.isBlank(TracerUtils.getEmptyStringIfNull(map, "key1")));
    }

    @Test
    public void testHostToHexString() {
        String str = "127.0.0.1";
        String hexString = TracerUtils.hostToHexString(str);
        assertTrue(hexString.length() >= 8);
    }

}
