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

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @since 2.2.0
 */
public class ExtendedStatement implements Statement {

    protected final ExtendedConnection conn;

    protected final Statement          delegate;

    protected List<Interceptor>        interceptors;

    protected Prop                     prop;

    protected List<String>             batchSqlList;

    ExtendedStatement(ExtendedConnection conn, Statement delegate, Prop prop) {
        this.conn = conn;
        this.delegate = delegate;
        this.prop = prop;
        this.interceptors = prop.getInterceptors();
    }

    public Statement getDelegate() {
        return delegate;
    }

    abstract class AbstractStatementInterceptorChain implements Interceptor.Chain {

        private final int        index;

        private final Invocation invocation;

        private final String     sql;

        private String           processingSql;

        AbstractStatementInterceptorChain(int index, String sql, Invocation invocation) {
            this(index, sql, sql, invocation);
        }

        AbstractStatementInterceptorChain(int index, String sql, String processingSql,
                                          Invocation invocation) {
            this.index = index;
            this.sql = sql;
            this.processingSql = processingSql;
            this.invocation = invocation;
        }

        @Override
        public Object proceed() throws Exception {
            if (interceptors != null && index < interceptors.size()) {
                Interceptor.Chain chain = newStatementInterceptorChain(index + 1, sql,
                    processingSql, invocation);
                Interceptor interceptor = interceptors.get(index);
                return interceptor.intercept(chain);
            }
            beforeInvoke(invocation);
            return invocation.invoke();
        }

        abstract AbstractStatementInterceptorChain newStatementInterceptorChain(int index,
                                                                                String sql,
                                                                                String processingSql,
                                                                                Invocation invocation);

        abstract void beforeInvoke(Invocation invocation) throws Exception;

        @Override
        public String getOriginalSql() {
            return sql;
        }

        @Override
        public String getProcessingSql() {
            return processingSql;
        }

        @Override
        public void setProcessingSql(String processingSql) {
            this.processingSql = processingSql;
        }

        @Override
        public BaseDataSource getDataSource() {
            return conn.getDataSource();
        }

        @Override
        public ExtendedConnection getConnection() {
            return conn;
        }

        @Override
        public ExtendedStatement getStatement() {
            return ExtendedStatement.this;
        }
    }

    class AbstractStatementInterceptorChainImpl extends AbstractStatementInterceptorChain {

        AbstractStatementInterceptorChainImpl(String sql, Invocation invocation) {
            super(0, sql, invocation);
        }

        AbstractStatementInterceptorChainImpl(int index, String sql, String processingSql,
                                              Invocation invocation) {
            super(index, sql, processingSql, invocation);
        }

        @Override
        AbstractStatementInterceptorChain newStatementInterceptorChain(int index, String sql,
                                                                       String processingSql,
                                                                       Invocation invocation) {
            return new AbstractStatementInterceptorChainImpl(index, sql, processingSql, invocation);
        }

        protected void beforeInvoke(Invocation invocation) throws SQLException {
            invocation.getArgs()[0] = getProcessingSql();
        }
    }

    Method getMethod(String methodName) {
        Method targetMethod = prop.getTargetMethod(methodName);
        if (targetMethod == null) {
            throw new IllegalStateException("method: " + methodName + "not registered");
        }
        return targetMethod;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        Interceptor.Chain chain = new AbstractStatementInterceptorChainImpl(sql, new Invocation(
            getMethod(MethodRegistry.METHOD_EXECUTE_QUERY), delegate, sql));
        try {
            return (ResultSet) chain.proceed();
        } catch (Exception e) {
            if (e instanceof SQLException) {
                throw (SQLException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return doExecuteUpdate(getMethod(MethodRegistry.METHOD_EXECUTE_UPDATE0), sql);
    }

    private int doExecuteUpdate(Method method, Object... args) throws SQLException {
        String sql = (String) args[0];
        Interceptor.Chain chain = new AbstractStatementInterceptorChainImpl(sql, new Invocation(
            method, delegate, args));
        try {
            return (Integer) chain.proceed();
        } catch (Exception e) {
            if (e instanceof SQLException) {
                throw (SQLException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() throws SQLException {
        delegate.close();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return delegate.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        delegate.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return delegate.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        delegate.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        delegate.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return delegate.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        delegate.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        delegate.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        delegate.setCursorName(name);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return doExecute(getMethod(MethodRegistry.METHOD_EXECUTE0), sql);
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return delegate.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return delegate.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return delegate.getMoreResults();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        delegate.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return delegate.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        delegate.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return delegate.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return delegate.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return delegate.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        if (batchSqlList == null) {
            batchSqlList = new ArrayList<String>();
        }
        batchSqlList.add(sql);
        delegate.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        if (batchSqlList != null) {
            batchSqlList.clear();
        }
        delegate.clearBatch();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return delegate.executeBatch();
    }

    public List<String> getBatchSqlList() {
        return batchSqlList;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return conn;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return delegate.getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return delegate.getGeneratedKeys();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return doExecuteUpdate(getMethod(MethodRegistry.METHOD_EXECUTE_UPDATE1), sql,
            autoGeneratedKeys);
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return doExecuteUpdate(getMethod(MethodRegistry.METHOD_EXECUTE_UPDATE2), sql, columnIndexes);
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return doExecuteUpdate(getMethod(MethodRegistry.METHOD_EXECUTE_UPDATE3), sql, columnNames);
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return doExecute(getMethod(MethodRegistry.METHOD_EXECUTE1), sql, autoGeneratedKeys);
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return doExecute(getMethod(MethodRegistry.METHOD_EXECUTE2), sql, columnIndexes);
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return doExecute(getMethod(MethodRegistry.METHOD_EXECUTE3), sql, columnNames);
    }

    private boolean doExecute(Method method, Object... args) throws SQLException {
        String sql = (String) args[0];
        Interceptor.Chain chain = new AbstractStatementInterceptorChainImpl(sql, new Invocation(
            method, delegate, args));
        try {
            return (Boolean) chain.proceed();
        } catch (Exception e) {
            if (e instanceof SQLException) {
                throw (SQLException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return delegate.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        delegate.setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return delegate.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        delegate.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return delegate.isCloseOnCompletion();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }
}
