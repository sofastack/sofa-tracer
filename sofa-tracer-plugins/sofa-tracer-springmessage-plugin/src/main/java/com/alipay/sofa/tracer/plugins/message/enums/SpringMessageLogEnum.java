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
package com.alipay.sofa.tracer.plugins.message.enums;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 2:46 PM
 * @since:
 **/
public enum SpringMessageLogEnum {

    MESSAGE_PUB_DIGEST("message_pub_digest_log_name", "message-pub-digest.log",
                       "message_pub_digest_rolling"), MESSAGE_PUB_STAT("message_pub_stat_log_name",
                                                                       "message-pub-stat.log",
                                                                       "message_pub_stat_rolling"),

    MESSAGE_SUB_DIGEST("message_sub_digest_log_name", "message-sub-digest.log",
                       "message_sub_digest_rolling"), MESSAGE_SUB_STAT("message_sub_stat_log_name",
                                                                       "message-sub-stat.log",
                                                                       "message_sub_stat_rolling");

    private String logNameKey;
    private String defaultLogName;
    private String rollingKey;

    SpringMessageLogEnum(String logNameKey, String defaultLogName, String rollingKey) {
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
