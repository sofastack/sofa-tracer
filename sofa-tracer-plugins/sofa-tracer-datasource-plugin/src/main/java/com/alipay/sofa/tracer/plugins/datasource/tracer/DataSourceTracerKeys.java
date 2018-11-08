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
package com.alipay.sofa.tracer.plugins.datasource.tracer;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @since 2.2.0
 */
public class DataSourceTracerKeys {
    public static final String CONNECTION_ESTABLISH_COST = "connection.establish.span";

    public static final String DB_EXECUTE_COST           = "db.execute.cost";

    public static final String SQL                       = "sql";

    public static final String DATABASE_TYPE             = "database.type";

    public static final String DATABASE_NAME             = "database.name";

    public static final String DATABASE_ENDPOINT         = "database.endpoint";

    public static final String LOCAL_APP                 = "local.app";

    public static final String RESULT_CODE               = "result.code";

    public static final String START_TIME                = "start.time";

    public static final String TOTAL_TIME                = "total.time";
}