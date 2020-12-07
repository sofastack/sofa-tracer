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
package com.sofa.alipay.tracer.plugins.spring.redis.tracer;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.ComponentNameConstants;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractClientTracer;
import com.sofa.alipay.tracer.plugins.spring.redis.encoder.RedisDigestEncoder;
import com.sofa.alipay.tracer.plugins.spring.redis.encoder.RedisDigestJsonEncoder;
import com.sofa.alipay.tracer.plugins.spring.redis.enums.RedisLogEnum;
import com.sofa.alipay.tracer.plugins.spring.redis.reporter.RedisStatJsonReporter;
import com.sofa.alipay.tracer.plugins.spring.redis.reporter.RedisStatReporter;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/11/18 9:03 PM
 * @since:
 **/
public class RedisSofaTracer extends AbstractClientTracer {

    private volatile static RedisSofaTracer redisSofaTracer = null;

    public static RedisSofaTracer getRedisSofaTracerSingleton() {
        if (redisSofaTracer == null) {
            synchronized (RedisSofaTracer.class) {
                if (redisSofaTracer == null) {
                    redisSofaTracer = new RedisSofaTracer(ComponentNameConstants.REDIS);
                }
            }
        }
        return redisSofaTracer;
    }

    public RedisSofaTracer(String tracerType) {
        super(tracerType);
    }

    @Override
    protected String getClientDigestReporterLogName() {
        return RedisLogEnum.REDIS_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getClientDigestReporterRollingKey() {
        return RedisLogEnum.REDIS_DIGEST.getRollingKey();
    }

    @Override
    protected String getClientDigestReporterLogNameKey() {
        return RedisLogEnum.REDIS_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getClientDigestEncoder() {
        //default json output
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new RedisDigestJsonEncoder();
        } else {
            return new RedisDigestEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateClientStatReporter() {
        RedisLogEnum redisLogEnum = RedisLogEnum.REDIS_STAT;
        String statLog = redisLogEnum.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration.getRollingPolicy(redisLogEnum
            .getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(redisLogEnum
            .getLogNameKey());
        //stat
        return this.getRedisClientStatReporter(statLog, statRollingPolicy, statLogReserveConfig);
    }

    protected AbstractSofaTracerStatisticReporter getRedisClientStatReporter(String statTracerName,
                                                                             String statRollingPolicy,
                                                                             String statLogReserveConfig) {

        if (SofaTracerConfiguration.isJsonOutput()) {
            return new RedisStatJsonReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        } else {
            return new RedisStatReporter(statTracerName, statRollingPolicy, statLogReserveConfig);
        }

    }
}
