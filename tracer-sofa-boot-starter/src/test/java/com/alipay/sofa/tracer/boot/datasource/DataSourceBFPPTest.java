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
package com.alipay.sofa.tracer.boot.datasource;

import com.alipay.sofa.tracer.boot.datasource.processor.DataSourceBeanFactoryPostProcessor;
import org.junit.Test;
import org.springframework.util.Assert;

import java.security.InvalidParameterException;

/**
 * @author qilong.zql
 * @since 2.2.0
 */
public class DataSourceBFPPTest {

    private String jdbcUrl  = "jdbc:oracle:thin:@localhost:1521/orcl";
    private String jdbcUrl2 = "jdbc:oracle:thin:@//localhost:1521/orcl.city.com";
    private String jdbcUrl3 = "jdbc:mysql://127.0.0.1:3306/imooc?useUnicode=true&amp;characterEncoding=utf-8";
    private String jdbcUrl4 = "jdbc:mysql://127.0.0.1:3306/dataBase";
    private String jdbcUrl5 = "invalid";

    @Test
    public void testDbType() {
        Assert.isTrue("oracle".equals(DataSourceBeanFactoryPostProcessor
            .resolveDbTypeFromUrl(jdbcUrl)));
        Assert.isTrue("oracle".equals(DataSourceBeanFactoryPostProcessor
            .resolveDbTypeFromUrl(jdbcUrl2)));
        Assert.isTrue("mysql".equals(DataSourceBeanFactoryPostProcessor
            .resolveDbTypeFromUrl(jdbcUrl3)));
        Assert.isTrue("mysql".equals(DataSourceBeanFactoryPostProcessor
            .resolveDbTypeFromUrl(jdbcUrl4)));

        boolean error = false;
        try {
            DataSourceBeanFactoryPostProcessor.resolveDbTypeFromUrl(jdbcUrl5);
        } catch (InvalidParameterException ex) {
            error = true;
        }
        Assert.isTrue(error);
    }

    @Test
    public void testDataBase() {
        Assert.isTrue("orcl".equals(DataSourceBeanFactoryPostProcessor
            .resolveDatabaseFromUrl(jdbcUrl)));
        Assert.isTrue("orcl.city.com".equals(DataSourceBeanFactoryPostProcessor
            .resolveDatabaseFromUrl(jdbcUrl2)));
        Assert.isTrue("imooc".equals(DataSourceBeanFactoryPostProcessor
            .resolveDatabaseFromUrl(jdbcUrl3)));
        Assert.isTrue("dataBase".equals(DataSourceBeanFactoryPostProcessor
            .resolveDatabaseFromUrl(jdbcUrl4)));

        boolean error = false;
        try {
            DataSourceBeanFactoryPostProcessor.resolveDbTypeFromUrl(jdbcUrl5);
        } catch (InvalidParameterException ex) {
            error = true;
        }
        Assert.isTrue(error);
    }

}