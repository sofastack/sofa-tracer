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
package com.alipay.common.tracer.core.appender.builder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.common.tracer.core.utils.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * JsonStringBuilder Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>五月 14, 2018</pre>
 */
public class JsonStringBuilderTest {

    /**
     * Method: append(String key, String value)
     */
    @Test
    public void testAppendKeyValue() throws Exception {
        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder();
        jsonStringBuilder.appendBegin();
        jsonStringBuilder.append("key1", "value1");
        jsonStringBuilder.append("key2", "value2");
        jsonStringBuilder.append("key3", "value3");
        jsonStringBuilder.append("key4", "value4");
        jsonStringBuilder.appendEnd("keyend", "valueend");
        String value = jsonStringBuilder.toString();
        JSONObject jsonObject = JSON.parseObject(value);
        assertEquals("value1", jsonObject.get("key1"));
        assertEquals(5, jsonObject.size());
        jsonStringBuilder.reset();
        assertTrue(StringUtils.isBlank(jsonStringBuilder.toString()));
        //
        jsonStringBuilder.appendBegin();
        jsonStringBuilder.append(null, (String) null);
        jsonStringBuilder.append(null, (Number) null);
        jsonStringBuilder.append(null, (String) null);
        jsonStringBuilder.appendEnd();
        String logValue = jsonStringBuilder.toString();
        JSONObject jsonObjectLog = JSON.parseObject(logValue);
        assertTrue(jsonObjectLog.size() > 0);
        //
        jsonStringBuilder.reset();
        assertTrue(StringUtils.isBlank(jsonStringBuilder.toString()));
        jsonStringBuilder.appendBegin();
        jsonStringBuilder.append("key1", "value1");
        jsonStringBuilder.appendEnd("keyend", (String) null);
        String logValue1 = jsonStringBuilder.toString();
        JSONObject jsonObjectLog1 = JSON.parseObject(logValue1);
        assertEquals(2, jsonObjectLog1.size());
        //
        jsonStringBuilder.reset();
        jsonStringBuilder.appendBegin("key", "value");
        jsonStringBuilder.append("key1", "value1");
        jsonStringBuilder.appendEnd("end", "end");
        String logValue2 = jsonStringBuilder.toString();
        JSONObject jsonObjectLog2 = JSON.parseObject(logValue2);
        assertEquals(3, jsonObjectLog2.size());

    }

    /**
     * Method: append(String key, String value)
     */
    @Test
    public void testAppendValueCheck() throws Exception {
        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder(true);
        jsonStringBuilder.appendBegin();
        jsonStringBuilder.appendEnd();
        String value = jsonStringBuilder.toString();
        assertEquals("{}" + StringUtils.NEWLINE, value);
        //
        jsonStringBuilder.reset();
        jsonStringBuilder.appendBegin();
        jsonStringBuilder.append("key1", "value1");
        jsonStringBuilder.append("key2", "value2");
        jsonStringBuilder.append("key3", "value3");
        jsonStringBuilder.appendEnd();
        String logValue = jsonStringBuilder.toString();
        JSONObject jsonObjectLog = JSON.parseObject(logValue);
        assertEquals(3, jsonObjectLog.size());
        //
        jsonStringBuilder.reset();
        jsonStringBuilder.appendBegin();
        jsonStringBuilder.append("key", (String) null);
        jsonStringBuilder.append("key1", (String) null);
        jsonStringBuilder.append("key", (String) null);
        jsonStringBuilder.appendEnd("key3", "value3");
        String logValue1 = jsonStringBuilder.toString();
        JSONObject jsonObjectLog1 = JSON.parseObject(logValue1);
        assertEquals(1, jsonObjectLog1.size());
        //
        jsonStringBuilder.reset();
        jsonStringBuilder.appendBegin();
        jsonStringBuilder.append("key", (Number) null);
        jsonStringBuilder.append("key1", (String) null);
        jsonStringBuilder.appendEnd("key2", (Boolean) null);
        String logValue2 = jsonStringBuilder.toString();
        JSONObject jsonObjectLog2 = JSON.parseObject(logValue2);
        assertEquals(0, jsonObjectLog2.size());
    }

    @Test
    public void testAppendNewLineFalse() throws Exception {
        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder();
        jsonStringBuilder.appendBegin();
        jsonStringBuilder.append("key", "value");
        jsonStringBuilder.appendEnd("end", "end", false);
        assertFalse(jsonStringBuilder.toString().endsWith(StringUtils.NEWLINE));
        //
        jsonStringBuilder.reset();
        jsonStringBuilder.appendBegin("key", "value");
        jsonStringBuilder.appendEnd(false);
        assertFalse(jsonStringBuilder.toString().endsWith(StringUtils.NEWLINE));
    }

    @Test
    public void testAppendObject() throws Exception {
        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder();
        jsonStringBuilder.appendBegin();
        jsonStringBuilder.append("sub", "sub");
        jsonStringBuilder.append("su1", "sub1");
        jsonStringBuilder.appendEnd(false);
        String sub = jsonStringBuilder.toString();
        JSONObject subObject = JSON.parseObject(sub);
        assertEquals(2, subObject.size());
        assertFalse(sub.endsWith(StringUtils.NEWLINE));
        //
        JsonStringBuilder jsonStringBuilder1 = new JsonStringBuilder();
        jsonStringBuilder1.appendBegin();
        jsonStringBuilder1.append("key", "value");
        jsonStringBuilder1.append("key1", "value1");
        jsonStringBuilder1.append("key2", "value2");
        jsonStringBuilder1.appendEnd("child", sub);
        String jsonStr = jsonStringBuilder1.toString();
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        assertEquals(4, jsonObject.size());
    }

    @Test
    public void testAppendNumBoolean() throws Exception {
        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder();
        jsonStringBuilder.appendBegin().append("key", 10);
        jsonStringBuilder.append("key1", false);
        jsonStringBuilder.appendEnd("key2", "value2");
        JSONObject jsonObject = JSON.parseObject(jsonStringBuilder.toString());
        assertEquals(3, jsonObject.size());
        //
        jsonStringBuilder.reset();
        jsonStringBuilder.appendBegin("key", 10).append("key1", "value1").append("key2", false)
            .append("key3", "value3").appendEnd("key4", false);
        JSONObject jsonObject1 = JSON.parseObject(jsonStringBuilder.toString());
        assertEquals(5, jsonObject1.size());
        //
        jsonStringBuilder.reset();
        jsonStringBuilder.appendBegin("key", false).append("key1", "value1").append("key2", 10)
            .append("key3", true).appendEnd("key4", 10);
        JSONObject jsonObject2 = JSON.parseObject(jsonStringBuilder.toString());
        assertEquals(5, jsonObject2.size());
    }

    @Test
    public void testAppendSubObjectNewLineBoolean() throws Exception {
        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder();
        jsonStringBuilder.appendBegin().append("sub1", "value1");
        jsonStringBuilder.append("sub2", "value2");
        jsonStringBuilder.appendEnd("sub3", 3, false);
        String sub = jsonStringBuilder.toString();
        JSONObject subObject = JSON.parseObject(sub);
        assertEquals(3, subObject.size());
        assertFalse(sub.endsWith(StringUtils.NEWLINE));
        //
        JsonStringBuilder jsonStringBuilder1 = new JsonStringBuilder();
        jsonStringBuilder1.appendBegin();
        jsonStringBuilder1.append("key", "value");
        jsonStringBuilder1.append("key1", "value1");
        jsonStringBuilder1.append("key2", "value2");
        jsonStringBuilder1.appendEnd("child", sub);
        String jsonStr = jsonStringBuilder1.toString();
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        assertEquals(4, jsonObject.size());
    }
}
