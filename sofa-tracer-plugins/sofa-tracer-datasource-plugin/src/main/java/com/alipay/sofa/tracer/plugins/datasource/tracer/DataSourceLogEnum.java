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
public enum DataSourceLogEnum {
    // DataSource Client Log
    DATA_SOURCE_CLIENT_DIGEST("datasource_client_digest_log_name", "datasource-client-digest.log",
                              "datasource_client_digest_rolling"), DATA_SOURCE_CLIENT_STAT(
                                                                                           "datasource_client_stat_log_name",
                                                                                           "datasource-client-stat.log",
                                                                                           "datasource_client_stat_rolling");

    private String logNameKey;
    private String defaultLogName;
    private String rollingKey;

    DataSourceLogEnum(String logNameKey, String defaultLogName, String rollingKey) {
        this.logNameKey = logNameKey;
        this.defaultLogName = defaultLogName;
        this.rollingKey = rollingKey;
    }

    public String getLogNameKey() {
        //log reserve config key
        return logNameKey;
    }

    public String getDefaultLogName() {
        return defaultLogName;
    }

    public String getRollingKey() {
        return rollingKey;
    }
}