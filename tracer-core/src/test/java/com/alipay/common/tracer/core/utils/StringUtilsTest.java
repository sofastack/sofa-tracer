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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * StringUtils Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>五月 15, 2018</pre>
 */
public class StringUtilsTest {

    /**
     * Method: isBlank(String str)
     */
    @Test
    public void testIsBlank() throws Exception {

    }

    /**
     * Method: isNotBlank(String str)
     */
    @Test
    public void testIsNotBlank() throws Exception {

    }

    /**
     * Method: mapToStringWithPrefix(Map<String, String> map, String prefix)
     */
    @Test
    public void testMapToStringWithPrefix() throws Exception {

    }

    /**
     * Method: mapToString(Map<String, String> map)
     */
    @Test
    public void testMapToString() throws Exception {

    }

    /**
     * Method: stringToMap(String str, Map<String, String> map)
     */
    @Test
    public void testStringToMap() throws Exception {

    }

    /**
     * Method: escapeComma(String str)
     */
    @Test
    public void testEscapeComma() throws Exception {

    }

    /**
     * Method: unescapeComma(String str)
     */
    @Test
    public void testUnescapeComma() throws Exception {

    }

    /**
     * Method: arrayToString(Object[] items, char separator, String prefix, String postfix)
     */
    @Test
    public void testArrayToString() throws Exception {

    }

    /**
     * Method: hasText(CharSequence str)
     */
    @Test
    public void testHasText() throws Exception {

    }

    /**
     * Method: cleanPath(String path)
     */
    @Test
    public void testCleanPath() throws Exception {

    }

    /**
     * Method: collectionToDelimitedString(Collection<?> coll, String delim)
     */
    @Test
    public void testCollectionToDelimitedStringForCollDelim() throws Exception {

    }

    /**
     * Method: collectionToDelimitedString(Collection<?> coll, String delim, String prefix, String suffix)
     */
    @Test
    public void testCollectionToDelimitedStringForCollDelimPrefixSuffix() throws Exception {

    }

    /**
     * Method: delimitedListToStringArray(String str, String delimiter)
     */
    @Test
    public void testDelimitedListToStringArrayForStrDelimiter() throws Exception {
        String value = "SpringMvcJsonOutput,http://localhost:64327/greeting,GET";
        String[] valueArray = StringUtils.delimitedListToStringArray(value, ",");
        assertEquals("SpringMvcJsonOutput", valueArray[0]);
        assertEquals("http://localhost:64327/greeting", valueArray[1]);
        assertEquals("GET", valueArray[2]);
        assertEquals(3, valueArray.length);
    }

    /**
     * Method: delimitedListToStringArray(String str, String delimiter, String charsToDelete)
     */
    @Test
    public void testDelimitedListToStringArrayForStrDelimiterCharsToDelete() throws Exception {

    }

    /**
     * Method: toStringArray(Collection<String> collection)
     */
    @Test
    public void testToStringArray() throws Exception {

    }

    /**
     * Method: deleteAny(String inString, String charsToDelete)
     */
    @Test
    public void testDeleteAny() throws Exception {

    }

    /**
     * Method: replace(String inString, String oldPattern, String newPattern)
     */
    @Test
    public void testReplace() throws Exception {

    }

    /**
     * Method: countMatches(String str, char c)
     */
    @Test
    public void testCountMatches() throws Exception {

    }

}
