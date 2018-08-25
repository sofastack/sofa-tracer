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
package com.alipay.sofa.tracer.plugins.datasource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @sicne 2.1.3
 */
public class DBType {

    public static DBType MYSQL      = new DBType("MYSQL");
    public static DBType ORACLE     = new DBType("ORACLE");
    public static DBType DB2        = new DBType("DB2");
    public static DBType HBASE      = new DBType("HBASE");
    public static DBType NEO4J      = new DBType("NEO4J");
    public static DBType HSQL       = new DBType("HSQL");
    public static DBType H2         = new DBType("H2");
    public static DBType DERBY      = new DBType("DERBY");
    public static DBType POSTGRESQL = new DBType("POSTGRESQL");
    public static DBType SQLSERVER  = new DBType("SQLSERVER");
    public static DBType OCEANBASE  = new DBType("OCEANBASE");
    public static DBType DBP        = new DBType("DBP");
    public static DBType MOCK       = new DBType("MOCK");

    private final String name;

    DBType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    static Map<String, DBType> supportedDbTypes = new HashMap<String, DBType>();

    static {
        supportedDbTypes.put("MYSQL", MYSQL);
        supportedDbTypes.put("ORACLE", ORACLE);
        supportedDbTypes.put("DB2", DB2);
        supportedDbTypes.put("HBASE", HBASE);
        supportedDbTypes.put("NEO4J", NEO4J);
        supportedDbTypes.put("HSQL", HSQL);
        supportedDbTypes.put("H2", H2);
        supportedDbTypes.put("DERBY", DERBY);
        supportedDbTypes.put("POSTGRESQL", POSTGRESQL);
        supportedDbTypes.put("SQLSERVER", SQLSERVER);
        supportedDbTypes.put("OCEANBASE", OCEANBASE);
        supportedDbTypes.put("DBP", DBP);
    }

    public static Set<String> getSupportedDbTypes() {
        return supportedDbTypes.keySet();
    }
}