/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.alipay.sofa.tracer.boot.base;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 * @author qilong.zql
 * @since 2.2.1
 */
public class DatasourceBeanDefinitionRegistry implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(HikariDataSource.class);
        AbstractBeanDefinition beanDefinition = definitionBuilder.getRawBeanDefinition();
        beanDefinition.setDestroyMethodName("close");
        beanDefinition.setPrimary(false);

        definitionBuilder.addPropertyValue("driverClassName", "org.h2.Driver");
        definitionBuilder.addPropertyValue("jdbcUrl", "jdbc:mysql://1.1.1.1:3306/xxx?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull");
        definitionBuilder.addPropertyValue("username", "sofa");
        definitionBuilder.addPropertyValue("password", "123456");

        registry.registerBeanDefinition("manualDataSource", definitionBuilder.getRawBeanDefinition());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // ignore
    }
}