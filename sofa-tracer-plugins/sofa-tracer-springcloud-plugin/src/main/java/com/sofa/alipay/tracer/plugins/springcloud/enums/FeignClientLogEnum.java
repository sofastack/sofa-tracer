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
package com.sofa.alipay.tracer.plugins.springcloud.enums;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 2:46 PM
 * @since:
 **/
public enum FeignClientLogEnum {
    // Feign Client Digest Log
    FEIGN_CLIENT_DIGEST("feign_digest_log_name", "feign-digest.log", "feign_digest_rolling"),
    // Feign Client Stat Log
    FEIGN_CLIENT_STAT("feign_stat_log_name", "feign-stat.log", "feign_stat_rolling");

    private String logNameKey;
    private String defaultLogName;
    private String rollingKey;

    FeignClientLogEnum(String logNameKey, String defaultLogName, String rollingKey) {
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
