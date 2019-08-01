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
package com.alipay.common.tracer.core.configuration;

import com.alipay.common.tracer.core.appender.file.TimedRollingFileAppender;
import com.alipay.common.tracer.core.appender.info.StaticInfoLog;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.utils.StringUtils;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author luoguimu123
 * @version $Id: SofaTracerConfiguration.java, v 0.1 June 22, 2017 3:12 PM luoguimu123 Exp $
 */
public class SofaTracerConfiguration {

    /**
     * Whether to open the Digest log of the middleware, closing this switch will close the Digest log of all middleware.
     */
    public static final String                     DISABLE_MIDDLEWARE_DIGEST_LOG_KEY            = "disable_middleware_digest_log";
    /**
     * com.alipay.sofa.tracer.disableConfiguration[logType]=true
     */
    public final static String                     DISABLE_DIGEST_LOG_KEY                       = "disable_digest_log";

    /**
     * Tracer's Global Rolling configured Key
     */
    public final static String                     TRACER_GLOBAL_ROLLING_KEY                    = "tracer_global_rolling_policy";

    /**
     * Tracer's global log retention days configured Key
     */
    public final static String                     TRACER_GLOBAL_LOG_RESERVE_DAY                = "tracer_global_log_reserve_day";

    /**
     * Default log retention days
     */
    public static final int                        DEFAULT_LOG_RESERVE_DAY                      = 7;

    /**
     * Threshold, the length of the service transparent field
     */
    public final static int                        PEN_ATTRS_LENGTH_TRESHOLD                    = 1024;

    /**
     * The configuration of the maximum value of the tracer's penetration data
     */
    public static final String                     TRACER_PENETRATE_ATTRIBUTE_MAX_LENGTH        = "tracer_penetrate_attribute_max_length";

    /**
     * The configuration key of the maximum value of the Tracer system penetration data
     */
    public static final String                     TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH = "tracer_system_penetrate_attribute_max_length";

    /**
     * The interval for printing the stat log.
     * This option is mainly for testability considerations. System attribute keywords.
     */
    public static final String                     STAT_LOG_INTERVAL                            = "stat_log_interval";

    /***************** Asynchronous queue configuration item  start ***************/

    /**
     * Whether to allow lost logs
     */
    public static final String                     TRACER_ASYNC_APPENDER_ALLOW_DISCARD          = "tracer_async_appender_allow_discard";
    /**
     * Whether the log output loses the number of logs
     */
    public static final String                     TRACER_ASYNC_APPENDER_IS_OUT_DISCARD_NUMBER  = "tracer_async_appender_is_out_discard_number";
    /**
     * Whether the log output loses the trace of the TraceId and RpcId
     */
    public static final String                     TRACER_ASYNC_APPENDER_IS_OUT_DISCARD_ID      = "tracer_async_appender_is_out_discard_id";
    /**
     * The number of lost logs reaches this threshold for a log output
     */
    public static final String                     TRACER_ASYNC_APPENDER_DISCARD_OUT_THRESHOLD  = "tracer_async_appender_discard_out_threshold";

    /***************** Asynchronous queue configuration item   end ***************/

    /**
     * app name
     */
    public static final String                     TRACER_APPNAME_KEY                           = "spring.application.name";

    private static Map<String, Object>             properties                                   = new ConcurrentHashMap<String, Object>();

    private static Properties                      fileProperties                               = new Properties();

    private static SofaTracerExternalConfiguration sofaTracerExternalConfiguration              = null;

    /** The key of Sampling policy name */
    public static final String                     SAMPLER_STRATEGY_NAME_KEY                    = "tracer_sampler_strategy_name_key";
    /** Custom sampling rule class name */
    public static final String                     SAMPLER_STRATEGY_CUSTOM_RULE_CLASS_NAME      = "tracer_sampler_strategy_custom_rule_class_name";
    /** The key of Sampling rate */
    public static final String                     SAMPLER_STRATEGY_PERCENTAGE_KEY              = "tracer_sampler_strategy_percentage_key";

    static {
        InputStream inputStream = null;
        try {
            inputStream = SofaTracerConfiguration.class.getClassLoader().getResourceAsStream(
                "sofa.tracer.properties");
            if (inputStream != null) {
                fileProperties.load(inputStream);
                inputStream.close();
            }
        } catch (Exception e) {
            SelfLog.info("sofa.tracer.properties文件不存在");
        }
        //Static statistics
        StaticInfoLog.logStaticInfo();
    }

    /**
     * Setting configuration item for String type value
     *
     * @param key   configuration item key
     * @param value configuration item value
     */
    public static void setProperty(String key, String value) {
        properties.put(key, value);
    }

    /**
     * Setting configuration item for Integer type value
     * @param key    configuration item key
     * @param value  configuration item value
     */
    public static void setProperty(String key, Integer value) {
        properties.put(key, value);
    }

    /**
     * Setting configuration item for Map type value
     * @param key   configuration item key
     * @param value configuration item value
     */
    public static void setProperty(String key, Map<String, String> value) {
        properties.put(key, value);
    }

    /**
     * get property by key
     *
     * @param key configuration item key
     * @return
     */
    public static String getProperty(String key) {
        return getProperty(key, StringUtils.EMPTY_STRING);
    }

    public static Integer getInteger(String key) {
        if (properties.containsKey(key)) {
            return (Integer) properties.get(key);
        }
        if (System.getProperties().containsKey(key)) {
            return Integer.valueOf(System.getProperty(key));
        }
        if (fileProperties.containsKey(key)) {
            return Integer.valueOf(fileProperties.getProperty(key));
        }
        return null;
    }

    /**
     * get property with default value
     * @param key key
     * @param defaultValue default value
     * @return Integer
     */
    public static Integer getIntegerDefaultIfNull(String key, Integer defaultValue) {
        Integer value = getInteger(key);
        return value == null ? defaultValue : value;
    }

    /**
     * get property
     * @param key key
     * @return map
     */
    public static Map<String, String> getMapEmptyIfNull(String key) {
        if (properties.containsKey(key)) {
            Object result = properties.get(key);
            if (result instanceof Map) {
                return (HashMap<String, String>) properties.get(key);
            } else {
                SelfLog.error("the value for " + key + " is not the type of map");
                return Collections.emptyMap();
            }
        }
        return Collections.emptyMap();
    }

    /**
     * get property
     *
     * @param key           key
     * @param defaultValue  defaultValue
     * @return defaultValue if value is not null
     */
    public static String getProperty(String key, String defaultValue) {
        if (properties.containsKey(key)) {
            return (String) properties.get(key);
        }
        if (System.getProperties().containsKey(key)) {
            return System.getProperty(key);
        }
        if (fileProperties.containsKey(key)) {
            return (String) fileProperties.get(key);
        }
        // Finally, it is determined whether the configuration value corresponding to
        // the key exists in the externally injected configuration.
        if (sofaTracerExternalConfiguration != null
            && sofaTracerExternalConfiguration.contains(key)) {
            return sofaTracerExternalConfiguration.getValue(key);
        }
        return defaultValue;
    }

    /**
     * Get a Rolling policy for a log
     *
     * @param rollingKey rollingKey
     * @return The Rolling policy of a log, the default policy is
     * {@link TimedRollingFileAppender#DAILY_ROLLING_PATTERN}
     */
    public static String getRollingPolicy(String rollingKey) {
        if (StringUtils.isBlank(rollingKey)) {
            return StringUtils.EMPTY_STRING;
        }
        String rollingPolicy = getProperty(rollingKey);
        if (StringUtils.isBlank(rollingPolicy)) {
            rollingPolicy = getProperty(TRACER_GLOBAL_ROLLING_KEY);
        }
        return StringUtils.isBlank(rollingPolicy) ? TimedRollingFileAppender.DAILY_ROLLING_PATTERN
            : rollingPolicy;
    }

    /**
     * Get log retention days
     * @param logReserveKey The key of Log retention days
     * @return
     */
    public static String getLogReserveConfig(String logReserveKey) {
        if (StringUtils.isBlank(logReserveKey)) {
            return StringUtils.EMPTY_STRING;
        }
        String reserveDay = getProperty(logReserveKey);

        if (StringUtils.isNotBlank(reserveDay)) {
            return reserveDay;
        }

        return String.valueOf(getProperty(TRACER_GLOBAL_LOG_RESERVE_DAY,
            String.valueOf(DEFAULT_LOG_RESERVE_DAY)));
    }

    public static void setSofaTracerExternalConfiguration(SofaTracerExternalConfiguration sofaTracerExternalConfiguration) {
        SofaTracerConfiguration.sofaTracerExternalConfiguration = sofaTracerExternalConfiguration;
    }

    public static String getSofaTracerSamplerStrategy() {
        String samplerName = getProperty(SAMPLER_STRATEGY_NAME_KEY);
        if (StringUtils.isBlank(samplerName)) {
            return null;
        }
        return samplerName;
    }
}