/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.sofa.tracer.plugins.datasource.base;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.druid.pool.DruidPooledPreparedStatement;
import com.alibaba.druid.pool.DruidPooledStatement;
import com.alipay.sofa.tracer.plugins.datasource.BaseDataSource;
import com.alipay.sofa.tracer.plugins.datasource.Interceptor;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @since 2.2.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.class)
public abstract class BaseTest {

    public static final String USER_HOME             = System.getProperty("user.home");
    public static final String DATASOURCE_CLIENT_DIGEST_LOG_FILE = USER_HOME
            + "/logs/tracelog/datasource-client-digest.log";

    protected BaseDataSource dataSource = null;

    @Mock
    protected Connection cn;

    @Mock
    protected PreparedStatement ps;

    @Mock
    protected Statement st;

    protected abstract DataSource newDataSource();

    protected abstract DataSource newDataSourceWithoutInitialization();

    @After
    public void afterTestCase() throws SQLException {
        if (ps != null) {
            ps.close();
        }
        if (st != null) {
            st.close();
        }
        if (cn != null) {
            cn.close();
        }
    }

    protected void initLogContext() {
        try {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void clearLogContext() {
        try {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test_select_statement() throws SQLException {
        try {
            initLogContext();
            cn = dataSource.getConnection();
            st = cn.createStatement();
            ResultSet rs = st.executeQuery("select id, username from mars");
            Assert.assertTrue("数据检查", rs.next());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clearLogContext();
        }
    }

    @Test
    @Ignore
    public void test_insert_statement() throws Exception {
        try {
            initLogContext();
            cn = dataSource.getConnection();
            st = cn.createStatement();
            int result = st
                    .executeUpdate("insert into mars (id, gmt_create, gmt_modified, username) \n"
                            + "values (100, now(), now(), 'neo')");
            Assert.assertTrue("数据检查", result == 1);
            ResultSet rs = st.executeQuery("select username from mars where id=100");
            Assert.assertTrue("数据检查", rs.next());
            Assert.assertEquals("结果检查", rs.getString(1), "neo");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            clearLogContext();
        }
    }

    @Test
    @Ignore
    public void test_insert_preparedStatement() throws Exception {
        try {
            initLogContext();
            cn = dataSource.getConnection();
            st = cn.createStatement();
            ps = cn
                    .prepareStatement("insert into mars (id, gmt_create, username) values (?, ?, ?)");
            ps.setInt(1, 101);
            ps.setDate(2, new Date(System.currentTimeMillis()));
            ps.setString(3, "frank");
            int result = ps.executeUpdate();
            Assert.assertTrue("数据检查", result == 1);
            ResultSet rs = st.executeQuery("select username from mars where id=101");
            Assert.assertTrue("数据检查", rs.next());
            Assert.assertEquals("结果检查", rs.getString(1), "frank");
            st.execute("delete from mars where id=101");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clearLogContext();
        }
    }

    @Test
    public void test_statement_with_interceptor() throws Exception {
        try {
            initLogContext();
            dataSource.setInterceptors(Collections
                    .<Interceptor> singletonList(new FakeInterceptor()));
            dataSource.init();
            cn = dataSource.getConnection();
            st = cn.createStatement();
            ResultSet rs = st.executeQuery("select * from mars");
            Assert.assertTrue("数据检查", rs.next());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clearLogContext();
        }
    }

    @Test
    public void test_prepare_statement() throws Exception {
        try {
            initLogContext();
            cn = dataSource.getConnection();
            ps = cn.prepareStatement("select * from mars");
            ResultSet rs = ps.executeQuery();
            Assert.assertTrue("数据检查", rs.next());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clearLogContext();
        }
    }

    @Test
    public void test_ps_batch() throws SQLException {
        try {
            initLogContext();
            cn = dataSource.getConnection();
            st = cn.createStatement();
            ps = cn
                    .prepareStatement("insert into mars (id, gmt_create, username) values (?, ?, ?)");
            ps.setLong(1, 101);
            ps.setDate(2, new Date(System.currentTimeMillis()));
            ps.setString(3, "frank");
            ps.addBatch();

            ps.setLong(1, 102);
            ps.setDate(2, new Date(System.currentTimeMillis()));
            ps.setString(3, "bryce");
            ps.addBatch();

            int[] results = ps.executeBatch();
            Assert.assertTrue("数据检查", results.length == 2);
            ResultSet rs = st.executeQuery("select username from mars where id=101");
            Assert.assertTrue("数据检查", rs.next());
            Assert.assertEquals("结果检查", rs.getString(1), "frank");

            rs = st.executeQuery("select username from mars where id=102");
            Assert.assertTrue("数据检查", rs.next());
            Assert.assertEquals("结果检查", rs.getString(1), "bryce");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clearLogContext();
        }
    }

    @Test
    @Ignore
    public void test_ps_execute() throws SQLException {
        try {
            initLogContext();
            cn = dataSource.getConnection();
            st = cn.createStatement();
            ps = cn
                    .prepareStatement("insert into mars (id, gmt_create, username) values (?, ?, ?)");
            ps.setLong(1, 101);
            ps.setDate(2, new Date(System.currentTimeMillis()));
            ps.setString(3, "frank");
            Assert.assertTrue("ps.execute", !ps.execute());
            Assert.assertTrue("updateCount", 1 == ps.getUpdateCount());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clearLogContext();
        }
    }

    @Test
    public void test_executeUpdate_in_batch_mode() throws SQLException {
        try {
            cn = dataSource.getConnection();
            ps = cn
                    .prepareStatement("insert into mars (id, gmt_create, gmt_modified, username) values (?, ?, ?, ?)");
            ps.setLong(1, 100000);
            ps.setDate(2, new Date(System.currentTimeMillis()));
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.setString(4, "frank");
            ps.addBatch();

            ps.setLong(1, 100001);
            ps.setDate(2, new Date(System.currentTimeMillis()));
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.setString(4, "bryce");
            ps.addBatch();
            ps.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
    }

    @Test
    public void test_prepare_statement_disable_trace() throws Exception {
        try {
            initLogContext();
            dataSource = (BaseDataSource) newDataSourceWithoutInitialization();
            dataSource.init();
            cn = dataSource.getConnection();
            ps = cn.prepareStatement("select * from mars");
            ResultSet rs = ps.executeQuery();
            Assert.assertTrue("数据检查", rs.next());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clearLogContext();
        }
    }

    @Test
    @Ignore
    public void test_zzz_tracelog() throws SQLException {
        try {
            initLogContext();
            cn = dataSource.getConnection();
            ps = cn.prepareStatement("select * from mars");
            ResultSet rs = ps.executeQuery();
            Assert.assertTrue("数据检查", rs.next());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clearLogContext();
        }
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<String> lines = readLines(DATASOURCE_CLIENT_DIGEST_LOG_FILE);
        String thisLog = lines.get(lines.size() - 1);
        String[] contents = thisLog.split(",");
        Assert.assertTrue(contents[1].equals("testApp"));
        Assert.assertTrue(contents[2].length() > 0);
        Assert.assertTrue(contents[3].equals("0.1"));
        Assert.assertTrue(contents[4].equals("testDb"));
        Assert.assertTrue(contents[5].equals("select * from mars"));
        Assert.assertTrue(contents[6].equals("success"));
        Assert.assertTrue(parseMs(contents[7]) > 0);
        Assert.assertTrue(parseMs(contents[8]) >= 0);
        Assert.assertTrue(parseMs(contents[9]) > 0);
        Assert.assertTrue(contents[10].equals("main"));
    }

    private Integer parseMs(String str) {
        String num = str.substring(0, str.length() - 2);
        return Integer.valueOf(num);
    }

    public static List<String> readLines(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return Collections.emptyList();
        }
        try {
            return FileUtils.readLines(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
