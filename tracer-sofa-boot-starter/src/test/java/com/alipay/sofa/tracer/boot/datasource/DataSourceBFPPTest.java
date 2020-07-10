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

import com.alipay.sofa.tracer.plugins.datasource.tracer.Endpoint;
import com.alipay.sofa.tracer.plugins.datasource.utils.DataSourceUtils;
import org.junit.Assert;
import org.junit.Test;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * @author qilong.zql
 * @author chenchen
 * @since 2.2.0
 */
public class DataSourceBFPPTest {

    private String jdbcUrl   = "jdbc:oracle:thin:@localhost:1521:orcl";
    private String jdbcUrl2  = "jdbc:oracle:thin:@//localhost:1521/orcl.city.com";
    private String jdbcUrl1  = "jdbc:oracle:thin:@(DESCRIPTION=(FAILOVER=on)(ADDRESS=(PROTOCOL=tcp)(HOST=tns1-svr)(PORT=1521))(ADDRESS=(PROTOCOL=tcp)(HOST=tns2-svr)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=tnsServiceName)))";
    private String jdbcUrl3  = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=tns3-svr)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=tns3ServiceName))";
    private String jdbcUrl12 = "jdbc@:oracle:thin:";

    private String jdbcUrl4  = "jdbc:mysql://127.0.0.1:3306/imooc?useUnicode=true&amp;characterEncoding=utf-8";
    private String jdbcUrl5  = "jdbc:mysql://127.0.0.1:3306/dataBase";
    private String jdbcUrl9  = "jdbc@:mysql://127.0.0.1:3306/dataBase";
    // SQL Server 2000
    private String jdbcUrl6  = "jdbc:microsoft:sqlserver://localhost:1433; DatabaseName=sample ";
    private String jdbcUrl10 = "invalid:sqlserver://localhost:1433; DatabaseName=sample ";
    // SQL Server 2005
    private String jdbcUrl7  = "jdbc:sqlserver://localhost:1433; DatabaseName=sample ";
    private String jdbcUrl11 = "jdbc://localhost/1433; DatabaseName=sample ";
    private String jdbcUrl8  = "invalid";

    @Test
    public void testDbType() {
        Assert.assertTrue("oracle".equals(DataSourceUtils.resolveDbTypeFromUrl(jdbcUrl)));
        Assert.assertTrue("oracle".equals(DataSourceUtils.resolveDbTypeFromUrl(jdbcUrl2)));
        Assert.assertTrue("oracle".equals(DataSourceUtils.resolveDbTypeFromUrl(jdbcUrl1)));
        Assert.assertTrue("oracle".equals(DataSourceUtils.resolveDbTypeFromUrl(jdbcUrl3)));
        Assert.assertTrue("mysql".equals(DataSourceUtils.resolveDbTypeFromUrl(jdbcUrl4)));
        Assert.assertTrue("mysql".equals(DataSourceUtils.resolveDbTypeFromUrl(jdbcUrl5)));
        Assert.assertTrue("sqlserver".equals(DataSourceUtils.resolveDbTypeFromUrl(jdbcUrl6)));
        Assert.assertTrue("sqlserver".equals(DataSourceUtils.resolveDbTypeFromUrl(jdbcUrl7)));

        boolean error = false;
        try {
            DataSourceUtils.resolveDbTypeFromUrl(jdbcUrl8);
        } catch (InvalidParameterException ex) {
            error = true;
        }
        Assert.assertTrue(error);
        error = false;
        try {
            DataSourceUtils.resolveDbTypeFromUrl(jdbcUrl9);
        } catch (InvalidParameterException ex) {
            error = true;
        }
        Assert.assertTrue(error);
        error = false;
        try {
            DataSourceUtils.resolveDbTypeFromUrl(jdbcUrl10);
        } catch (InvalidParameterException ex) {
            error = true;
        }
        Assert.assertTrue(error);
        error = false;
        try {
            DataSourceUtils.resolveDbTypeFromUrl(jdbcUrl11);
        } catch (InvalidParameterException ex) {
            error = true;
        }
        Assert.assertTrue(error);
        error = false;
        try {
            DataSourceUtils.resolveDbTypeFromUrl(jdbcUrl12);
        } catch (InvalidParameterException ex) {
            error = true;
        }
        Assert.assertTrue(error);
    }

    @Test
    public void testDataBase() {
        Assert.assertTrue("orcl".equals(DataSourceUtils.resolveDatabaseFromUrl(jdbcUrl)));
        Assert.assertTrue("orcl.city.com".equals(DataSourceUtils.resolveDatabaseFromUrl(jdbcUrl2)));
        Assert.assertTrue("tnsServiceName".equals(DataSourceUtils.resolveDatabaseFromUrl(jdbcUrl1)));
        Assert.assertTrue("tns3ServiceName".equals(DataSourceUtils.resolveDatabaseFromUrl(jdbcUrl3)));

        Assert.assertTrue("imooc".equals(DataSourceUtils.resolveDatabaseFromUrl(jdbcUrl4)));
        Assert.assertTrue("dataBase".equals(DataSourceUtils.resolveDatabaseFromUrl(jdbcUrl5)));

        Assert.assertTrue("sample".equals(DataSourceUtils.resolveDatabaseFromUrl(jdbcUrl6)));
        Assert.assertTrue("sample".equals(DataSourceUtils.resolveDatabaseFromUrl(jdbcUrl7)));

        boolean error = false;
        try {
            DataSourceUtils.resolveDbTypeFromUrl(jdbcUrl8);
        } catch (InvalidParameterException ex) {
            error = true;
        }
        Assert.assertTrue(error);
    }

    @Test
    public void testEndpoint() {
        Endpoint endpoint;
        endpoint = DataSourceUtils.getEndpointFromConnectionURL(jdbcUrl);
        Assert.assertEquals("localhost", endpoint.getHost());
        Assert.assertEquals(1521, endpoint.getPort());

        endpoint = DataSourceUtils.getEndpointFromConnectionURL(jdbcUrl2);
        Assert.assertEquals("localhost", endpoint.getHost());
        Assert.assertEquals(1521, endpoint.getPort());

        endpoint = DataSourceUtils.getEndpointFromConnectionURL(jdbcUrl4);
        Assert.assertEquals("127.0.0.1", endpoint.getHost());
        Assert.assertEquals(3306, endpoint.getPort());

        endpoint = DataSourceUtils.getEndpointFromConnectionURL(jdbcUrl5);
        Assert.assertEquals("127.0.0.1", endpoint.getHost());
        Assert.assertEquals(3306, endpoint.getPort());

        endpoint = DataSourceUtils.getEndpointFromConnectionURL(jdbcUrl6);
        Assert.assertEquals("localhost", endpoint.getHost());
        Assert.assertEquals(1433, endpoint.getPort());

        endpoint = DataSourceUtils.getEndpointFromConnectionURL(jdbcUrl7);
        Assert.assertEquals("localhost", endpoint.getHost());
        Assert.assertEquals(1433, endpoint.getPort());
    }

    @Test
    public void testEndpoints() {
        List<Endpoint> endpoints;
        endpoints = DataSourceUtils.getEndpointsFromConnectionURL(jdbcUrl1);
        Assert.assertEquals(2, endpoints.size());
        Assert.assertEquals("tns1-svr:1521", endpoints.get(0).getEndpoint());
        Assert.assertEquals("tns2-svr:1521", endpoints.get(1).getEndpoint());

        endpoints = DataSourceUtils.getEndpointsFromConnectionURL(jdbcUrl3);
        Assert.assertEquals(1, endpoints.size());
        Assert.assertEquals("tns3-svr:1521", endpoints.get(0).getEndpoint());
    }

}