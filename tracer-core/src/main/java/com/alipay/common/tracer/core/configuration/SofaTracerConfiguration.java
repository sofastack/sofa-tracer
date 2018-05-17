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
 * @version $Id: SofaTracerConfiguration.java, v 0.1 2017年06月22日 下午3:12 luoguimu123 Exp $
 */
public class SofaTracerConfiguration {

    /**
     * 是否开启中间件的 Digest 日志，关闭这个开关将会关闭所有的中间件的 Digest 日志
     */
    public static final String                     DISABLE_MIDDLEWARE_DIGEST_LOG_KEY            = "disable_middleware_digest_log";
    /**
     * key=是否开启特定的中间件 Digest 日志开关,value=map[日志类型为key:value(开关)]
     */
    public final static String                     DISABLE_DIGEST_LOG_KEY                       = "disable_digest_log";

    /**
     * Tracer 的全局的 Rolling 的配置的 Key
     */
    public final static String                     TRACER_GLOBAL_ROLLING_KEY                    = "tracer_global_rolling_policy";

    /**
     * Tracer 的全局的日志的保留天数配置的 Key
     */
    public final static String                     TRACER_GLOBAL_LOG_RESERVE_DAY                = "tracer_global_log_reserve_day";

    /***
     * 默认日志的保留天数
     */
    public static final int                        DEFAULT_LOG_RESERVE_DAY                      = 7;

    /**
     * 阈值，业务透传字段的限制长度
     */
    public final static int                        PEN_ATTRS_LENGTH_TRESHOLD                    = 1024;

    /**
     * Tracer 的穿透数据的最大值的配置的 Key
     */
    public static final String                     TRACER_PENETRATE_ATTRIBUTE_MAX_LENGTH        = "tracer_penetrate_attribute_max_length";

    /**
     * Tracer 系统穿透数据的最大值的配置的 Key
     */
    public static final String                     TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH = "tracer_system_penetrate_attribute_max_length";

    /**
     * 统计日志的打印的间隔，加入这个选项主要是为了可测试性的考虑,系统属性关键字
     */
    public static final String                     STAT_LOG_INTERVAL                            = "stat_log_interval";

    /*****************异步队列配置项   start***************/

    /**
     * 是否允许丢失日志
     */
    public static final String                     TRACER_ASYNC_APPENDER_ALLOW_DISCARD          = "tracer_async_appender_allow_discard";
    /**
     * 是否日志输出丢失日志的数量
     */
    public static final String                     TRACER_ASYNC_APPENDER_IS_OUT_DISCARD_NUMBER  = "tracer_async_appender_is_out_discard_number";
    /**
     * 是否日志输出丢失日志的TraceId和RpcId
     */
    public static final String                     TRACER_ASYNC_APPENDER_IS_OUT_DISCARD_ID      = "tracer_async_appender_is_out_discard_id";
    /**
     * 丢失日志的数量达到该阈值进行一次日志输出
     */
    public static final String                     TRACER_ASYNC_APPENDER_DISCARD_OUT_THRESHOLD  = "tracer_async_appender_discard_out_threshold";

    /*****************异步队列配置项   end***************/

    /**
     * 应用名称
     */
    public static final String                     TRACER_APPNAME_KEY                           = "spring.application.name";

    private static Map<String, Object>             properties                                   = new ConcurrentHashMap<String, Object>();

    private static Properties                      fileProperties                               = new Properties();

    private static SofaTracerExternalConfiguration sofaTracerExternalConfiguration              = null;

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
        //静态统计信息
        StaticInfoLog.logStaticInfo();
    }

    /**
     * 设置配置项
     *
     * @param key   配置项 key
     * @param value 配置项的值
     */
    public static void setProperty(String key, String value) {
        properties.put(key, value);
    }

    /**
     * @param key 关键字
     * @param value 值
     */
    public static void setProperty(String key, Integer value) {
        properties.put(key, value);
    }

    /**
     * @param key 关键字
     * @param value 值
     */
    public static void setProperty(String key, Map<String, String> value) {
        properties.put(key, value);
    }

    /**
     * 获取配置项
     *
     * @param key 配置项的 key
     * @return 属性值
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
     * @param key 关键字
     * @param defaultValue 默认值
     * @return 整数
     */
    public static Integer getIntegerDefaultIfNull(String key, Integer defaultValue) {
        Integer value = getInteger(key);
        return value == null ? defaultValue : value;
    }

    /**
     * @param key 关键字
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
     * 获取配置项
     *
     * @param key          配置项的 key
     * @param defaultValue 如果获取出得配置项是 null，则返回 defaultValue
     * @return 配置值或者默认值
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
        //最后判断外部注入的配置中是否存在该key对应的配置值
        if (sofaTracerExternalConfiguration != null
            && sofaTracerExternalConfiguration.contains(key)) {
            return sofaTracerExternalConfiguration.getValue(key);
        }
        return defaultValue;
    }

    /**
     * 获取某一个日志的 Rolling 策略
     *
     * @param rollingKey 滚动策略名称
     * @return 某一个日志的 Rolling 策略，默认的策略是
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

    /***
     * 获取日志保留天数
     * @param logReserveKey 日志保留天数关键字,跟进此获取具体的保留值
     * @return 此关键字对应的日志保留天数
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
}