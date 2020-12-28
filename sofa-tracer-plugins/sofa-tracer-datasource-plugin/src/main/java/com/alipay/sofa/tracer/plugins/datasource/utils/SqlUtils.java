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
package com.alipay.sofa.tracer.plugins.datasource.utils;

import com.alipay.common.tracer.core.utils.StringUtils;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @since 2.2.0
 */
public class SqlUtils {
    private static final String DEFAULT_SEPARATOR        = ",";
    private static final String DEFAULT_SEPARATOR_ESCAPE = "%2C";
    private static final String DEFAULT_NEW_LINE         = "\n";
    private static final String DEFAULT_RETURN           = "\r";
    private static final String EMPTY_STRING             = "";
    private static final int    DIGEST_LOG_SQL_LIMIT     = 4096;

    public static String getSqlEscaped(String sql) {
        String limitSql = sql;
        if (limitSql != null && limitSql.length() > DIGEST_LOG_SQL_LIMIT) {
            limitSql = limitSql.substring(0, DIGEST_LOG_SQL_LIMIT) + " ...";
        }
        return escape(escapeWithSpecialCheck(limitSql), DEFAULT_SEPARATOR, DEFAULT_SEPARATOR_ESCAPE);
    }

    private static String escape(String str, String oldStr, String newStr) {
        if (str == null) {
            return EMPTY_STRING;
        }
        return StringUtils.replace(str, oldStr, newStr);
    }

    private static String escapeWithSpecialCheck(String str) {
        if (str == null) {
            return "";
        }
        StringBuilder appender = new StringBuilder();
        int len = str.length();
        appender.ensureCapacity(appender.length() + len);
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c == '\n' || c == '\r' || c == '|') {
                c = ' ';
            }
            if (c != ' ' || c == ' ' && appender.length() > 0
                && appender.charAt(appender.length() - 1) != ' ') {
                appender.append(c);
            }
        }
        return appender.toString().trim();
    }
}