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
package com.sofa.alipay.tracer.plugins.kafkamq.enums;

/**
 * KafkaMqLogEnum.
 *
 * @author chenchen6  2020/7/19 20:44
 * @since 3.1.0-SNAPSHOT
 */
public enum KafkaMqLogEnum {

    MQ_SEND_DIGEST("kafkamq_send_digest_log_name", "kafkamq-send-digest.log",
                   "kafkamq_send_digest_rolling"), MQ_SEND_STAT("kafkamq_send_stat_log_name",
                                                                "kafkamq-send-stat.log",
                                                                "kafkamq_send_stat_rolling"),

    MQ_CONSUME_DIGEST("kafkamq_consume_digest_log_name", "kafkamq-consume-digest.log",
                      "kafkamq_consume_digest_rolling"), MQ_CONSUME_STAT(
                                                                         "kafkamq_consume_stat_log_name",
                                                                         "kafkamq-consume-stat.log",
                                                                         "kafkamq_consume_stat_rolling");

    private String logNameKey;
    private String defaultLogName;
    private String rollingKey;

    KafkaMqLogEnum(String logNameKey, String defaultLogName, String rollingKey) {
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
    }
}
