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

import com.alipay.common.tracer.core.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * XStringBuilder Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>七月 12, 2017</pre>
 */
public class XStringBuilderTest {

    /***
     * 测试_appendEndMap_逗号被转义
     */
    @Test
    public void appendEndMapTransferredMeaning() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("test", "test" + XStringBuilder.DEFAULT_SEPARATOR);
        String converted = new XStringBuilder().appendEnd(map).toString();
        assertEquals("test=test" + XStringBuilder.DEFAULT_SEPARATOR_ESCAPE
                     + XStringBuilder.AND_SEPARATOR + StringUtils.NEWLINE, converted);
    }

    @Test
    public void testXStringBuilderAppender() {
        XStringBuilder xStringBuilder = new XStringBuilder(128);
        xStringBuilder.append(1l, ",");
        xStringBuilder.append(1l, ',');
        xStringBuilder.append("test1", ",");

        Map<String, String> map = new HashMap<String, String>();
        xStringBuilder.append(1);
        xStringBuilder.append(',');
        xStringBuilder.append(map);
        //after append start with newline
        xStringBuilder.appendEnd(1);
        xStringBuilder.appendEnd(',');
        xStringBuilder.appendEnd(1l);
        xStringBuilder.appendEscape("test2");
        XStringBuilder append = xStringBuilder.appendEscapeRaw("test3");
        Assert.assertEquals("1,1,test1,1,,,,1\r\n,\r\n1\r\ntest2,test3", append.toString());
    }
}
