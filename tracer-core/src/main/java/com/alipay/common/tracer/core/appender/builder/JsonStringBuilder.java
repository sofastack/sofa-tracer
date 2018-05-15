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

/**
 * JsonStringBuilder
 * <p>
 * 方便日志输出的字符串拼接工具
 *
 * @author yangguanchao
 * @since 2018/05/14
 */
public class JsonStringBuilder {

    private static final int DEFAULT_BUFFER_SIZE = 256;

    private StringBuilder    sb;

    private boolean          isValueNullCheck    = false;

    public JsonStringBuilder() {
        this(false, DEFAULT_BUFFER_SIZE);
    }

    public JsonStringBuilder(boolean isValueNullCheck) {
        this(isValueNullCheck, DEFAULT_BUFFER_SIZE);
    }

    public JsonStringBuilder(boolean isValueNullCheck, int size) {
        this.isValueNullCheck = isValueNullCheck;
        this.sb = new StringBuilder(size);
    }

    public JsonStringBuilder appendBegin() {
        sb.append('{');
        return this;
    }

    public JsonStringBuilder appendBegin(String key, Object value) {
        this.appendBegin();
        this.append(key, value);
        return this;
    }

    public JsonStringBuilder append(String key, Object value) {
        if (value == null) {
            if (this.isValueNullCheck) {
                return this;
            }
        }
        this.append(key, value, ',');
        return this;
    }

    public JsonStringBuilder appendEnd() {
        return this.appendEnd(true);
    }

    public JsonStringBuilder appendEnd(boolean isNewLine) {
        if (this.sb.charAt(sb.length() - 1) == ',') {
            this.sb.deleteCharAt(sb.length() - 1);
        }
        this.sb.append('}');
        if (isNewLine) {
            this.sb.append(StringUtils.NEWLINE);
        }
        return this;
    }

    public JsonStringBuilder appendEnd(String key, Object value) {
        return this.appendEnd(key, value, true);
    }

    public JsonStringBuilder appendEnd(String key, Object value, boolean isNewLine) {
        if (value == null) {
            if (this.isValueNullCheck) {
                return this.appendEnd(isNewLine);
            } else {
                this.append(key, value, '}');
            }
        } else {
            this.append(key, value, '}');
        }
        if (isNewLine) {
            this.sb.append(StringUtils.NEWLINE);
        }
        return this;
    }

    private JsonStringBuilder append(String key, Object value, char endChar) {
        if (value == null) {
            this.sb.append('"').append(key).append('"').append(':').append('"').append(value)
                .append('"').append(endChar);
            return this;
        }
        if (value instanceof String) {
            String valueStr = (String) value;
            if (valueStr.length() <= 0 || (valueStr.charAt(0) != '{' && valueStr.charAt(0) != '[')) {
                //string
                this.sb.append('"').append(key).append('"').append(':').append('"').append(value)
                    .append('"').append(endChar);
                return this;
            }
        }
        //array/object/number/boolean
        this.sb.append('"').append(key).append('"').append(':').append(value).append(endChar);
        return this;
    }

    /**
     * @return JsonStringBuilder
     */
    public JsonStringBuilder reset() {
        sb.delete(0, sb.length());
        return this;
    }

    /**
     * @return string
     */
    @Override
    public String toString() {
        return sb.toString();
    }
}
