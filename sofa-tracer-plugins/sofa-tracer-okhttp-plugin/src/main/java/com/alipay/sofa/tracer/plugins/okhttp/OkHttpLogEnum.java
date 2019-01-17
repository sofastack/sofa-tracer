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
package com.alipay.sofa.tracer.plugins.okhttp;

/**
 * @author xianglong.chen
 * @description
 * @time 2019/1/16 15:56
 */
public enum OkHttpLogEnum {

    // OkHttp Digest Log
    OK_HTTP_DIGEST("okhttp_digest_log_name", "okhttp-digest.log", "okhttp_digest_rolling"),

    // OkHttp Stat Log
    OK_HTTP_STAT("okhttp_stat_log_name", "okhttp-stat.log", "okhttp_stat_rolling");

    private String logNameKey;
    private String defaultLogName;
    private String rollingKey;

    OkHttpLogEnum(String logNameKey, String defaultLogName, String rollingKey) {
        this.logNameKey = logNameKey;
        this.defaultLogName = defaultLogName;
        this.rollingKey = rollingKey;
    }

    public String getLogNameKey() {
        return logNameKey;
    }

    public String getDefaultLogName() {
        return defaultLogName;
    }

    public String getRollingKey() {
        return rollingKey;
    }}