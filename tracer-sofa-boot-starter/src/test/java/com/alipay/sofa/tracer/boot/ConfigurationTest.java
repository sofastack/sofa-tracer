/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.alipay.sofa.tracer.boot;

import com.alipay.common.tracer.core.appender.file.TimedRollingFileAppender;
import com.alipay.sofa.tracer.boot.base.AbstractTestBase;
import com.alipay.sofa.tracer.boot.base.ConfigurationHolder;
import com.alipay.sofa.tracer.boot.properties.SofaTracerProperties;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

/**
 * @author qilong.zql
 * @since 2.2.2
 */
@ActiveProfiles("config")
public class ConfigurationTest extends AbstractTestBase {

    @Test
    public void testAdvanceTracerConfig() {
        SofaTracerProperties sofaTracerProperties = ConfigurationHolder.getSofaTracerProperties();
        Assert.assertEquals("true", sofaTracerProperties.getDisableDigestLog());

        Map<String, String> disableConfiguration = sofaTracerProperties.getDisableConfiguration();
        Assert.assertTrue("v1".equals(disableConfiguration.get("k1")));
        Assert.assertTrue("v2".equals(disableConfiguration.get("k2")));
        Assert.assertEquals(2, disableConfiguration.size());

        Assert.assertEquals(TimedRollingFileAppender.HOURLY_ROLLING_PATTERN, sofaTracerProperties.getTracerGlobalRollingPolicy());
        Assert.assertEquals("1", sofaTracerProperties.getTracerGlobalLogReserveDay());
        Assert.assertEquals("1", sofaTracerProperties.getStatLogInterval());
        Assert.assertEquals("1", sofaTracerProperties.getBaggageMaxLength());
    }

}