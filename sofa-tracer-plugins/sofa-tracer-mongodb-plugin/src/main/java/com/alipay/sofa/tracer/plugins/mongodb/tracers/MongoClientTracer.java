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
package com.alipay.sofa.tracer.plugins.mongodb.tracers;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractClientTracer;
import com.alipay.sofa.tracer.plugins.mongodb.encodes.MongoDigestEncoder;
import com.alipay.sofa.tracer.plugins.mongodb.encodes.MongoDigestJsonEncoder;
import com.alipay.sofa.tracer.plugins.mongodb.enums.MongoClientLogEnum;
import com.alipay.sofa.tracer.plugins.mongodb.repoters.MongoStatJsonReporter;
import com.alipay.sofa.tracer.plugins.mongodb.repoters.MongoStatReporter;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 10:52 AM
 * @since:
 **/
public class MongoClientTracer extends AbstractClientTracer {

    private volatile static MongoClientTracer mongoClientTracer = null;

    /***
     * Http Client Tracer Singleton
     * @return singleton
     */
    public static MongoClientTracer getMongoClientTracerSingleton() {
        if (mongoClientTracer == null) {
            synchronized (MongoClientTracer.class) {
                if (mongoClientTracer == null) {
                    mongoClientTracer = new MongoClientTracer();
                }
            }
        }
        return mongoClientTracer;
    }

    protected MongoClientTracer() {
        super("open-feign");
    }

    @Override
    protected String getClientDigestReporterLogName() {
        return MongoClientLogEnum.MONGO_CLIENT_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getClientDigestReporterRollingKey() {
        return MongoClientLogEnum.MONGO_CLIENT_DIGEST.getRollingKey();
    }

    @Override
    protected String getClientDigestReporterLogNameKey() {
        return MongoClientLogEnum.MONGO_CLIENT_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getClientDigestEncoder() {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new MongoDigestJsonEncoder();
        } else {
            return new MongoDigestEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateClientStatReporter() {
        MongoClientLogEnum logEnum = MongoClientLogEnum.MONGO_CLIENT_STAT;
        String statLog = logEnum.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration
            .getRollingPolicy(logEnum.getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(logEnum
            .getLogNameKey());
        return this.getStatJsonReporter(statLog, statRollingPolicy, statLogReserveConfig);
    }

    protected AbstractSofaTracerStatisticReporter getStatJsonReporter(String statTracerName,
                                                                      String statRollingPolicy,
                                                                      String statLogReserveConfig) {
        if (SofaTracerConfiguration.isJsonOutput()) {
            return new MongoStatJsonReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        } else {
            return new MongoStatReporter(statTracerName, statRollingPolicy, statLogReserveConfig);
        }
    }
}
