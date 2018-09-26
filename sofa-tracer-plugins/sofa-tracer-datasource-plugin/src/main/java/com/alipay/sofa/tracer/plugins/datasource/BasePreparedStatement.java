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

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @since 2.2.0
 */
public abstract class BasePreparedStatement implements PreparedStatement {

    private final ExtendedConnection      connection;
    private final Prop                    prop;

    private AtomicBoolean                 initialized;
    private boolean                       closed;

    private PreparedStatement             realPreparedStatement;

    private final List<PreparedParameter> preparedParameters = new ArrayList<PreparedParameter>();

    public BasePreparedStatement(ExtendedConnection connection) {
        this.connection = connection;
        this.prop = connection.getProp();
    }

    public void initPreparedStatement(String sql) throws Exception {
        if (closed) {
            throw new IllegalStateException("BasePreparedStatement has been closed");
        }
        if (initialized.compareAndSet(false, true)) {
            realPreparedStatement = doPrepareStatement(sql);
            for (PreparedParameter preparedParameter : preparedParameters) {
                preparedParameter.invoke(realPreparedStatement);
            }
        }
    }

    public class PreparedParameter {
        private final Method   method;
        private final Object[] params;

        private PreparedParameter(Method method, Object... params) {
            this.method = method;
            this.params = params;
        }

        public Method getMethod() {
            return method;
        }

        public Object[] getParams() {
            return params;
        }

        Object invoke(Object target) throws SQLException {
            try {
                return method.invoke(target, params);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<PreparedParameter> getPreparedParameters() {
        return preparedParameters;
    }

    protected abstract PreparedStatement doPrepareStatement(String sql) throws SQLException;

    private void checkState() throws SQLException {
        if (!initialized.get()) {
            throw new IllegalStateException("BasePreparedStatement has not been initialized");
        }
        if (closed) {
            throw new IllegalStateException("BasePreparedStatement has been closed");
        }
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        checkState();
        return realPreparedStatement.executeQuery();
    }

    @Override
    public int executeUpdate() throws SQLException {
        checkState();
        return realPreparedStatement.executeUpdate();
    }

    private void setParameter(String methodName, Object... args) {
        Method method = prop.getTargetMethod(methodName);
        if (method == null) {
            throw new IllegalStateException("method: " + methodName + " not registered");
        }
        preparedParameters.add(new PreparedParameter(prop.getTargetMethod(methodName), args));
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_NULL_II, parameterIndex, sqlType);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_BOOLEAN, parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_BYTE, parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_SHORT, parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_INT, parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_LONG, parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_FLOAT, parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_DOUBLE, parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_BIGDECIMAL, parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_STRING, parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_BYTES, parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_DATE_ID, parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_TIME_IT, parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_TIMESTAMP_IT, parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_ASCIISTREAM_III, parameterIndex, x, length);
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_UNICODESTREAM, parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_BINARYSTREAM_III, parameterIndex, x, length);
    }

    @Override
    public void clearParameters() throws SQLException {
        checkState();
        realPreparedStatement.clearParameters();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_OBJECT_IOI, parameterIndex, x, targetSqlType);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_OBJECT_IO, parameterIndex, x);
    }

    @Override
    public boolean execute() throws SQLException {
        checkState();
        return realPreparedStatement.execute();
    }

    @Override
    public void addBatch() throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_ADD_BATCH);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length)
                                                                                 throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_CHARACTERSTREAM_IRI, parameterIndex, reader,
            length);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_REF, parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_BLOB_IB, parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_CLOB_IC, parameterIndex, x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_ARRAY, parameterIndex, x);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        checkState();
        return realPreparedStatement.getMetaData();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_DATE_IDC, parameterIndex, x, cal);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_TIME_ITC, parameterIndex, x, cal);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_TIME_ITC, parameterIndex, x, cal);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_NULL_IIS, parameterIndex, sqlType, typeName);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_URL, parameterIndex, x);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        checkState();
        return realPreparedStatement.getParameterMetaData();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_ROWID, parameterIndex, x);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_NSTRING, parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length)
                                                                                  throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_NCHARACTERSTREAM_IRL, parameterIndex, value,
            length);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_NCLOB_IN, parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_CLOB_IRL, parameterIndex, reader, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length)
                                                                                 throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_BLOB_IIL, parameterIndex, inputStream, length);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_NCLOB_IRL, parameterIndex, reader, length);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_SQLXML, parameterIndex, xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength)
                                                                                             throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_OBJECT_IOII, parameterIndex, x, targetSqlType,
            scaleOrLength);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_ASCIISTREAM_IIL, parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_BINARYSTREAM_IIL, parameterIndex, x, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length)
                                                                                  throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_CHARACTERSTREAM_IRL, parameterIndex, reader,
            length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_ASCIISTREAM_II, parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_BINARYSTRAM_II, parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_CHARACTERSTREAM_IR, parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_NCHARACTERSTREAM_IR, parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_CLOB_IR, parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_BLOB_II, parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        setParameter(MethodRegistry.METHOD_PS_SET_NCLOB_IR, parameterIndex, reader);
    }

    // statement methods
    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        checkState();
        return realPreparedStatement.executeQuery(sql);
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        checkState();
        return realPreparedStatement.executeUpdate(sql);
    }

    @Override
    public void close() throws SQLException {
        if (realPreparedStatement != null) {
            realPreparedStatement.close();
        }
        closed = true;
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        checkState();
        return realPreparedStatement.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        setParameter(MethodRegistry.METHOD_SET_MAX_FIELD_SIZE, max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        checkState();
        return realPreparedStatement.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        setParameter(MethodRegistry.METHOD_SET_MAX_ROWS, max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        setParameter(MethodRegistry.METHOD_SET_ESCAPE_PROCESSING, enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        checkState();
        return realPreparedStatement.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        setParameter(MethodRegistry.METHOD_SET_QUERY_TIMEOUT, seconds);
    }

    @Override
    public void cancel() throws SQLException {
        checkState();
        realPreparedStatement.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        checkState();
        return realPreparedStatement.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        checkState();
        realPreparedStatement.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        setParameter(MethodRegistry.METHOD_SET_CURSOR_NAME, name);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        checkState();
        return realPreparedStatement.execute(sql);
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        checkState();
        return realPreparedStatement.getResultSet();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        checkState();
        return realPreparedStatement.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        checkState();
        return realPreparedStatement.getMoreResults();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        setParameter(MethodRegistry.METHOD_SET_FETCH_DIRECTION, direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        checkState();
        return realPreparedStatement.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        setParameter(MethodRegistry.METHOD_SET_FETCH_SIZE, rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        checkState();
        return realPreparedStatement.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        checkState();
        return realPreparedStatement.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        checkState();
        return realPreparedStatement.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        checkState();
        realPreparedStatement.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        checkState();
        realPreparedStatement.clearBatch();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        checkState();
        return realPreparedStatement.executeBatch();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        checkState();
        return realPreparedStatement.getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        checkState();
        return realPreparedStatement.getGeneratedKeys();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        checkState();
        return realPreparedStatement.executeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        checkState();
        return realPreparedStatement.executeUpdate(sql, columnIndexes);
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        checkState();
        return realPreparedStatement.executeUpdate(sql, columnNames);
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        checkState();
        return realPreparedStatement.execute(sql, autoGeneratedKeys);
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        checkState();
        return realPreparedStatement.execute(sql, columnIndexes);
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        checkState();
        return realPreparedStatement.execute(sql, columnNames);
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        checkState();
        return realPreparedStatement.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        checkState();
        return realPreparedStatement.isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        setParameter(MethodRegistry.METHOD_SET_POOLABLE, poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        checkState();
        return realPreparedStatement.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        checkState();
        realPreparedStatement.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        checkState();
        return realPreparedStatement.isCloseOnCompletion();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        checkState();
        return realPreparedStatement.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        checkState();
        return realPreparedStatement.isWrapperFor(iface);
    }
}
