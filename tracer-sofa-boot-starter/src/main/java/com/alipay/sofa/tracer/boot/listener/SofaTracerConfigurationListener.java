/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.alipay.sofa.tracer.boot.listener;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.boot.properties.SofaTracerProperties;
import org.springframework.boot.bind.PropertiesConfigurationFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;

/**
 * Parse SOFATracer Configuration in early stage.
 *
 * @author qilong.zql
 * @since 2.2.2
 */
public class SofaTracerConfigurationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, PriorityOrdered {

    public final static String SOFA_TRACER_CONFIGURATION_PREFIX="com.alipay.sofa.tracer";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();

        // check spring.application.name
        String applicationName = environment
                .getProperty(SofaTracerConfiguration.TRACER_APPNAME_KEY);
        Assert.isTrue(!StringUtils.isBlank(applicationName),
                SofaTracerConfiguration.TRACER_APPNAME_KEY + " must be configured!");

        SofaTracerProperties tempTarget = new SofaTracerProperties();
        PropertiesConfigurationFactory<SofaTracerProperties> binder = new PropertiesConfigurationFactory<SofaTracerProperties>(tempTarget);
        binder.setTargetName(SOFA_TRACER_CONFIGURATION_PREFIX);
        binder.setConversionService(new DefaultConversionService());
        binder.setPropertySources(environment.getPropertySources());
        try {
            binder.bindPropertiesToTarget();
        }
        catch (BindException ex) {
            throw new IllegalStateException("Cannot bind to SofaTracerProperties", ex);
        }

        //properties convert to tracer
        SofaTracerConfiguration.setProperty(
                SofaTracerConfiguration.DISABLE_MIDDLEWARE_DIGEST_LOG_KEY,
                tempTarget.getDisableDigestLog());
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.DISABLE_DIGEST_LOG_KEY,
                tempTarget.getDisableConfiguration());
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.TRACER_GLOBAL_ROLLING_KEY,
                tempTarget.getTracerGlobalRollingPolicy());
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.TRACER_GLOBAL_LOG_RESERVE_DAY,
                tempTarget.getTracerGlobalLogReserveDay());
        //stat log interval
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.STAT_LOG_INTERVAL,
                tempTarget.getStatLogInterval());
        //baggage length
        SofaTracerConfiguration.setProperty(
                SofaTracerConfiguration.TRACER_PENETRATE_ATTRIBUTE_MAX_LENGTH,
                tempTarget.getBaggageMaxLength());
        SofaTracerConfiguration.setProperty(
                SofaTracerConfiguration.TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH,
                tempTarget.getBaggageMaxLength());
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 10;
    }
}