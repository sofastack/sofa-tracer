/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.alipay.sofa.tracer.boot.base;

import com.alipay.sofa.tracer.boot.properties.SofaTracerProperties;

/**
 * @author qilong.zql
 * @since 2.2.2
 */
public class ConfigurationHolder {
    public static SofaTracerProperties sofaTracerProperties;

    public static SofaTracerProperties getSofaTracerProperties() {
        return sofaTracerProperties;
    }

    public static void setSofaTracerProperties(SofaTracerProperties sofaTracerProperties) {
        ConfigurationHolder.sofaTracerProperties = sofaTracerProperties;
    }
}