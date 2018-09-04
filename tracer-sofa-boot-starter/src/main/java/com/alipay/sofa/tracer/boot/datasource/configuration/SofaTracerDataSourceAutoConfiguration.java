/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.alipay.sofa.tracer.boot.datasource.configuration;

import com.alipay.sofa.tracer.boot.datasource.processor.DataSourceBeanFactoryPostProcessor;
import com.alipay.sofa.tracer.boot.datasource.properties.SofaTracerDataSourceProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author qilong.zql
 * @since 2.2.0
 */
@Configuration
@EnableConfigurationProperties(SofaTracerDataSourceProperties .class)
public class SofaTracerDataSourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value="om.alipay.sofa.tracer.datasource.enableTracer", matchIfMissing=true)
    public DataSourceBeanFactoryPostProcessor DataSourceBeanFactoryPostProcessor() {
        return new DataSourceBeanFactoryPostProcessor();
    }
}