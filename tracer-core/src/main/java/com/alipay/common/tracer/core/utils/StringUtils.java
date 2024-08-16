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

    public static final int      INDEX_NOT_FOUND          = -1;

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

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Convert the map to a string and add the specified prefix to each key, such as {"k1":"v1"}
     *
     * @param map       origin data
     * @param prefix    prefix
     * @return
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
     * Convert the map to a string, such as {"k1":"v1"}
     *
     * @param map origin data
     * @return
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
     * Convert from string to map, the inverse of the function mapToString
     * @param str origin data
     * @param map default result
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
     * Array to string
     * @param items     origin data
     * @param separator separator
     * @param prefix    prefix
     * @param postfix   postfix
     * @return
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

    /**
     * Replace "&"、"=" and "%"
     * @param str origin data
     */
    public static String escapePercentEqualAnd(String str) {
        if (str == null) {
            return StringUtils.EMPTY_STRING;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == PERCENT_CHAR) {
                sb.append(PERCENT_ESCAPE);
            } else if (ch == AND_SEPARATOR_CHAR) {
                sb.append(AND_SEPARATOR_ESCAPE);
            } else if (ch == EQUAL_SEPARATOR_CHAR) {
                sb.append(EQUAL_SEPARATOR_ESCAPE);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * escapePercentEqualAnd's Reverse Operation
     * @param str
     * @return
     */
    public static String unescapeEqualAndPercent(String str) {
        // You must first escape the %
        return escape(
            escape(escape(str, EQUAL_SEPARATOR_ESCAPE, EQUAL_SEPARATOR), AND_SEPARATOR_ESCAPE,
                AND_SEPARATOR), PERCENT_ESCAPE, PERCENT);
    }

    /**
     * Replace oldStr in str with newStr
     * @param str       target origin data
     * @param oldStr    oldStr
     * @param newStr    newStr
     */
    private static String escape(String str, String oldStr, String newStr) {
        if (str == null) {
            return StringUtils.EMPTY_STRING;
        }
        return replace(str, oldStr, newStr);
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
        return replace(inString, oldPattern, newPattern, -1);
    }

    /**
     * Replaces a String with another String inside a larger String,
     * for the first {@code max} values of the search String.
     *
     * <p>A {@code null} reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringUtils.replace(null, *, *, *)         = null
     * StringUtils.replace("", *, *, *)           = ""
     * StringUtils.replace("any", null, *, *)     = "any"
     * StringUtils.replace("any", *, null, *)     = "any"
     * StringUtils.replace("any", "", *, *)       = "any"
     * StringUtils.replace("any", *, *, 0)        = "any"
     * StringUtils.replace("abaa", "a", null, -1) = "abaa"
     * StringUtils.replace("abaa", "a", "", -1)   = "b"
     * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
     * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
     * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
     * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
     * </pre>
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for, may be null
     * @param replacement  the String to replace it with, may be null
     * @param max          maximum number of values to replace, or {@code -1} if no maximum
     * @return the text with any replacements processed,
     * {@code null} if null String input
     */
    public static String replace(final String text, final String searchString,
                                 final String replacement, final int max) {
        return replace(text, searchString, replacement, max, false);
    }

    /**
     * Replaces a String with another String inside a larger String,
     * for the first {@code max} values of the search String,
     * case-sensitively/insensitively based on {@code ignoreCase} value.
     *
     * <p>A {@code null} reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringUtils.replace(null, *, *, *, false)         = null
     * StringUtils.replace("", *, *, *, false)           = ""
     * StringUtils.replace("any", null, *, *, false)     = "any"
     * StringUtils.replace("any", *, null, *, false)     = "any"
     * StringUtils.replace("any", "", *, *, false)       = "any"
     * StringUtils.replace("any", *, *, 0, false)        = "any"
     * StringUtils.replace("abaa", "a", null, -1, false) = "abaa"
     * StringUtils.replace("abaa", "a", "", -1, false)   = "b"
     * StringUtils.replace("abaa", "a", "z", 0, false)   = "abaa"
     * StringUtils.replace("abaa", "A", "z", 1, false)   = "abaa"
     * StringUtils.replace("abaa", "A", "z", 1, true)   = "zbaa"
     * StringUtils.replace("abAa", "a", "z", 2, true)   = "zbza"
     * StringUtils.replace("abAa", "a", "z", -1, true)  = "zbzz"
     * </pre>
     *
     * @param text         text to search and replace in, may be null
     * @param searchString the String to search for (case-insensitive), may be null
     * @param replacement  the String to replace it with, may be null
     * @param max          maximum number of values to replace, or {@code -1} if no maximum
     * @param ignoreCase   if true replace is case-insensitive, otherwise case-sensitive
     * @return the text with any replacements processed,
     * {@code null} if null String input
     */
    private static String replace(final String text, String searchString, final String replacement,
                                  int max, final boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }
        if (ignoreCase) {
            searchString = searchString.toLowerCase();
        }
        int start = 0;
        int end = ignoreCase ? indexOfIgnoreCase(text, searchString, start) : indexOf(text,
            searchString, start);
        if (end == INDEX_NOT_FOUND) {
            return text;
        }
        final int replLength = searchString.length();
        int increase = Math.max(replacement.length() - replLength, 0);
        increase *= max < 0 ? 16 : Math.min(max, 64);
        final StringBuilder buf = new StringBuilder(text.length() + increase);
        while (end != INDEX_NOT_FOUND) {
            buf.append(text, start, end).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = ignoreCase ? indexOfIgnoreCase(text, searchString, start) : indexOf(text,
                searchString, start);
        }
        buf.append(text, start, text.length());
        return buf.toString();
    }

    /**
     * Finds the first index within a CharSequence, handling {@code null}.
     * This method uses {@link String#indexOf(String, int)} if possible.
     *
     * <p>A {@code null} CharSequence will return {@code -1}.
     * A negative start position is treated as zero.
     * An empty ("") search CharSequence always matches.
     * A start position greater than the string length only matches
     * an empty search CharSequence.</p>
     *
     * <pre>
     * StringUtils.indexOf(null, *, *)          = -1
     * StringUtils.indexOf(*, null, *)          = -1
     * StringUtils.indexOf("", "", 0)           = 0
     * StringUtils.indexOf("", *, 0)            = -1 (except when * = "")
     * StringUtils.indexOf("aabaabaa", "a", 0)  = 0
     * StringUtils.indexOf("aabaabaa", "b", 0)  = 2
     * StringUtils.indexOf("aabaabaa", "ab", 0) = 1
     * StringUtils.indexOf("aabaabaa", "b", 3)  = 5
     * StringUtils.indexOf("aabaabaa", "b", 9)  = -1
     * StringUtils.indexOf("aabaabaa", "b", -1) = 2
     * StringUtils.indexOf("aabaabaa", "", 2)   = 2
     * StringUtils.indexOf("abc", "", 9)        = 3
     * </pre>
     *
     * @param seq       the CharSequence to check, may be null
     * @param searchSeq the CharSequence to find, may be null
     * @param startPos  the start position, negative treated as zero
     * @return the first index of the search CharSequence (always &ge; startPos),
     * -1 if no match or {@code null} string input
     * @since 2.0
     * @since 3.0 Changed signature from indexOf(String, String, int) to indexOf(CharSequence, CharSequence, int)
     */
    public static int indexOf(final CharSequence seq, final CharSequence searchSeq,
                              final int startPos) {
        if (seq == null || searchSeq == null) {
            return INDEX_NOT_FOUND;
        }
        if (seq instanceof String) {
            return ((String) seq).indexOf(searchSeq.toString(), startPos);
        } else if (seq instanceof StringBuilder) {
            return ((StringBuilder) seq).indexOf(searchSeq.toString(), startPos);
        } else if (seq instanceof StringBuffer) {
            return ((StringBuffer) seq).indexOf(searchSeq.toString(), startPos);
        }
        return seq.toString().indexOf(searchSeq.toString(), startPos);
    }

    /**
     * Case in-sensitive find of the first index within a CharSequence
     * from the specified position.
     *
     * <p>A {@code null} CharSequence will return {@code -1}.
     * A negative start position is treated as zero.
     * An empty ("") search CharSequence always matches.
     * A start position greater than the string length only matches
     * an empty search CharSequence.</p>
     *
     * <pre>
     * StringUtils.indexOfIgnoreCase(null, *, *)          = -1
     * StringUtils.indexOfIgnoreCase(*, null, *)          = -1
     * StringUtils.indexOfIgnoreCase("", "", 0)           = 0
     * StringUtils.indexOfIgnoreCase("aabaabaa", "A", 0)  = 0
     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", 0)  = 2
     * StringUtils.indexOfIgnoreCase("aabaabaa", "AB", 0) = 1
     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", 3)  = 5
     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", 9)  = -1
     * StringUtils.indexOfIgnoreCase("aabaabaa", "B", -1) = 2
     * StringUtils.indexOfIgnoreCase("aabaabaa", "", 2)   = 2
     * StringUtils.indexOfIgnoreCase("abc", "", 9)        = -1
     * </pre>
     *
     * @param str       the CharSequence to check, may be null
     * @param searchStr the CharSequence to find, may be null
     * @param startPos  the start position, negative treated as zero
     * @return the first index of the search CharSequence (always &ge; startPos),
     * -1 if no match or {@code null} string input
     * @since 2.5
     * @since 3.0 Changed signature from indexOfIgnoreCase(String, String, int) to indexOfIgnoreCase(CharSequence, CharSequence, int)
     */
    public static int indexOfIgnoreCase(final CharSequence str, final CharSequence searchStr,
                                        int startPos) {
        if (str == null || searchStr == null) {
            return INDEX_NOT_FOUND;
        }
        if (startPos < 0) {
            startPos = 0;
        }
        final int endLimit = str.length() - searchStr.length() + 1;
        if (startPos > endLimit) {
            return INDEX_NOT_FOUND;
        }
        if (searchStr.length() == 0) {
            return startPos;
        }
        for (int i = startPos; i < endLimit; i++) {
            if (regionMatches(str, true, i, searchStr, 0, searchStr.length())) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * Green implementation of regionMatches.
     *
     * @param cs         the {@code CharSequence} to be processed
     * @param ignoreCase whether or not to be case insensitive
     * @param thisStart  the index to start on the {@code cs} CharSequence
     * @param substring  the {@code CharSequence} to be looked for
     * @param start      the index to start on the {@code substring} CharSequence
     * @param length     character length of the region
     * @return whether the region matched
     */
    public static boolean regionMatches(final CharSequence cs, final boolean ignoreCase,
                                        final int thisStart, final CharSequence substring,
                                        final int start, final int length) {
        if (cs instanceof String && substring instanceof String) {
            return ((String) cs).regionMatches(ignoreCase, thisStart, (String) substring, start,
                length);
        }
        int index1 = thisStart;
        int index2 = start;
        int tmpLen = length;

        // Extract these first so we detect NPEs the same as the java.lang.String version
        final int srcLen = cs.length() - thisStart;
        final int otherLen = substring.length() - start;

        // Check for invalid parameters
        if (thisStart < 0 || start < 0 || length < 0) {
            return false;
        }

        // Check that the regions are long enough
        if (srcLen < length || otherLen < length) {
            return false;
        }

        while (tmpLen-- > 0) {
            final char c1 = cs.charAt(index1++);
            final char c2 = substring.charAt(index2++);

            if (c1 == c2) {
                continue;
            }

            if (!ignoreCase) {
                return false;
            }

            // The real same check as in String.regionMatches():
            final char u1 = Character.toUpperCase(c1);
            final char u2 = Character.toUpperCase(c2);
            if (u1 != u2 && Character.toLowerCase(u1) != Character.toLowerCase(u2)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Counts the characters that appear in the specified string
     *
     * @param str   target str
     * @param c     target char c
     * @return
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
