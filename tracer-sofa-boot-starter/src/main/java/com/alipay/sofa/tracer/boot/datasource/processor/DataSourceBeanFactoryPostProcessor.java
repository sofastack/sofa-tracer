/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.alipay.sofa.tracer.boot.datasource.processor;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.datasource.SmartDataSource;
import com.alipay.sofa.tracer.plugins.datasource.utils.DataSourceUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.alipay.common.tracer.core.configuration.SofaTracerConfiguration.TRACER_APPNAME_KEY;

/**
 * @author qilong.zql
 * @since 2.2.0
 */
public class DataSourceBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    public static final String SOFA_TRACER_DATASOURCE = "s_t_d_s_";


    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (String beanName : getBeanNames(beanFactory, DataSource.class)) {
            if (beanName.startsWith(SOFA_TRACER_DATASOURCE)) {
                continue;
            }
            BeanDefinition dataSource = getBeanDefinition(beanName, beanFactory);
            if(DataSourceUtils.isDruidDataSource(dataSource.getBeanClassName())) {
                createDruidDataSourceProxy(beanFactory, beanName, dataSource);
            } else if (DataSourceUtils.isC3p0DataSource(dataSource.getBeanClassName())) {
                createC3P0DataSourceProxy(beanFactory, beanName, dataSource);
            }
        }
    }

    private Iterable<String> getBeanNames(ListableBeanFactory beanFactory, Class clazzType) {
        Set<String> names = new HashSet<>();
        names.addAll(Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                beanFactory, clazzType, true, false)));
        return names;
    }

    private BeanDefinition getBeanDefinition(String beanName,
                                                    ConfigurableListableBeanFactory beanFactory) {
        try {
            return beanFactory.getBeanDefinition(beanName);
        }
        catch (NoSuchBeanDefinitionException ex) {
            BeanFactory parentBeanFactory = beanFactory.getParentBeanFactory();
            if (parentBeanFactory instanceof ConfigurableListableBeanFactory) {
                return getBeanDefinition(beanName,
                        (ConfigurableListableBeanFactory) parentBeanFactory);
            }
            throw ex;
        }
    }

    private void createDruidDataSourceProxy(ConfigurableListableBeanFactory beanFactory, String beanName, BeanDefinition druidDataSource) {
        // re-register origin datasource bean
        BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry)beanFactory;
        beanDefinitionRegistry.removeBeanDefinition(beanName);
        beanDefinitionRegistry.registerBeanDefinition(transformDatasourceBeanName(beanName), druidDataSource);
        // register proxied datasource
        RootBeanDefinition proxiedBeanDefinition = new RootBeanDefinition(SmartDataSource.class);
        proxiedBeanDefinition.setRole(BeanDefinition.ROLE_APPLICATION);
        proxiedBeanDefinition.setInitMethodName("init");
        proxiedBeanDefinition.setDependsOn(transformDatasourceBeanName(beanName));
        MutablePropertyValues originValues = druidDataSource.getPropertyValues();
        MutablePropertyValues values = new MutablePropertyValues();
        values.add("appName", SofaTracerConfiguration.getProperty(TRACER_APPNAME_KEY));
        values.add("delegate", new RuntimeBeanReference(transformDatasourceBeanName(beanName)));
        values.add("dbType", resolveDbTypeFromUrl((String)originValues.get("url")));
        values.add("database", resolveDbTypeFromUrl((String)originValues.get("url")));
        beanDefinitionRegistry.registerBeanDefinition(beanName, proxiedBeanDefinition);
    }

    private void createC3P0DataSourceProxy(ConfigurableListableBeanFactory beanFactory, String beanName, BeanDefinition c3p0DataSource) {
        // re-register origin datasource bean
        BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry)beanFactory;
        beanDefinitionRegistry.removeBeanDefinition(beanName);
        beanDefinitionRegistry.registerBeanDefinition(transformDatasourceBeanName(beanName), c3p0DataSource);
        // register proxied datasource

    }

    public static String transformDatasourceBeanName(String originName) {
        return SOFA_TRACER_DATASOURCE + originName;
    }

    public static String resolveDbTypeFromUrl(String url) {
        Assert.isTrue(!StringUtils.isBlank(url), "Jdbc url must not be empty!");
        int start = url.indexOf("jdbc:") + "jdbc:".length();
        if(start < "jdbc:".length()) {
            throw new InvalidParameterException("jdbc url is invalid!");
        }
        int end = url.indexOf(":", start);
        if (end < 0) {
            throw new InvalidParameterException("jdbc url is invalid!");
        }
        return url.substring(start, end);
    }

    public static String resolveDatabaseFromUrl(String url) {
        Assert.isTrue(!StringUtils.isBlank(url), "Jdbc url must not be empty!");
        int start = url.lastIndexOf("/");
        if (start < 0) {
            throw new InvalidParameterException("jdbc url is invalid!");
        }
        return url.substring(start + 1);
    }

}