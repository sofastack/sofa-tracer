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
package com.sofa.alipay.tracer.plugins.rabbitmq.enums;

/**
 * RabbitMqLogEnum.
 *
 * @author chenchen6  2020/7/19 20:44
 * @since 3.1.0
 */
public enum RabbitMqLogEnum {

    MQ_SEND_DIGEST("rabbitmq_send_digest_log_name", "rabbitmq-send-digest.log",
                   "rabbitmq_send_digest_rolling"), MQ_SEND_STAT("rabbitmq_send_stat_log_name",
                                                                 "rabbitmq-send-stat.log",
                                                                 "rabbitmq_send_stat_rolling"),

    MQ_CONSUME_DIGEST("rabbitmq_consume_digest_log_name", "rabbitmq-consume-digest.log",
                      "rabbitmq_consume_digest_rolling"), MQ_CONSUME_STAT(
                                                                          "rabbitmq_consume_stat_log_name",
                                                                          "rabbitmq-consume-stat.log",
                                                                          "rabbitmq_consume_stat_rolling");

    private String logNameKey;
    private String defaultLogName;
    private String rollingKey;

    RabbitMqLogEnum(String logNameKey, String defaultLogName, String rollingKey) {
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
