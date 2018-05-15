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
package com.alipay.common.tracer.core.reporter.type;

/**
 * TracerSystemLogEnum
 *
 * @author yangguanchao
 * @since 2017/06/27
 */
public enum TracerSystemLogEnum {

    /***
     * 性能日志
     */
    RPC_PROFILE("rpc_profile_log_name", "rpc-profile.log", "rpc_profile_rolling"),

    // 其他日志
    BIZ_SUCCESS("biz_success_log_name", "biz_success.log", "biz_success_rolling"), // 业务成功日志
    BIZ_FAIL("biz_fail_log_name", "biz_fail.log", "biz_fail_rolling"), // 业务异常日志

    /***
     * 中间件错误日志
     */
    MIDDLEWARE_ERROR("middleware_error_log_name", "middleware_error.log",
                     "middleware_error_rolling");

    /***
     * 获取保留天数 getLogReverseDay 关键字
     */
    private String logReverseKey;

    /***
     * 默认生成的日志名字 .log 结尾同时作为一个类型
     */
    private String defaultLogName;

    /***
     * 日志的滚动策略
     */
    private String rollingKey;

    TracerSystemLogEnum(String logReverseKey, String defaultLogName, String rollingKey) {
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
