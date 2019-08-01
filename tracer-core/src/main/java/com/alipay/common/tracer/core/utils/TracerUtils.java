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

import com.alipay.common.tracer.core.appender.config.LogReserveConfig;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map;
import java.util.TimeZone;

/**
 * Tracer's tool class, this tool class is an internal tool class, please do not rely on non-Tracer related JAR packages.
 *
 * @author khotyn 4/4/14 1:39 PM
 */
public class TracerUtils {

    private static int         TRACE_PENETRATE_ATTRIBUTE_MAX_LENGTH         = -1;

    private static int         TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH = -1;

    public static final String KEY_OF_CURRENT_ZONE                          = "com.alipay.ldc.zone";

    public static final String CURRENT_ZONE                                 = System
                                                                                .getProperty(KEY_OF_CURRENT_ZONE);

    public static String       P_ID_CACHE                                   = null;

    /**
     * Get trace id from current tracer context.
     *
     * @return <ol>
     * <li>If current tracer context is not null, but trace id in it is null, returns an empty string.</li>
     * <li>If current tracer context is not null, and trace id in it is not null, returns trace id.</li>
     * <li>If current tracer context is null, returns an empty string.</li>
     * </ol>
     */
    public static String getTraceId() {
        SofaTracerSpan currentSpan = SofaTraceContextHolder.getSofaTraceContext().getCurrentSpan();
        if (currentSpan == null) {
            return StringUtils.EMPTY_STRING;
        }
        SofaTracerSpanContext sofaTracerSpanContext = currentSpan.getSofaTracerSpanContext();

        String traceId = null;
        if (sofaTracerSpanContext != null) {
            traceId = sofaTracerSpanContext.getTraceId();
        }
        return StringUtils.isBlank(traceId) ? StringUtils.EMPTY_STRING : traceId;
    }

    /**
     * @param sofaTracerSpan sofaTracerSpan
     * @param key            key
     * @param value          value
     * @return
     */
    public static boolean checkBaggageLength(SofaTracerSpan sofaTracerSpan, String key, String value) {
        int length = sofaTracerSpan.getSofaTracerSpanContext().getBizSerializedBaggage().length();
        if (sofaTracerSpan.getBaggageItem(key) == null) {
            length += key.length() + value.length();
        } else {
            length += value.length() - sofaTracerSpan.getBaggageItem(key).length();
        }

        length = length + StringUtils.AND.length() + StringUtils.EQUAL.length();

        return length <= getBaggageMaxLength();
    }

    /**
     * System penetration data length can be set by different -D parameters
     *
     * @return
     */
    public static int getSysBaggageMaxLength() {
        if (TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH < 0) {
            String length = SofaTracerConfiguration
                .getProperty(SofaTracerConfiguration.TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH);
            if (StringUtils.isBlank(length)) {
                //default value
                TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH = SofaTracerConfiguration.PEN_ATTRS_LENGTH_TRESHOLD;
            } else {
                try {
                    TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH = Integer.parseInt(length);
                } catch (NumberFormatException e) {
                    TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH = SofaTracerConfiguration.PEN_ATTRS_LENGTH_TRESHOLD;
                }
            }
        }
        return TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH;
    }

    public static int getBaggageMaxLength() {
        if (TRACE_PENETRATE_ATTRIBUTE_MAX_LENGTH < 0) {
            String length = SofaTracerConfiguration
                .getProperty(SofaTracerConfiguration.TRACER_PENETRATE_ATTRIBUTE_MAX_LENGTH);
            if (StringUtils.isBlank(length)) {
                //default value
                TRACE_PENETRATE_ATTRIBUTE_MAX_LENGTH = SofaTracerConfiguration.PEN_ATTRS_LENGTH_TRESHOLD;
            } else {
                try {
                    TRACE_PENETRATE_ATTRIBUTE_MAX_LENGTH = Integer.parseInt(length);
                } catch (NumberFormatException e) {
                    TRACE_PENETRATE_ATTRIBUTE_MAX_LENGTH = SofaTracerConfiguration.PEN_ATTRS_LENGTH_TRESHOLD;
                }
            }
        }
        return TRACE_PENETRATE_ATTRIBUTE_MAX_LENGTH;
    }

    /**
     * This method can be a better way under JDK9, but in the current JDK version, it can only be implemented in this way.
     *
     * In Mac OS , JDK6，JDK7，JDK8 ,it's OK
     * In Linux OS,JDK6，JDK7，JDK8 ,it's OK
     *
     * @return Process ID
     */
    public static String getPID() {
        //check pid is cached
        if (P_ID_CACHE != null) {
            return P_ID_CACHE;
        }
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();

        if (StringUtils.isBlank(processName)) {
            return StringUtils.EMPTY_STRING;
        }

        String[] processSplitName = processName.split("@");

        if (processSplitName.length == 0) {
            return StringUtils.EMPTY_STRING;
        }

        String pid = processSplitName[0];

        if (StringUtils.isBlank(pid)) {
            return StringUtils.EMPTY_STRING;
        }
        P_ID_CACHE = pid;
        return pid;
    }

    public static LogReserveConfig parseLogReserveConfig(String logReserveConfig) {
        if (StringUtils.isBlank(logReserveConfig)) {
            return new LogReserveConfig(SofaTracerConfiguration.DEFAULT_LOG_RESERVE_DAY, 0);
        }

        int day;
        int hour = 0;
        int dayIndex = logReserveConfig.indexOf("D");

        if (dayIndex >= 0) {
            day = Integer.valueOf(logReserveConfig.substring(0, dayIndex));
        } else {
            day = Integer.valueOf(logReserveConfig);
        }

        int hourIndex = logReserveConfig.indexOf("H");

        if (hourIndex >= 0) {
            hour = Integer.valueOf(logReserveConfig.substring(dayIndex + 1, hourIndex));
        }

        return new LogReserveConfig(day, hour);
    }

    public static boolean isLoadTest(SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null || sofaTracerSpan.getSofaTracerSpanContext() == null) {
            return false;
        } else {
            SofaTracerSpanContext spanContext = sofaTracerSpan.getSofaTracerSpanContext();
            Map<String, String> baggage = spanContext.getBizBaggage();
            return SofaTracerConstant.LOAD_TEST_VALUE.equals(baggage
                .get(SofaTracerConstant.LOAD_TEST_TAG));
        }
    }

    public static String getLoadTestMark(SofaTracerSpan span) {
        if (TracerUtils.isLoadTest(span)) {
            return SofaTracerConstant.LOAD_TEST_VALUE;
        } else {
            //non-pressure test
            return SofaTracerConstant.NON_LOAD_TEST_VALUE;
        }
    }

    public static String getInetAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress address = null;
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(":") == -1) {
                        return address.getHostAddress();
                    }
                }
            }
            return null;
        } catch (Throwable t) {
            return null;
        }
    }

    public static String removeJSessionIdFromUrl(String url) {
        if (url == null) {
            return null;
        }
        int index = url.indexOf(";jsessionid=");
        if (index < 0) {
            return url;
        }
        return url.substring(0, index);
    }

    public static String getCurrentZone() {
        return CURRENT_ZONE;
    }

    public static String getDefaultTimeZone() {
        return TimeZone.getDefault().getID();
    }

    /**
     * Get a value from the Map, or return an empty string if it is null
     *
     * @param map map
     * @param key key
     * @return
     */
    public static String getEmptyStringIfNull(Map<String, String> map, String key) {
        String value = map.get(key);
        return value == null ? StringUtils.EMPTY_STRING : value;
    }

    /**
     * Convert a Host address to a hexadecimal number
     *
     * @param host host address
     * @return hexadecimal number
     */
    public static String hostToHexString(String host) { //NOPMD
        return Integer.toHexString(host.hashCode());
    }
}
