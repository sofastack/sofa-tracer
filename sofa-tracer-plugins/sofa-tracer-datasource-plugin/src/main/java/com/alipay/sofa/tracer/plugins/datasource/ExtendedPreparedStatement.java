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
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @since 2.2.0
 */
public class ExtendedPreparedStatement extends ExtendedStatement implements PreparedStatement {

    private final String originalSql;

    ExtendedPreparedStatement(ExtendedConnection connection, PreparedStatement ps, String sql,
                              Prop prop) {
        super(connection, ps, prop);
        this.originalSql = sql;
    }

    private String getOriginalSql() {
        return originalSql;
    }

    class PreparedBaseStatementInterceptorChainImpl extends BaseStatementInterceptorChain {

        PreparedBaseStatementInterceptorChainImpl(String sql, Invocation invocation) {
            super(sql, sql, invocation);
        }

        @Override
        void beforeInvoke(Invocation invocation) throws Exception {
            if (delegate instanceof BasePreparedStatement) {
                ((BasePreparedStatement) delegate).initPreparedStatement(getProcessingSql());
            }
        }
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        Interceptor.Chain chain = new PreparedBaseStatementInterceptorChainImpl(getOriginalSql(),
            new Invocation(getMethod(MethodRegistry.METHOD_PS_EXECUTE_QUERY), delegate));
        try {
            return (ResultSet) chain.proceed();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        Interceptor.Chain chain = new PreparedBaseStatementInterceptorChainImpl(getOriginalSql(),
            new Invocation(getMethod(MethodRegistry.METHOD_PS_EXECUTE_UPDATE), delegate));
        try {
            return (Integer) chain.proceed();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public int[] executeBatch() throws SQLException {
        Interceptor.Chain chain = new PreparedBaseStatementInterceptorChainImpl(getOriginalSql(),
            new Invocation(getMethod(MethodRegistry.METHOD_EXECUTE_BATCH), delegate));
        try {
            return (int[]) chain.proceed();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public boolean execute() throws SQLException {
        Interceptor.Chain chain = new PreparedBaseStatementInterceptorChainImpl(getOriginalSql(),
            new Invocation(getMethod(MethodRegistry.METHOD_PS_EXECUTE), delegate));
        try {
            return (Boolean) chain.proceed();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        getDelegate().setNull(parameterIndex, sqlType);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        getDelegate().setBoolean(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        getDelegate().setByte(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        getDelegate().setShort(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        getDelegate().setInt(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        getDelegate().setLong(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        getDelegate().setFloat(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        getDelegate().setDouble(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        getDelegate().setBigDecimal(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        getDelegate().setString(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        getDelegate().setBytes(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        getDelegate().setDate(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        getDelegate().setTime(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        getDelegate().setTimestamp(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        getDelegate().setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        getDelegate().setUnicodeStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        getDelegate().setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void clearParameters() throws SQLException {
        getDelegate().clearParameters();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        getDelegate().setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        getDelegate().setObject(parameterIndex, x);
    }

    @Override
    public void addBatch() throws SQLException {
        getDelegate().addBatch();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length)
                                                                                 throws SQLException {
        getDelegate().setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        getDelegate().setRef(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        getDelegate().setBlob(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        getDelegate().setClob(parameterIndex, x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        getDelegate().setArray(parameterIndex, x);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return getDelegate().getMetaData();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        getDelegate().setDate(parameterIndex, x, cal);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        getDelegate().setTime(parameterIndex, x, cal);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        getDelegate().setTimestamp(parameterIndex, x, cal);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        getDelegate().setNull(parameterIndex, sqlType, typeName);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        getDelegate().setURL(parameterIndex, x);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return getDelegate().getParameterMetaData();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        getDelegate().setRowId(parameterIndex, x);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        getDelegate().setNString(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length)
                                                                                  throws SQLException {
        getDelegate().setNCharacterStream(parameterIndex, value, length);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        getDelegate().setNClob(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        getDelegate().setClob(parameterIndex, reader, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length)
                                                                                 throws SQLException {
        getDelegate().setBlob(parameterIndex, inputStream, length);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        getDelegate().setNClob(parameterIndex, reader, length);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        getDelegate().setSQLXML(parameterIndex, xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength)
                                                                                             throws SQLException {
        getDelegate().setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        getDelegate().setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        getDelegate().setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length)
                                                                                  throws SQLException {
        getDelegate().setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        getDelegate().setAsciiStream(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        getDelegate().setBinaryStream(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        getDelegate().setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        getDelegate().setNCharacterStream(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        getDelegate().setClob(parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        getDelegate().setBlob(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        getDelegate().setNClob(parameterIndex, reader);
    }

    @Override
    public PreparedStatement getDelegate() {
        return (PreparedStatement) delegate;
    }
}
