/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.sofa.tracer.plugins.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.druid.pool.DruidPooledPreparedStatement;
import com.alibaba.druid.pool.DruidPooledStatement;
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
    protected DruidDataSource druidDataSource;

    @Mock
    protected DruidPooledConnection druidPooledConnection;

    @Mock
    protected DruidPooledStatement druidPooledStatement;

    @Mock
    protected DruidPooledPreparedStatement druidPooledPreparedStatement;

    @Before
    public void beforeTestCase() throws Exception{
        when(druidDataSource.getUrl()).thenReturn("jdbc:oracle:thin:@//mockJdbcHost:9336");
        when(druidDataSource.getConnection()).thenReturn(druidPooledConnection);
        when(druidPooledConnection.prepareStatement(any(String.class))).thenReturn(druidPooledPreparedStatement);
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
        ((SmartDataSource) dataSource).setAppName("druidMockApp");
        ((SmartDataSource) dataSource).setDatabase("druidMockDB");
        ((SmartDataSource) dataSource).setDbType(DBType.MYSQL.getName());
        ((SmartDataSource) dataSource).setClientTracer(DataSourceClientTracer.getDataSourceClientTracer());
        return dataSource;
    }
}