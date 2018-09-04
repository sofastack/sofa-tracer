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
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
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
public class DataSourceBeanFactoryPostProcessor implements BeanFactoryPostProcessor,
                                               EnvironmentAware {

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
            if (DataSourceUtils.isDruidDataSource(dataSource.getBeanClassName())) {
                createDataSourceProxy(beanFactory, beanName, dataSource, getDruidJdbcUrlKey());
            } else if (DataSourceUtils.isC3p0DataSource(dataSource.getBeanClassName())) {
                createDataSourceProxy(beanFactory, beanName, dataSource, getC3p0JdbcUrlKey());
            } else if (DataSourceUtils.isDbcpDataSource(dataSource.getBeanClassName())) {
                createDataSourceProxy(beanFactory, beanName, dataSource, getDbcpJdbcUrlKey());
            } else if (DataSourceUtils.isTomcatDataSource(dataSource.getBeanClassName())) {
                createDataSourceProxy(beanFactory, beanName, dataSource, getTomcatJdbcUrlKey());
            } else if (DataSourceUtils.isHikariDataSource(dataSource.getBeanClassName())) {
                createDataSourceProxy(beanFactory, beanName, dataSource, getHikariJdbcUrlKey());
            }
        }
    }

    private Iterable<String> getBeanNames(ListableBeanFactory beanFactory, Class clazzType) {
        Set<String> names = new HashSet<>();
        names.addAll(Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory,
            clazzType, true, false)));
        return names;
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

    private String getTomcatJdbcUrlKey() {
        return "url";
    }

    private String getDbcpJdbcUrlKey() {
        return "url";
    }

    private String getDruidJdbcUrlKey() {
        return "url";
    }

    private String getC3p0JdbcUrlKey() {
        return "jdbcUrl";
    }

    private String getHikariJdbcUrlKey() {
        return "jdbcUrl";
    }

    private void createDataSourceProxy(ConfigurableListableBeanFactory beanFactory,
                                       String beanName, BeanDefinition originDataSource,
                                       String jdbcUrl) {
        // re-register origin datasource bean
        BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) beanFactory;
        beanDefinitionRegistry.removeBeanDefinition(beanName);
        originDataSource.setPrimary(false);
        beanDefinitionRegistry.registerBeanDefinition(transformDatasourceBeanName(beanName),
            originDataSource);
        // register proxied datasource
        RootBeanDefinition proxiedBeanDefinition = new RootBeanDefinition(SmartDataSource.class);
        proxiedBeanDefinition.setRole(BeanDefinition.ROLE_APPLICATION);
        proxiedBeanDefinition.setPrimary(true);
        proxiedBeanDefinition.setInitMethodName("init");
        proxiedBeanDefinition.setDependsOn(transformDatasourceBeanName(beanName));
        MutablePropertyValues originValues = originDataSource.getPropertyValues();
        MutablePropertyValues values = new MutablePropertyValues();
        String appName = environment.getProperty(TRACER_APPNAME_KEY);
        Assert.isTrue(!StringUtils.isBlank(appName), TRACER_APPNAME_KEY + " must be configured!");
        values.add("appName", appName);
        values.add("delegate", new RuntimeBeanReference(transformDatasourceBeanName(beanName)));
        values.add("dbType",
            resolveDbTypeFromUrl(((TypedStringValue) originValues.get(jdbcUrl)).getValue()));
        values.add("database",
            resolveDatabaseFromUrl(((TypedStringValue) originValues.get(jdbcUrl)).getValue()));
        proxiedBeanDefinition.setPropertyValues(values);
        beanDefinitionRegistry.registerBeanDefinition(beanName, proxiedBeanDefinition);
    }

    public static String transformDatasourceBeanName(String originName) {
        return SOFA_TRACER_DATASOURCE + originName;
    }

    public static String resolveDbTypeFromUrl(String url) {
        Assert.isTrue(!StringUtils.isBlank(url), "Jdbc url must not be empty!");
        int start = url.indexOf("jdbc:") + "jdbc:".length();
        if (start < "jdbc:".length()) {
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
        int end = url.indexOf("?", start);
        if (end != -1) {
            return url.substring(start, end);
        }
        return url.substring(start + 1);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}