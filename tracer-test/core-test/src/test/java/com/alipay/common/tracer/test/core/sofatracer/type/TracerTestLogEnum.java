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
package com.alipay.common.tracer.test.core.sofatracer.type;

/**
 * TracerSystemLogEnum
 *
 * @author yangguanchao
 * @since 2017/06/27
 */
public enum TracerTestLogEnum {

    // digest
    RPC_CLIENT_DIGEST("rpc_client_log_name_reserve", "rpc_client_digest.log", "rpc_client_rolling"), RPC_SERVER_DIGEST(
                                                                                                                       "rpc_server_log_name_reserve",
                                                                                                                       "rpc_server_digest.log",
                                                                                                                       "rpc_server_rolling"),

    // stat
    RPC_CLIENT_STAT("rpc_client_log_name_reserve", "rpc_client_stat.log", "rpc_client_rolling"), RPC_SERVER_STAT(
                                                                                                                 "rpc_server_log_name_reserve",
                                                                                                                 "rpc_server_stat.log",
                                                                                                                 "rpc_server_rolling"), ;
    private String logReverseKey;

    private String defaultLogName;

    private String rollingKey;

    TracerTestLogEnum(String logReverseKey, String defaultLogName, String rollingKey) {
        this.logReverseKey = logReverseKey;
        this.defaultLogName = defaultLogName;
        this.rollingKey = rollingKey;
    }

    public String getLogReverseKey() {
        return logReverseKey;
    }

    public String getDefaultLogName() {
        return defaultLogName;
    }

    public String getRollingKey() {
        return rollingKey;
    }
}
