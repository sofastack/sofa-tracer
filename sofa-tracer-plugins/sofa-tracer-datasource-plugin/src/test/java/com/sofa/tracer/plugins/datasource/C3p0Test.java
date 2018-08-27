/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.sofa.tracer.plugins.datasource;

import com.alipay.sofa.tracer.plugins.datasource.BaseDataSource;
import com.alipay.sofa.tracer.plugins.datasource.DBType;
import com.alipay.sofa.tracer.plugins.datasource.SmartDataSource;
import com.alipay.sofa.tracer.plugins.datasource.tracer.DataSourceClientTracer;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.sofa.tracer.plugins.datasource.base.BaseTest;
import org.junit.Before;
import org.mockito.Mock;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @sicne 2.2.0
 */
public class C3p0Test extends BaseTest {

    @Mock
    protected ComboPooledDataSource comboPooledDataSource;

    @Before
    public void beforeTestCase() throws Exception{
        when(comboPooledDataSource.getJdbcUrl()).thenReturn("jdbc:oracle:thin:@//mockJdbcHost:9336");
        when(comboPooledDataSource.getConnection()).thenReturn(cn);
        when(cn.prepareStatement(any(String.class))).thenReturn(ps);
        when(cn.createStatement()).thenReturn(st);
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
        dataSource = new SmartDataSource(comboPooledDataSource);
        ((SmartDataSource) dataSource).setAppName("c3p0MockApp");
        ((SmartDataSource) dataSource).setDatabase("c3p0MockDB");
        ((SmartDataSource) dataSource).setDbType(DBType.MYSQL.getName());
        ((SmartDataSource) dataSource).setClientTracer(DataSourceClientTracer.getDataSourceClientTracer());
        return dataSource;
    }
}