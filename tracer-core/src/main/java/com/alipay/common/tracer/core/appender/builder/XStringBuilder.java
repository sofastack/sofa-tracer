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

import java.util.Map;

/**
 * XStringBuilder
 * <p>
 * String splicing tool for convenient log output
 * </p>
 * @author yangguanchao
 * @since 2017/06/25
 */
public class XStringBuilder {

    public static final int    DEFAULT_BUFFER_SIZE      = 256;
    public static final char   DEFAULT_SEPARATOR        = ',';
    public static final String DEFAULT_SEPARATOR_ESCAPE = "%2C";
    public static final String AND_SEPARATOR            = "&";
    public static final char   AND_SEPARATOR_CHAR       = '&';
    public static final String AND_SEPARATOR_ESCAPE     = "%26";
    public static final String EQUAL_SEPARATOR          = "=";
    public static final char   EQUAL_SEPARATOR_CHAR     = '=';
    public static final String EQUAL_SEPARATOR_ESCAPE   = "%3D";
    public static final String PERCENT                  = "%";
    public static final char   PERCENT_CHAR             = '%';
    public static final String PERCENT_ESCAPE           = "%25";

    private static char        separator                = DEFAULT_SEPARATOR;
    private static String      separatorStr             = separator + "";
    private static String      spearatorEscape          = DEFAULT_SEPARATOR_ESCAPE;
    private StringBuilder      sb;

    public XStringBuilder() {
        this(DEFAULT_BUFFER_SIZE, DEFAULT_SEPARATOR);
    }

    public XStringBuilder(int size) {
        this(size, DEFAULT_SEPARATOR);
    }

    public XStringBuilder(int size, char separator) {
        XStringBuilder.separator = separator;
        sb = new StringBuilder(size);
    }

    /**
     * @param str Input string
     * @return this
     */
    public XStringBuilder append(String str) {
        sb.append(str == null ? StringUtils.EMPTY_STRING : str).append(separator);
        return this;
    }

    /**
     * @param str Input string
     * @return this
     */
    public XStringBuilder append(long str) {
        sb.append(str).append(separator);
        return this;
    }

    /**
     * @param str Input string
     * @param separator
     * @return this
     */
    public XStringBuilder append(long str, String separator) {
        sb.append(str).append(separator);
        return this;
    }

    /**
     * @param str Input string
     * @param separator
     * @return this
     */
    public XStringBuilder append(long str, char separator) {
        sb.append(str).append(separator);
        return this;
    }

    /**
     * @param str Input string
     * @param separator
     * @return this
     */
    public XStringBuilder append(String str, String separator) {
        sb.append(str == null ? StringUtils.EMPTY_STRING : str).append(separator);
        return this;
    }

    /**
     * @param str Input string
     * @return this
     */
    public XStringBuilder append(int str) {
        sb.append(str).append(separator);
        return this;
    }

    /**
     * @param str Input string
     * @return this
     */
    public XStringBuilder append(char str) {
        sb.append(str).append(separator);
        return this;
    }

    /**
     * @param map map param
     * @return this
     */
    public XStringBuilder append(Map<String, String> map) {
        this.appendEscape(StringUtils.mapToString(map));
        return this;
    }

    /**
     * @param str Input string
     * @return this
     */
    public XStringBuilder appendEnd(int str) {
        sb.append(str).append(StringUtils.NEWLINE);
        return this;
    }

    /**
     * @param str Input string
     * @return this
     */
    public XStringBuilder appendEnd(String str) {
        sb.append(str == null ? StringUtils.EMPTY_STRING : str).append(StringUtils.NEWLINE);
        return this;
    }

    /**
     * @param str Input string
     * @return this
     */
    public XStringBuilder appendEnd(long str) {
        sb.append(str).append(StringUtils.NEWLINE);
        return this;
    }

    /**
     * @param c Input char
     * @return this
     */
    public XStringBuilder appendEnd(char c) {
        sb.append(String.valueOf(c)).append(StringUtils.NEWLINE);
        return this;
    }

    /**
     * @param map map param
     * @return this
     */
    public XStringBuilder appendEnd(Map<String, String> map) {
        this.appendEscapeEnd(StringUtils.mapToString(map));
        return this;
    }

    /**
     * @param str Input string
     * @return this
     */
    public XStringBuilder appendRaw(String str) {
        sb.append(str == null ? StringUtils.EMPTY_STRING : str);
        return this;
    }

    /**
     * Change the separator in the string to the corresponding escape character
     * @param str Input string
     * @return this
     */
    public XStringBuilder appendEscape(String str) {
        str = (str == null) ? StringUtils.EMPTY_STRING : str;
        str = str.replace(separatorStr, spearatorEscape);
        return append(str);
    }

    /**
     * Change the separator in the string to the corresponding escape character on raw
     * @param str Input string
     * @return this
     */
    public XStringBuilder appendEscapeRaw(String str) {
        str = (str == null) ? StringUtils.EMPTY_STRING : str;
        str = str.replace(separatorStr, spearatorEscape);
        return appendRaw(str);
    }

    /**
     * Change the separator in the string to the corresponding escape character at the end
     * @param str Input string
     * @return this
     */
    public XStringBuilder appendEscapeEnd(String str) {
        str = (str == null) ? StringUtils.EMPTY_STRING : str;
        str = str.replace(separatorStr, spearatorEscape);
        return appendEnd(str);
    }

    /**
     * @return this
     */
    public XStringBuilder reset() {
        sb.delete(0, sb.length());
        return this;
    }

    /**
     * @return this
     */
    @Override
    public String toString() {
        return sb.toString();
    }
}
