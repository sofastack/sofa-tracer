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

import com.alipay.common.tracer.core.utils.ReflectionUtils;
import com.alipay.sofa.tracer.plugins.datasource.tracer.Endpoint;
import com.alipay.sofa.tracer.plugins.datasource.utils.DataSourceUtils;
import com.alipay.sofa.tracer.plugins.datasource.utils.SqlUtils;
import com.sofa.tracer.plugins.datasource.bean.ConcreteClassService;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author qilong.zql
 * @since 2.3.2
 */
public class DataSourceUtilTest {
    @Test
    public void testReflectionUtils() throws Throwable {
        ConcreteClassService concreteClassService = new ConcreteClassService();
        Method method = ReflectionUtils.findMethod(concreteClassService.getClass(), "service");
        Assert.assertEquals("concreteClassService", method.invoke(concreteClassService));
        method = ReflectionUtils.findMethod(concreteClassService.getClass(), "serviceA");
        Assert.assertEquals("serviceA", method.invoke(concreteClassService));
        method = ReflectionUtils.findMethod(concreteClassService.getClass(), "serviceB");
        Assert.assertEquals("serviceB", method.invoke(concreteClassService));
    }

    @Test
    public void testGetSqlEscaped() {
        // start with blank and special character
        String str1 = "  select app1\n" + ",app2\r\n" + " from table where          " + " id = a;";
        String result = SqlUtils.getSqlEscaped(str1);
        Assert.assertTrue(!result.contains("\n"));
        Assert.assertTrue(!result.contains("\r"));
        Assert.assertTrue(result.equals("select app1 %2Capp2 from table where id = a;"));
        Assert.assertTrue(!result.startsWith(" "));
        // normal string
        String str2 = "select app1 ,app2 from table where id = a;";
        result = SqlUtils.getSqlEscaped(str2);
        Assert.assertTrue(!result.contains("\n"));
        Assert.assertTrue(!result.contains("\r"));
        Assert.assertTrue(result.equals("select app1 %2Capp2 from table where id = a;"));
    }

    @Test
    public void testGetEndpointsFromConnectionURL() {
        String mysqlStr = "jdbc:mysql://127.0.0.1:5432";
        List<Endpoint> mysqlEndpoints = DataSourceUtils.getEndpointsFromConnectionURL(mysqlStr);
        Assert.assertTrue(mysqlEndpoints.size() == 1);
        Assert.assertEquals(mysqlEndpoints.get(0).getHost(), "127.0.0.1");
        Assert.assertEquals(mysqlEndpoints.get(0).getPort(), 5432);

        String pgStr = "jdbc:postgresql://pgm-b1j86r7w2287y711.pg.rds.ops.cloudtest.jxnxs.com:3433,pgr-b1jyp8yi35bj7267.pg.rds.ops.cloudtest.jxnxs.com:3306/postgres?serverTimezone=GMT%2B8&allowMultiQueries=true";
        List<Endpoint> pgEndpoints = DataSourceUtils.getEndpointsFromConnectionURL(pgStr);
        Assert.assertTrue(pgEndpoints.size() == 2);
        Assert.assertEquals(pgEndpoints.get(0).getHost(),
            "pgm-b1j86r7w2287y711.pg.rds.ops.cloudtest.jxnxs.com");
        Assert.assertEquals(pgEndpoints.get(0).getPort(), 3433);
        Assert.assertEquals(pgEndpoints.get(1).getHost(),
            "pgr-b1jyp8yi35bj7267.pg.rds.ops.cloudtest.jxnxs.com");
        Assert.assertEquals(pgEndpoints.get(1).getPort(), 3306);
    }
}