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

import com.alipay.common.tracer.core.appender.builder.XStringBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.alipay.common.tracer.core.appender.builder.XStringBuilder.*;

/**
 * StringUtils
 *
 * @author yangguanchao
 * @since 2017/06/17
 */
public class StringUtils {

    public static final String   NEWLINE                  = "\r\n";

    public static final String   EMPTY_STRING             = "";

    public static final String   EQUAL                    = "=";

    public static final String   AND                      = "&";

    public static final char     EQUAL_CHARACTER          = '=';

    public static final char     AND_CHARACTER            = '&';

    public static final String[] EMPTY_STRING_ARRAY       = new String[0];

    private static final String  FOLDER_SEPARATOR         = "/";

    private static final String  WINDOWS_FOLDER_SEPARATOR = "\\";

    private static final String  TOP_PATH                 = "..";

    private static final String  CURRENT_PATH             = ".";

    /***
     * 字符串是否为空
     * @param str 字符串
     * @return true : 字符串为空
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(str.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 字符串是否不为空
     *
     * @param str 字符串
     * @return true : 非空
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * <p>
     * 将map转成string并在每一个 key 添加指定的前缀, 如 {"k1":"v1"}
     * </p>
     * @param map map 要映射的集合
     * @param prefix prefix 前缀
     * @return 字符串携带指定前缀 prefix_k1=v1
     */
    public static String mapToStringWithPrefix(Map<String, String> map, String prefix) {
        StringBuilder sb = new StringBuilder(XStringBuilder.DEFAULT_BUFFER_SIZE);

        if (prefix == null) {
            prefix = StringUtils.EMPTY_STRING;
        }
        if (map == null) {
            sb.append(StringUtils.EMPTY_STRING);
        } else {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = prefix + escapePercentEqualAnd(entry.getKey());
                String val = escapePercentEqualAnd(entry.getValue());
                sb.append(key).append(StringUtils.EQUAL).append(val).append(StringUtils.AND);
            }
        }

        return sb.toString();
    }

    /**
     * <p>
     * 将map转成string, 如{"k1":"v1"}
     * </p>
     * @param map 要映射的集合
     * @return 字符串 k1=v1
     */
    public static String mapToString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder(XStringBuilder.DEFAULT_BUFFER_SIZE);

        if (map == null || map.size() == 0) {
            sb.append(StringUtils.EMPTY_STRING);
        } else {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = escapePercentEqualAnd(entry.getKey());
                String val = escapePercentEqualAnd(entry.getValue());
                sb.append(key).append(StringUtils.EQUAL).append(val).append(StringUtils.AND);
            }
        }

        return sb.toString();
    }

    /**
     * 由string转成map, 函数 mapToString 的逆过程
     * @param str 字符串
     * @param map 映射的集合
     */
    public static void stringToMap(String str, Map<String, String> map) {

        if (isBlank(str)) {
            return;
        }

        String key = null;
        String value = null;
        int mark = -1;
        for (int i = 0; i < str.length(); i++) {

            char c = str.charAt(i);
            switch (c) {
                case StringUtils.AND_CHARACTER:
                    value = str.substring(mark + 1, i);
                    if (key != null) {
                        map.put(unescapeEqualAndPercent(key), unescapeEqualAndPercent(value));
                    }
                    key = null;
                    mark = i;
                    break;
                case StringUtils.EQUAL_CHARACTER:
                    key = str.substring(mark + 1, i);
                    mark = i;
                    break;
                default:
                    break;
            }
        }

        if (key != null) {
            int l = str.length() - mark - 1;
            value = l == 0 ? "" : str.substring(mark + 1);
            map.put(key, unescapeEqualAndPercent(value));
        }
    }

    public static String escapeComma(String str) {
        return escape(str, XStringBuilder.DEFAULT_SEPARATOR + EMPTY_STRING,
            XStringBuilder.DEFAULT_SEPARATOR_ESCAPE);
    }

    public static String unescapeComma(String str) {
        return escape(str, XStringBuilder.DEFAULT_SEPARATOR_ESCAPE,
            XStringBuilder.DEFAULT_SEPARATOR + EMPTY_STRING);
    }

    /**
     * @param items 列表
     * @param separator 分隔符
     * @param prefix 前缀
     * @param postfix 后缀
     * @return 字符串
     */
    public static String arrayToString(Object[] items, char separator, String prefix, String postfix) {
        String emptyArrayString = (EMPTY_STRING + prefix) + postfix;

        // handle null, zero and one elements before building a buffer
        if (items == null) {
            return emptyArrayString;
        }
        if (items.length == 0) {
            return emptyArrayString;
        }

        Object first = items[0];

        if (items.length == 1) {
            return first == null ? emptyArrayString : prefix + first.toString() + postfix;
        }

        StringBuffer buf = new StringBuffer(256);
        buf.append(prefix);

        if (first != null) {
            buf.append(first);
        }

        for (int i = 1; i < items.length; i++) {
            buf.append(separator);
            Object obj = items[i];

            if (obj != null) {
                buf.append(obj);
            }
        }

        buf.append(postfix);
        return buf.toString();
    }

    //替换str中的"&"，"=" 和 "%"
    public static String escapePercentEqualAnd(String str) {
        // 必须先对 % 做转义
        return escape(
            escape(escape(str, PERCENT, PERCENT_ESCAPE), AND_SEPARATOR, AND_SEPARATOR_ESCAPE),
            EQUAL_SEPARATOR, EQUAL_SEPARATOR_ESCAPE);
    }

    //将 str 中被转义的 & ， = 和 % 转义回来
    public static String unescapeEqualAndPercent(String str) {
        // 必须最后才对 % 做转义
        return escape(
            escape(escape(str, EQUAL_SEPARATOR_ESCAPE, EQUAL_SEPARATOR), AND_SEPARATOR_ESCAPE,
                AND_SEPARATOR), PERCENT_ESCAPE, PERCENT);
    }

    /**
     * 将str中的oldStr替换为newStr
     */
    private static String escape(String str, String oldStr, String newStr) {
        if (str == null) {
            return StringUtils.EMPTY_STRING;
        }
        return str.replace(oldStr, newStr);
    }

    public static boolean hasText(CharSequence str) {
        if (isNotBlank(str.toString())) {
            return false;
        }

        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static String cleanPath(String path) {
        if (path == null) {
            return null;
        }
        String pathToUse = replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);

        // Strip prefix from path to analyze, to not treat it as part of the
        // first path element. This is necessary to correctly parse paths like
        // "file:core/../core/io/Resource.class", where the ".." should just
        // strip the first "core" directory while keeping the "file:" prefix.
        int prefixIndex = pathToUse.indexOf(":");
        String prefix = "";
        if (prefixIndex != -1) {
            prefix = pathToUse.substring(0, prefixIndex + 1);
            if (prefix.contains("/")) {
                prefix = "";
            } else {
                pathToUse = pathToUse.substring(prefixIndex + 1);
            }
        }
        if (pathToUse.startsWith(FOLDER_SEPARATOR)) {
            prefix = prefix + FOLDER_SEPARATOR;
            pathToUse = pathToUse.substring(1);
        }

        String[] pathArray = delimitedListToStringArray(pathToUse, FOLDER_SEPARATOR);
        List<String> pathElements = new LinkedList<String>();
        int tops = 0;

        for (int i = pathArray.length - 1; i >= 0; i--) {
            String element = pathArray[i];
            if (CURRENT_PATH.equals(element)) {// NOPMD
                // Points to current directory - drop it.
            } else if (TOP_PATH.equals(element)) {
                // Registering top path found.
                tops++;
            } else {
                if (tops > 0) {
                    // Merging path element with element corresponding to top path.
                    tops--;
                } else {
                    // Normal path element found.
                    pathElements.add(0, element);
                }
            }
        }

        // Remaining top paths need to be retained.
        for (int i = 0; i < tops; i++) {
            pathElements.add(0, TOP_PATH);
        }

        return prefix + collectionToDelimitedString(pathElements, FOLDER_SEPARATOR);
    }

    /**
     * Convert a {@code Collection} into a delimited {@code String} (e.g. CSV).
     * <p>Useful for {@code toString()} implementations.
     * @param coll the {@code Collection} to convert
     * @param delim the delimiter to use (typically a ",")
     * @return the delimited {@code String}
     */
    public static String collectionToDelimitedString(Collection<?> coll, String delim) {
        return collectionToDelimitedString(coll, delim, "", "");
    }

    public static String collectionToDelimitedString(Collection<?> coll, String delim,
                                                     String prefix, String suffix) {
        if (coll == null || coll.size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Iterator<?> it = coll.iterator();
        while (it.hasNext()) {
            sb.append(prefix).append(it.next()).append(suffix);
            if (it.hasNext()) {
                sb.append(delim);
            }
        }
        return sb.toString();
    }

    public static String[] delimitedListToStringArray(String str, String delimiter) {
        return delimitedListToStringArray(str, delimiter, null);
    }

    /**
     * Take a {@code String} that is a delimited list and convert it into
     * a {@code String} array.
     * <p>A single {@code delimiter} may consist of more than one character,
     * but it will still be considered as a single delimiter string, rather
     * than as bunch of potential delimiter characters, in contrast to
     * @param str the input {@code String}
     * @param delimiter the delimiter between elements (this is a single delimiter,
     * rather than a bunch individual delimiter characters)
     * @param charsToDelete a set of characters to delete; useful for deleting unwanted
     * line breaks: e.g. "\r\n\f" will delete all new lines and line feeds in a {@code String}
     * @return an array of the tokens in the list
     */
    public static String[] delimitedListToStringArray(String str, String delimiter,
                                                      String charsToDelete) {
        if (str == null) {
            return new String[0];
        }
        if (delimiter == null) {
            return new String[] { str };
        }

        List<String> result = new ArrayList<String>();
        if ("".equals(delimiter)) {
            for (int i = 0; i < str.length(); i++) {
                result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
            }
        } else {
            int pos = 0;
            int delPos;
            while ((delPos = str.indexOf(delimiter, pos)) != -1) {
                result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
                pos = delPos + delimiter.length();
            }
            if (str.length() > 0 && pos <= str.length()) {
                // Add rest of String, but not in case of empty input.
                result.add(deleteAny(str.substring(pos), charsToDelete));
            }
        }
        return toStringArray(result);
    }

    public static String[] toStringArray(Collection<String> collection) {
        if (collection == null) {
            return null;
        }

        return collection.toArray(new String[collection.size()]);
    }

    /**
     * Delete any character in a given {@code String}.
     * @param inString the original {@code String}
     * @param charsToDelete a set of characters to delete.
     * E.g. "az\n" will delete 'a's, 'z's and new lines.
     * @return the resulting {@code String}
     */
    public static String deleteAny(String inString, String charsToDelete) {
        if (isBlank(inString) || isBlank(charsToDelete)) {
            return inString;
        }

        StringBuilder sb = new StringBuilder(inString.length());
        for (int i = 0; i < inString.length(); i++) {
            char c = inString.charAt(i);
            if (charsToDelete.indexOf(c) == -1) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Replace all occurrences of a substring within a string with
     * another string.
     * @param inString {@code String} to examine
     * @param oldPattern {@code String} to replace
     * @param newPattern {@code String} to insert
     * @return a {@code String} with the replacements
     */
    public static String replace(String inString, String oldPattern, String newPattern) {
        if (isBlank(inString) || isBlank(oldPattern) || newPattern == null) {
            return inString;
        }
        int index = inString.indexOf(oldPattern);
        if (index == -1) {
            // no occurrence -> can return input as-is
            return inString;
        }

        int capacity = inString.length();
        if (newPattern.length() > oldPattern.length()) {
            capacity += 16;
        }
        StringBuilder sb = new StringBuilder(capacity);

        int pos = 0; // our position in the old string
        int patLen = oldPattern.length();
        while (index >= 0) {
            sb.append(inString.substring(pos, index));
            sb.append(newPattern);
            pos = index + patLen;
            index = inString.indexOf(oldPattern, pos);
        }

        // append any characters to the right of a match
        sb.append(inString.substring(pos));
        return sb.toString();
    }

    /***
     * 对指定的字符串中出现的字符进行计数
     * @param str 要被统计的字符串
     * @param c 出现的字符
     * @return 个数
     */
    public static int countMatches(String str, char c) {
        if (str == null || str.length() == 0) {
            return 0;
        }

        int count = 0;

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) {
                count++;
            }
        }

        return count;
    }

}
