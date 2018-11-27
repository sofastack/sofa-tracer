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
package com.sofa.tracer.plugins.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.druid.pool.DruidPooledPreparedStatement;
import com.alibaba.druid.pool.DruidPooledStatement;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.samplers.SofaTracerPercentageBasedSampler;
import com.alipay.sofa.tracer.plugins.datasource.BaseDataSource;
import com.alipay.sofa.tracer.plugins.datasource.DBType;
import com.alipay.sofa.tracer.plugins.datasource.SmartDataSource;
import com.alipay.sofa.tracer.plugins.datasource.tracer.DataSourceClientTracer;
import com.sofa.tracer.plugins.datasource.base.BaseTest;
import org.junit.Before;
import org.mockito.Mock;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author qilong.zql 18/8/27-上午9:23
 */
public class DruidTest extends BaseTest {

    @Mock
    protected DruidDataSource              druidDataSource;

    @Mock
    protected DruidPooledConnection        druidPooledConnection;

    @Mock
    protected DruidPooledStatement         druidPooledStatement;

    @Mock
    protected DruidPooledPreparedStatement druidPooledPreparedStatement;

    public DruidTest() {
    }

    @Before
    public void beforeTestCase() throws Exception {

        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.SAMPLER_STRATEGY_NAME_KEY,
            SofaTracerPercentageBasedSampler.TYPE);
        SofaTracerConfiguration.setProperty(
            SofaTracerConfiguration.SAMPLER_STRATEGY_PERCENTAGE_KEY, "100");

        cn = druidPooledConnection;
        ps = druidPooledPreparedStatement;
        st = druidPooledStatement;
        sqlExecutionMock();
        when(druidDataSource.getUrl()).thenReturn("jdbc:oracle:thin:@//mockJdbcHost:9336");
        when(druidDataSource.getConnection()).thenReturn(druidPooledConnection);
        when(druidPooledConnection.prepareStatement(any(String.class))).thenReturn(
            druidPooledPreparedStatement);
        when(druidPooledConnection.createStatement()).thenReturn(druidPooledStatement);
        dataSource = (BaseDataSource) newDataSource();
    }

    @Override
    protected DataSource newDataSource() {
        dataSource = (BaseDataSource) newDataSourceWithoutInitialization();
        dataSource.init();
        return dataSource;
    }

    @Override
    protected DataSource newDataSourceWithoutInitialization() {
        dataSource = new SmartDataSource(druidDataSource);
        ((SmartDataSource) dataSource).setAppName("mockApp");
        ((SmartDataSource) dataSource).setDatabase("mockDB");
        ((SmartDataSource) dataSource).setDbType(DBType.MYSQL.getName());
        ((SmartDataSource) dataSource).setClientTracer(DataSourceClientTracer
            .getDataSourceClientTracer());
        return dataSource;
    }
}