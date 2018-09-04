/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.alipay.sofa.tracer.boot.datasource.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author qilong.zql 18/9/4-下午1:41
 */
@ConfigurationProperties("com.alipay.sofa.tracer.datasource")
public class SofaTracerDataSourceProperties {
    private boolean enableTrace;

    public boolean isEnableTrace() {
        return enableTrace;
    }

    public void setEnableTrace(boolean enableTrace) {
        this.enableTrace = enableTrace;
    }
}