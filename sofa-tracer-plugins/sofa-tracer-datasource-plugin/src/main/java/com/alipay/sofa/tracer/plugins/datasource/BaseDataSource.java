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

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * @author qilong.zql
 * @author shusong.yss
 * @since 2.2.0
 */
public abstract class BaseDataSource implements DataSource {

    private DataSource        delegate;

    private SecuritySpec      securitySpec;

    private List<Interceptor> interceptors;

    private List<Interceptor> dataSourceInterceptors;

    private final Prop        prop;

    AtomicBoolean             initialized = new AtomicBoolean();

    public BaseDataSource(DataSource delegate) {
        this.delegate = delegate;
        this.prop = new Prop();
    }

    public DataSource getDelegate() {
        return delegate;
    }

    public void setDelegate(DataSource delegate) {
        this.delegate = delegate;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public List<Interceptor> getDataSourceInterceptors() {
        return dataSourceInterceptors;
    }

    public void setDataSourceInterceptors(List<Interceptor> dataSourceInterceptors) {
        this.dataSourceInterceptors = dataSourceInterceptors;
    }

    public SecuritySpec getSecuritySpec() {
        return securitySpec;
    }

    public void setSecuritySpec(SecuritySpec securitySpec) {
        this.securitySpec = securitySpec;
    }

    public Prop getProp() {
        return prop;
    }

    /**
     * init method must be invoked first
     */
    public abstract void init();

    private void checkInit() {
        if (!initialized.get()) {
            throw new IllegalStateException("DataSource has not been initialized");
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        checkInit();
        DataSourceInterceptorChain dataSourceInterceptorChain = new DataSourceInterceptorChain(
            new Invocation(prop.getTargetMethod(MethodRegistry.METHOD_DS_GET_CONNECTION), delegate));
        try {
            Connection conn = (Connection) dataSourceInterceptorChain.proceed();
            if (conn != null) {
                return new ExtendedConnection(this, conn, prop);
            }
        } catch (Exception e) {
            throw new SQLException(e);
        }
        throw new SQLException("getConnection failed");
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new UnsupportedOperationException(
            "please setup user and password in delegate dataSource");
    }

    private class DataSourceInterceptorChain implements Interceptor.Chain {

        private final int        index;

        private final Invocation invocation;

        DataSourceInterceptorChain(Invocation invocation) {
            this(0, invocation);
        }

        DataSourceInterceptorChain(int index, Invocation invocation) {
            this.index = index;
            this.invocation = invocation;
        }

        @Override
        public Object proceed() throws Exception {
            if (dataSourceInterceptors != null && index < dataSourceInterceptors.size()) {
                Interceptor.Chain chain = new DataSourceInterceptorChain(index + 1, invocation);
                return dataSourceInterceptors.get(index).intercept(chain);
            }
            return invocation.invoke();
        }

        @Override
        public String getOriginalSql() {
            throw new UnsupportedOperationException("this operation not unsupported");
        }

        @Override
        public String getProcessingSql() {
            throw new UnsupportedOperationException("this operation unsupported");

        }

        @Override
        public void setProcessingSql(String processingSql) {
            throw new UnsupportedOperationException("this operation unsupported");
        }

        @Override
        public BaseDataSource getDataSource() {
            return BaseDataSource.this;
        }

        @Override
        public ExtendedConnection getConnection() {
            throw new UnsupportedOperationException("this operation unsupported");
        }

        @Override
        public ExtendedStatement getStatement() {
            throw new UnsupportedOperationException("this operation unsupported");
        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }
}