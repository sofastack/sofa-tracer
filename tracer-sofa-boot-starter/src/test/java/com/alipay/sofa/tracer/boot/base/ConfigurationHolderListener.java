/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.alipay.sofa.tracer.boot.base;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.sofa.tracer.boot.properties.SofaTracerProperties;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author qilong.zql
 * @since 2.2.2
 */
public class ConfigurationHolderListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        SofaTracerProperties sofaTracerProperties = new SofaTracerProperties();
        sofaTracerProperties.setDisableDigestLog(SofaTracerConfiguration.getProperty(SofaTracerConfiguration.DISABLE_MIDDLEWARE_DIGEST_LOG_KEY));
        sofaTracerProperties.setDisableConfiguration(SofaTracerConfiguration.getMapEmptyIfNull(SofaTracerConfiguration.DISABLE_DIGEST_LOG_KEY));
        sofaTracerProperties.setTracerGlobalRollingPolicy(SofaTracerConfiguration.getProperty(SofaTracerConfiguration.TRACER_GLOBAL_ROLLING_KEY));
        sofaTracerProperties.setTracerGlobalLogReserveDay(SofaTracerConfiguration.getProperty(SofaTracerConfiguration.TRACER_GLOBAL_LOG_RESERVE_DAY));
        sofaTracerProperties.setStatLogInterval(SofaTracerConfiguration.getProperty(SofaTracerConfiguration.STAT_LOG_INTERVAL));
        sofaTracerProperties.setBaggageMaxLength(SofaTracerConfiguration.getProperty(SofaTracerConfiguration.TRACER_PENETRATE_ATTRIBUTE_MAX_LENGTH));
        ConfigurationHolder.setSofaTracerProperties(sofaTracerProperties);
    }
}