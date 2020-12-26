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
package com.alipay.sofa.tracer.boot.datasource.processor;

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
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;
import org.springframework.core.type.classreading.MethodMetadataReadingVisitor;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashSet;

import static com.alipay.common.tracer.core.configuration.SofaTracerConfiguration.TRACER_APPNAME_KEY;
import static com.alipay.common.tracer.core.configuration.SofaTracerConfiguration.TRACER_JDBC_URL_KEY;

/**
 * @author qilong.zql
 * @since 2.2.0
 *
 * Add method
 * {@link com.alipay.sofa.tracer.boot.datasource.processor.DataSourceBeanFactoryPostProcessor#resolveBeanClassName(org.springframework.beans.factory.config.BeanDefinition)}
 *
 * Modified the way to get bean class name from beanDefinition
 * Modified the way to get jdbcUrl. For the beanDefinition scanned by the Java configuration class, use a special way
 *
 * For adapt spring-cloud-config-client
 *
 * @author XCXCXCXCX
 * @since 3.0.8
 */
public class DataSourceBeanFactoryPostProcessor implements BeanFactoryPostProcessor,
                                               PriorityOrdered, EnvironmentAware {

    public static final String SOFA_TRACER_DATASOURCE = "s_t_d_s_";

    private Environment        environment;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
                                                                                   throws BeansException {
        for (String beanName : getBeanNames(beanFactory, DataSource.class)) {
            if (beanName.startsWith(SOFA_TRACER_DATASOURCE)) {
                continue;
            }
            BeanDefinition dataSource = getBeanDefinition(beanName, beanFactory);
            String beanClassName = resolveBeanClassName(dataSource);
            if (DataSourceUtils.isDruidDataSource(beanClassName)) {
                createDataSourceProxy(beanFactory, beanName, dataSource,
                    DataSourceUtils.getDruidJdbcUrlKey());
            } else if (DataSourceUtils.isC3p0DataSource(beanClassName)) {
                createDataSourceProxy(beanFactory, beanName, dataSource,
                    DataSourceUtils.getC3p0JdbcUrlKey());
            } else if (DataSourceUtils.isDbcpDataSource(beanClassName)) {
                createDataSourceProxy(beanFactory, beanName, dataSource,
                    DataSourceUtils.getDbcpJdbcUrlKey());
            } else if (DataSourceUtils.isTomcatDataSource(beanClassName)) {
                createDataSourceProxy(beanFactory, beanName, dataSource,
                    DataSourceUtils.getTomcatJdbcUrlKey());
            } else if (DataSourceUtils.isHikariDataSource(beanClassName)) {
                createDataSourceProxy(beanFactory, beanName, dataSource,
                    DataSourceUtils.getHikariJdbcUrlKey());
            }
        }
    }

    private String resolveBeanClassName(BeanDefinition dataSource) {
        String beanClassName = dataSource.getBeanClassName();
        if (beanClassName != null) {
            return beanClassName;
        }
        Object source = dataSource.getSource();
        if (source instanceof MethodMetadataReadingVisitor) {
            return ((MethodMetadataReadingVisitor) source).getReturnTypeName();
        }
        return null;
    }

    private Iterable<String> getBeanNames(ListableBeanFactory beanFactory, Class clazzType) {
        return new HashSet<>(Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
            beanFactory, clazzType, true, false)));
    }

    private BeanDefinition getBeanDefinition(String beanName,
                                             ConfigurableListableBeanFactory beanFactory) {
        try {
            return beanFactory.getBeanDefinition(beanName);
        } catch (NoSuchBeanDefinitionException ex) {
            BeanFactory parentBeanFactory = beanFactory.getParentBeanFactory();
            if (parentBeanFactory instanceof ConfigurableListableBeanFactory) {
                return getBeanDefinition(beanName,
                    (ConfigurableListableBeanFactory) parentBeanFactory);
            }
            throw ex;
        }
    }

    private void createDataSourceProxy(ConfigurableListableBeanFactory beanFactory,
                                       String beanName, BeanDefinition originDataSource,
                                       String jdbcUrlKey) {
        // re-register origin datasource bean
        BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) beanFactory;
        beanDefinitionRegistry.removeBeanDefinition(beanName);
        boolean isPrimary = originDataSource.isPrimary();
        originDataSource.setPrimary(false);
        beanDefinitionRegistry.registerBeanDefinition(transformDatasourceBeanName(beanName),
            originDataSource);
        // register proxied datasource
        RootBeanDefinition proxiedBeanDefinition = new RootBeanDefinition(SmartDataSource.class);
        proxiedBeanDefinition.setRole(BeanDefinition.ROLE_APPLICATION);
        proxiedBeanDefinition.setPrimary(isPrimary);
        proxiedBeanDefinition.setInitMethodName("init");
        proxiedBeanDefinition.setDependsOn(transformDatasourceBeanName(beanName));
        MutablePropertyValues originValues = originDataSource.getPropertyValues();

        MutablePropertyValues values = new MutablePropertyValues();
        String appName = environment.getProperty(TRACER_APPNAME_KEY);
        Assert.isTrue(!StringUtils.isBlank(appName), TRACER_APPNAME_KEY + " must be configured!");
        String jdbcUrl = environment.getProperty(TRACER_JDBC_URL_KEY,
            String.valueOf(originValues.get(jdbcUrlKey)));
        String dbType = DataSourceUtils.resolveDbTypeFromUrl(unwrapPropertyValue(jdbcUrl));
        String database = DataSourceUtils.resolveDatabaseFromUrl(unwrapPropertyValue(jdbcUrl));

        values.add("appName", appName);
        values.add("delegate", new RuntimeBeanReference(transformDatasourceBeanName(beanName)));
        values.add("dbType", dbType);
        values.add("database", database);
        proxiedBeanDefinition.setPropertyValues(values);
        beanDefinitionRegistry.registerBeanDefinition(beanName, proxiedBeanDefinition);
    }

    protected String unwrapPropertyValue(Object propertyValue) {
        if (propertyValue instanceof TypedStringValue) {
            return ((TypedStringValue) propertyValue).getValue();
        } else if (propertyValue instanceof String) {
            return (String) propertyValue;
        }
        throw new IllegalArgumentException(
            "The property value of jdbcUrl must be the type of String or TypedStringValue");
    }

    public static String transformDatasourceBeanName(String originName) {
        return SOFA_TRACER_DATASOURCE + originName;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}