/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.sofa.tracer.plugins.datasource.base;

import com.alibaba.druid.mock.MockResultSet;
import com.alipay.sofa.tracer.plugins.datasource.BaseDataSource;
import com.alipay.sofa.tracer.plugins.datasource.DBType;
import com.alipay.sofa.tracer.plugins.datasource.Interceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;

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

    protected void sqlExecutionMock() throws Exception{
        when(ps.executeQuery()).thenReturn(new MockResultSet(null, Arrays.asList(new Object[][]{{"数据检查"}})));
        when(ps.executeBatch()).thenReturn(new int[]{0, 1});
        when(ps.executeUpdate()).thenReturn(1);
        when(ps.execute()).thenReturn(false);
        when(ps.getUpdateCount()).thenReturn(1);
        when(st
                .executeUpdate("insert into mars (id, gmt_create, gmt_modified, username) \n"
                        + "values (100, now(), now(), 'neo')")).thenReturn(1);
        when(st.executeQuery("select username from mars where id=100")).thenReturn(new MockResultSet(null, Arrays.asList(new Object[][]{{"数据检查", "neo"}})));
        when(st.executeQuery("select username from mars where id=101")).thenReturn(new MockResultSet(null, Arrays.asList(new Object[][]{{"frank"}})));
        when(st.executeQuery("select username from mars where id=102")).thenReturn(new MockResultSet(null, Arrays.asList(new Object[][]{{"bryce"}})));
        when(st.executeQuery("select id, username from mars")).thenReturn(new MockResultSet(null, Arrays.asList(new Object[][]{{"数据检查"}})));
        when(st.executeQuery("select * from mars")).thenReturn(new MockResultSet(null, Arrays.asList(new Object[][]{{"数据检查"}})));
    }

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

    @Test
    public void test_select_statement() throws SQLException {
        try {
            cn = dataSource.getConnection();
            st = cn.createStatement();
            ResultSet rs = st.executeQuery("select id, username from mars");
            Assert.assertTrue("数据检查", rs.next());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_insert_statement() throws Exception {
        try {
            cn = dataSource.getConnection();
            st = cn.createStatement();
            int result = st
                    .executeUpdate("insert into mars (id, gmt_create, gmt_modified, username) \n"
                            + "values (100, now(), now(), 'neo')");
            Assert.assertTrue("数据检查", result == 1);
            ResultSet rs = st.executeQuery("select username from mars where id=100");
            Assert.assertTrue("数据检查", rs.next());
            Assert.assertEquals("结果检查", rs.getString(2), "neo");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void test_insert_preparedStatement() throws Exception {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_statement_with_interceptor() throws Exception {
        try {
            dataSource.setInterceptors(Collections
                    .<Interceptor> singletonList(new FakeInterceptor()));
            dataSource.init();
            cn = dataSource.getConnection();
            st = cn.createStatement();
            ResultSet rs = st.executeQuery("select * from mars");
            Assert.assertTrue("数据检查", rs.next());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_prepare_statement() throws Exception {
        try {
            cn = dataSource.getConnection();
            ps = cn.prepareStatement("select * from mars");
            ResultSet rs = ps.executeQuery();
            Assert.assertTrue("数据检查", rs.next());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void test_ps_batch() throws SQLException {
        try {
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
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void test_ps_execute() throws SQLException {
        try {
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
            Assert.assertNotNull(e);
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
            Assert.assertNotNull(e);
            throw e;
        }
    }

    @Test
    public void test_prepare_statement_disable_trace() throws Exception {
        try {
            dataSource = (BaseDataSource) newDataSourceWithoutInitialization();
            dataSource.init();
            cn = dataSource.getConnection();
            ps = cn.prepareStatement("select * from mars");
            ResultSet rs = ps.executeQuery();
            Assert.assertTrue("数据检查", rs.next());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test_zzz_tracelog() throws Exception {
        try {
            cn = dataSource.getConnection();
            ps = cn.prepareStatement("select * from mars");
            ResultSet rs = ps.executeQuery();
            Assert.assertTrue("数据检查", rs.next());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<String> lines = readLines(DATASOURCE_CLIENT_DIGEST_LOG_FILE);
        String thisLog = lines.get(lines.size() - 1);
        ObjectMapper mapper = new ObjectMapper();
        Map result = mapper.readValue(thisLog, Map.class);
        Assert.assertTrue(result.get("local.app").equals("mockApp"));
        Assert.assertTrue(result.get("database.name").equals("mockDB"));
        Assert.assertTrue(result.get("sql").equals("select * from mars"));
        Assert.assertTrue(result.get("result.code").equals("success"));
        String totalTime = result.get("total.time").toString();
        Assert.assertTrue(totalTime.endsWith("ms"));
        Assert.assertTrue(Integer.valueOf(totalTime.substring(0, totalTime.length() - 2)) >= 0);
        Assert.assertTrue(result.get("database.type").equals(DBType.MYSQL.getName()));
        Assert.assertTrue(result.get("database.endpoint").equals("mockJdbcHost:9336"));
        Assert.assertTrue(result.get("result.code").equals("success"));
        Assert.assertTrue(result.get("current.thread.name").equals("main"));


//        Assert.assertTrue(contents[10].equals("main"));
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
