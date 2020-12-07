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

import com.alipay.sofa.tracer.plugins.datasource.tracer.Endpoint;
import com.alipay.sofa.tracer.plugins.datasource.utils.DataSourceUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 *
 * @author chenchen6    2020/8/22 13:28
 * @since
 */
public class OracleTnsTest {

    private String jdbcUrl1  = "jdbc:oracle:thin:@(DESCRIPTION=(FAILOVER=on)(ADDRESS=(PROTOCOL=tcp)(HOST=tns1-svr)(PORT=1521))(ADDRESS=(PROTOCOL=tcp)(HOST=tns2-svr)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=tnsServiceName)))";
    private String jdbcUrl3  = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=tns3-svr)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=tns3ServiceName))";
    private String jdbcUrl12 = "jdbc@:oracle:thin:";

    @Test
    public void testTnsDbType() {
        Assert.assertTrue("oracle".equals(DataSourceUtils.resolveDbTypeFromUrl(jdbcUrl1)));
        Assert.assertTrue("oracle".equals(DataSourceUtils.resolveDbTypeFromUrl(jdbcUrl3)));

        boolean flagError = false;
        try {
            DataSourceUtils.resolveDbTypeFromUrl(jdbcUrl12);
        } catch (Throwable throwable) {
            flagError = true;
        }
        Assert.assertTrue(flagError);
    }

    @Test
    public void testDataBase() {
        Assert
            .assertTrue("tnsServiceName".equals(DataSourceUtils.resolveDatabaseFromUrl(jdbcUrl1)));
        Assert
            .assertTrue("tns3ServiceName".equals(DataSourceUtils.resolveDatabaseFromUrl(jdbcUrl3)));
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
