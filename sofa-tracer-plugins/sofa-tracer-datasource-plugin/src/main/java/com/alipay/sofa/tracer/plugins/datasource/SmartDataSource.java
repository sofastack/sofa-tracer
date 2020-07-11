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
package com.alipay.sofa.tracer.plugins.datasource;

import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.datasource.tracer.*;
import com.alipay.sofa.tracer.plugins.datasource.utils.DataSourceUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @author chenchen
 * @since 2.2.0
 */
public class SmartDataSource extends BaseDataSource {

    private String                           appName;

    private String                           database;

    private String                           dbType;

    private boolean                          isEnableTrace = Boolean.TRUE;

    private DataSourceClientTracer           clientTracer  = DataSourceClientTracer
                                                               .getDataSourceClientTracer();

    /**
     * DataSource basic info. Including appName, dbType, dbName, dbEndpoint.
     */
    protected final List<KeyValueAnnotation> traceAnnotations;

    public SmartDataSource() {
        this(null);
    }

    public SmartDataSource(DataSource delegate) {
        super(delegate);
        traceAnnotations = new ArrayList<>();
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public boolean isEnableTrace() {
        return isEnableTrace;
    }

    public void setEnableTrace(boolean enableTrace) {
        isEnableTrace = enableTrace;
    }

    public DataSourceClientTracer getClientTracer() {
        return clientTracer;
    }

    public void setClientTracer(DataSourceClientTracer clientTracer) {
        this.clientTracer = clientTracer;
    }

    public List<KeyValueAnnotation> getTraceAnnotations() {
        return traceAnnotations;
    }

    /**
     * init method must be invoked first after construction
     */
    @Override
    public void init() {
        if (initialized.compareAndSet(false, true)) {
            if (StringUtils.isBlank(dbType)) {
                throw new IllegalArgumentException("dbType must not be null or empty");
            }
            String upperDbType = dbType.toUpperCase();
            if (!DBType.supportedDbTypes.containsKey(upperDbType)) {
                throw new IllegalArgumentException("dbType: " + upperDbType
                                                   + "not in support list: "
                                                   + DBType.supportedDbTypes);
            }
            traceAnnotations
                .add(new KeyValueAnnotation(DataSourceTracerKeys.DATABASE_TYPE, dbType));
            if (StringUtils.isBlank(database)) {
                throw new IllegalArgumentException("database must not be null or empty");
            }
            traceAnnotations.add(new KeyValueAnnotation(DataSourceTracerKeys.DATABASE_NAME,
                database));
            if (StringUtils.isBlank(appName)) {
                throw new IllegalArgumentException("appName must not be null or empty");
            }
            traceAnnotations.add(new KeyValueAnnotation(DataSourceTracerKeys.LOCAL_APP, appName));

            DataSource dataSource = getDelegate();
            String jdbcUrl = DataSourceUtils.getJdbcUrl(dataSource);
            //multiple virtual IPs point to the same database.
            traceAnnotations.add(new KeyValueAnnotation(DataSourceTracerKeys.DATABASE_ENDPOINT,
                getEndpointsStr(DataSourceUtils.getEndpointsFromConnectionURL(jdbcUrl))));

            Prop prop = getProp();
            List<Interceptor> interceptors = getInterceptors();
            if (interceptors != null && !interceptors.isEmpty()) {
                prop.addAll(interceptors);
            }
            if (isEnableTrace && clientTracer != null) {
                setupStatementTracerInterceptor();
                setupConnectionTracerInterceptor();
            }
        }
    }

    protected void setupStatementTracerInterceptor() {
        StatementTracerInterceptor statementTracerInterceptor = new StatementTracerInterceptor();
        statementTracerInterceptor.setClientTracer(clientTracer);
        getProp().addInterceptor(statementTracerInterceptor);
    }

    protected void setupConnectionTracerInterceptor() {
        List<Interceptor> dataSourceInterceptors = getDataSourceInterceptors();
        ConnectionTraceInterceptor connectionTraceInterceptor = new ConnectionTraceInterceptor(
            traceAnnotations);
        if (dataSourceInterceptors != null) {
            dataSourceInterceptors.add(connectionTraceInterceptor);
        } else {
            setDataSourceInterceptors(Collections
                .<Interceptor> singletonList(connectionTraceInterceptor));
        }
    }

    private String getEndpointsStr(List<Endpoint> endpoints) {
        if (null == endpoints) {
            return StringUtils.EMPTY_STRING;
        }

        int endpointSize = endpoints.size();
        if (0 == endpointSize) {
            return StringUtils.EMPTY_STRING;
        }

        if (1 == endpointSize) {
            return endpoints.get(0).getEndpoint();
        }

        StringBuilder sb = new StringBuilder();
        for (Endpoint endpoint : endpoints) {
            sb.append(endpoint.getEndpoint()).append("/");
        }
        return sb.substring(0, sb.lastIndexOf("/"));
    }
}
