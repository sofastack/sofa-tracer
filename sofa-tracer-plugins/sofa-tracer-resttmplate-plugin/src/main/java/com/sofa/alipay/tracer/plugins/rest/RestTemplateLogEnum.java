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
package com.sofa.alipay.tracer.plugins.rest;

/**
 * RestTemplateLogEnum
 * @author: guolei.sgl
 * @since: 18/10/15
 */
public enum RestTemplateLogEnum {

    // RestTemplate Client Digest Log
    REST_TEMPLATE_DIGEST("resttemplate_digest_log_name", "resttemplate-digest.log",
                         "resttemplate_digest_rolling"),
    // RestTemplate Stat Log
    REST_TEMPLATE_STAT("resttemplate_stat_log_name", "resttemplate-stat.log",
                       "resttemplate_stat_rolling");

    private String logNameKey;
    private String defaultLogName;
    private String rollingKey;

    RestTemplateLogEnum(String logNameKey, String defaultLogName, String rollingKey) {
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
