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
package com.alipay.common.tracer.core.span;

import com.alipay.common.tracer.core.appender.self.Timestamp;
import com.alipay.common.tracer.core.utils.StringUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Span event data.
 *
 * @author yuqian
 * @version : SpanEventData.java, v 0.1 2025-04-07 20:17 yuqian Exp $$
 */
public class SpanEventData implements Serializable {

    private long                       timestamp;

    private final Map<String, String>  eventTagWithStr    = new ConcurrentHashMap<>();

    private final Map<String, Number>  eventTagWithNumber = new ConcurrentHashMap<>();

    private final Map<String, Boolean> eventTagWithBool   = new ConcurrentHashMap<>();

    /**
     * Builder span event data builder.
     *
     * @return the span event data builder
     */
    public static SpanEventDataBuilder builder() {
        return new SpanEventDataBuilder();
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets event tag with number.
     *
     * @return the event tag with number
     */
    public Map<String, Number> getEventTagWithNumber() {
        return eventTagWithNumber;
    }

    /**
     * Gets event tag with bool.
     *
     * @return the event tag with bool
     */
    public Map<String, Boolean> getEventTagWithBool() {
        return eventTagWithBool;
    }

    /**
     * Gets event tag with str.
     *
     * @return the event tag with str
     */
    public Map<String, String> getEventTagWithStr() {
        return eventTagWithStr;
    }

    /**
     * The type Span event data builder.
     */
    public static class SpanEventDataBuilder {
        private final Map<String, String>  eventTagWithStr    = new ConcurrentHashMap<>();
        private final Map<String, Number>  eventTagWithNumber = new ConcurrentHashMap<>();
        private final Map<String, Boolean> eventTagWithBool   = new ConcurrentHashMap<>();

        private SpanEventDataBuilder() {

        }

        /**
         * Build span event data.
         *
         * @return the span event data
         */
        public SpanEventData build() {
            SpanEventData spanEventData = new SpanEventData();
            spanEventData.eventTagWithStr.putAll(eventTagWithStr);
            spanEventData.eventTagWithNumber.putAll(eventTagWithNumber);
            spanEventData.eventTagWithBool.putAll(eventTagWithBool);
            spanEventData.timestamp = System.currentTimeMillis();
            return spanEventData;
        }

        /**
         * Sets event tag.
         *
         * @param key   the key
         * @param value the value
         * @return the event tag
         */
        public SpanEventDataBuilder setEventTag(String key, String value) {
            if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
                return this;
            }
            this.eventTagWithStr.put(key, value);
            return this;
        }

        /**
         * Sets event tag.
         *
         * @param key   the key
         * @param value the value
         * @return the event tag
         */
        public SpanEventDataBuilder setEventTag(String key, boolean value) {
            if (StringUtils.isBlank(key)) {
                return this;
            }
            this.eventTagWithBool.put(key, value);
            return this;
        }

        /**
         * Sets event tag.
         *
         * @param key    the key
         * @param number the number
         * @return the event tag
         */
        public SpanEventDataBuilder setEventTag(String key, Number number) {
            if (StringUtils.isBlank(key) || number == null) {
                return this;
            }
            this.eventTagWithNumber.put(key, number);
            return this;
        }
    }
}