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
package com.alipay.sofa.tracer.plugins.datasource.tracer;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractClientTracer;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.datasource.utils.SqlUtils;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @since 2.2.0
 */
public class DataSourceClientTracer extends AbstractClientTracer {

    private static final ThreadLocal<DataSourceTracerState> traceState             = new ThreadLocal<DataSourceTracerState>() {
                                                                                       @Override
                                                                                       protected DataSourceTracerState initialValue() {
                                                                                           return null;
                                                                                       }
                                                                                   };

    private volatile static DataSourceClientTracer          dataSourceClientTracer = null;

    public static final String                              RESULT_CODE_SUCCESS    = "success";

    public static final String                              RESULT_CODE_FAILED     = "failed";

    /***
     * DataSource Client Tracer Singleton
     * @return singleton
     */
    public static DataSourceClientTracer getDataSourceClientTracer() {
        if (dataSourceClientTracer == null) {
            synchronized (DataSourceClientTracer.class) {
                if (dataSourceClientTracer == null) {
                    dataSourceClientTracer = new DataSourceClientTracer();
                }
            }
        }
        return dataSourceClientTracer;
    }

    private DataSourceClientTracer() {
        super("dataSource");
    }

    @Override
    protected String getClientDigestReporterLogName() {
        return DataSourceLogEnum.DATA_SOURCE_CLIENT_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getClientDigestReporterRollingKey() {
        return DataSourceLogEnum.DATA_SOURCE_CLIENT_DIGEST.getRollingKey();
    }

    @Override
    protected String getClientDigestReporterLogNameKey() {
        return DataSourceLogEnum.DATA_SOURCE_CLIENT_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getClientDigestEncoder() {
        return new DataSourceClientDigestJsonEncoder();
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateClientStatReporter() {
        DataSourceLogEnum dataSourceLogEnum = DataSourceLogEnum.DATA_SOURCE_CLIENT_STAT;
        String statLogName = dataSourceLogEnum.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration.getRollingPolicy(dataSourceLogEnum
            .getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(dataSourceLogEnum
            .getLogNameKey());
        return new DataSourceClientStatJsonReporter(statLogName, statRollingPolicy,
            statLogReserveConfig);
    }

    public void startTrace(String sql) {
        SofaTracerSpan sofaTracerSpan = clientSend((String) getStateValue(DataSourceTracerKeys.DATABASE_NAME));
        SofaTracerSpanContext cxt = sofaTracerSpan.getSofaTracerSpanContext();
        if (cxt != null) {
            propagate();
            if (isProcessingFirstSql()) {
                sofaTracerSpan.setStartTime((Long) getStateValue(DataSourceTracerKeys.START_TIME));
                sofaTracerSpan.setTag(DataSourceTracerKeys.CONNECTION_ESTABLISH_COST,
                    (Long) getStateValue(DataSourceTracerKeys.CONNECTION_ESTABLISH_COST));
            } else {
                sofaTracerSpan.setStartTime((Long) getStateValue(DataSourceTracerKeys.START_TIME));
                sofaTracerSpan.setTag(DataSourceTracerKeys.CONNECTION_ESTABLISH_COST, 0L);
            }
            sofaTracerSpan.setTag(DataSourceTracerKeys.LOCAL_APP,
                (String) getStateValue(DataSourceTracerKeys.LOCAL_APP));
            sofaTracerSpan.setTag(DataSourceTracerKeys.DATABASE_TYPE,
                (String) getStateValue(DataSourceTracerKeys.DATABASE_TYPE));
            sofaTracerSpan.setTag(DataSourceTracerKeys.DATABASE_NAME,
                (String) getStateValue(DataSourceTracerKeys.DATABASE_NAME));
            sofaTracerSpan.setTag(DataSourceTracerKeys.DATABASE_ENDPOINT,
                (String) getStateValue(DataSourceTracerKeys.DATABASE_ENDPOINT));
            sofaTracerSpan.setTag(DataSourceTracerKeys.SQL, SqlUtils.getSqlEscaped(sql));
        }
    }

    public void endTrace(long cost, String resultCode) {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        if (sofaTraceContext != null) {
            SofaTracerSpan sofaTracerSpan = sofaTraceContext.getCurrentSpan();
            if (sofaTracerSpan != null) {
                sofaTracerSpan.setTag(DataSourceTracerKeys.DB_EXECUTE_COST, cost);
                try {
                    sofaTracerSpan.setTag(CommonSpanTags.RESULT_CODE, resultCode);
                    sofaTracerSpan.setEndTime(System.currentTimeMillis());
                    clientReceive(resultCode);
                } catch (Throwable throwable) {
                    SelfLog.errorWithTraceId("db processed", throwable);
                }
            }
        }

    }

    protected void propagate() {
        DataSourceTracerState state = traceState.get();
        if (state != null) {
            state.propagate();
        }
    }

    protected boolean isProcessingFirstSql() {
        DataSourceTracerState state = traceState.get();
        return state == null || state.isProcessingFirstSql();
    }

    protected Object getStateValue(String key) {
        DataSourceTracerState state = traceState.get();
        if (state != null) {
            return state.getValue(key);
        }
        return null;
    }

    public static DataSourceTracerState newState() {
        DataSourceTracerState state = new DataSourceTracerState();
        traceState.set(state);
        return state;
    }
}