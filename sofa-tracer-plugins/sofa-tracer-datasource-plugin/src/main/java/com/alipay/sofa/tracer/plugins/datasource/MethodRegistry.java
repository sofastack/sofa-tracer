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

import com.alipay.common.tracer.core.appender.self.SelfLog;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @since 2.2.0
 */
public class MethodRegistry {

    private final Map<String, Method> methodRegistry                     = new HashMap<String, Method>();

    public static final String        METHOD_DS_GET_CONNECTION           = "getConnection";
    public static final String        METHOD_DS_GET_CONNECTION_SS        = "getConnection_ss";

    public static final String        METHOD_EXECUTE0                    = "execute0";
    public static final String        METHOD_EXECUTE1                    = "execute1";
    public static final String        METHOD_EXECUTE2                    = "execute2";
    public static final String        METHOD_EXECUTE3                    = "execute3";
    public static final String        METHOD_EXECUTE_QUERY               = "executeQuery";
    public static final String        METHOD_EXECUTE_UPDATE0             = "executeUpdate0";
    public static final String        METHOD_EXECUTE_UPDATE1             = "executeUpdate1";
    public static final String        METHOD_EXECUTE_UPDATE2             = "executeUpdate2";
    public static final String        METHOD_EXECUTE_UPDATE3             = "executeUpdate3";
    public static final String        METHOD_EXECUTE_BATCH               = "executeBatch";
    public static final String        METHOD_ADD_BATCH                   = "addBatch";

    public static final String        METHOD_PS_EXECUTE_QUERY            = "ps_executeQuery";
    public static final String        METHOD_PS_EXECUTE_UPDATE           = "ps_executeUpdate";
    public static final String        METHOD_PS_EXECUTE                  = "ps_execute";
    public static final String        METHOD_PS_ADD_BATCH                = "ps_addBatch";

    public static final String        METHOD_PS_SET_STRING               = "ps_setString";
    public static final String        METHOD_PS_SET_NULL_II              = "ps_setNull_ii";
    public static final String        METHOD_PS_SET_BOOLEAN              = "ps_setBoolean";
    public static final String        METHOD_PS_SET_BYTE                 = "ps_setByte";
    public static final String        METHOD_PS_SET_SHORT                = "ps_setShort";
    public static final String        METHOD_PS_SET_INT                  = "ps_setInt";
    public static final String        METHOD_PS_SET_LONG                 = "ps_setLong";
    public static final String        METHOD_PS_SET_FLOAT                = "ps_setFloat";
    public static final String        METHOD_PS_SET_DOUBLE               = "ps_setDouble";
    public static final String        METHOD_PS_SET_BIGDECIMAL           = "ps_setBigDecimal";
    public static final String        METHOD_PS_SET_BYTES                = "ps_setBytes";
    public static final String        METHOD_PS_SET_DATE_ID              = "ps_setDate_id";
    public static final String        METHOD_PS_SET_TIME_IT              = "ps_setTime_it";
    public static final String        METHOD_PS_SET_TIMESTAMP_IT         = "ps_setTimestamp_it";
    public static final String        METHOD_PS_SET_ASCIISTREAM_III      = "ps_setAsciiStream_iii";
    public static final String        METHOD_PS_SET_UNICODESTREAM        = "ps_setUnicodeStream";
    public static final String        METHOD_PS_SET_BINARYSTREAM_III     = "ps_setBinaryStream_iii";
    public static final String        METHOD_PS_CLEAR_PARAMETERS         = "ps_clearParameters";
    public static final String        METHOD_PS_SET_OBJECT_IOI           = "ps_setObject_ioi";
    public static final String        METHOD_PS_SET_OBJECT_IO            = "ps_setObject_io";
    public static final String        METHOD_PS_SET_CHARACTERSTREAM_IRI  = "ps_setCharacterStream_iri";
    public static final String        METHOD_PS_SET_REF                  = "ps_setRef";
    public static final String        METHOD_PS_SET_BLOB_IB              = "ps_setBlob_ib";
    public static final String        METHOD_PS_SET_CLOB_IC              = "ps_setClob_ic";
    public static final String        METHOD_PS_SET_ARRAY                = "ps_setArray";
    public static final String        METHOD_PS_SET_DATE_IDC             = "ps_setDate_idc";
    public static final String        METHOD_PS_SET_TIME_ITC             = "ps_setTime_itc";
    public static final String        METHOD_PS_SET_TIMESTAMP_ITC        = "ps_setTimestamp_itc";
    public static final String        METHOD_PS_SET_NULL_IIS             = "ps_setNull_iis";
    public static final String        METHOD_PS_SET_URL                  = "ps_setURL";
    public static final String        METHOD_PS_SET_ROWID                = "ps_setRowId";
    public static final String        METHOD_PS_SET_NSTRING              = "ps_setNString";
    public static final String        METHOD_PS_SET_NCHARACTERSTREAM_IRL = "ps_setNCharacterStream_irl";
    public static final String        METHOD_PS_SET_NCLOB_IN             = "ps_setNClob_IN";
    public static final String        METHOD_PS_SET_CLOB_IRL             = "ps_setClob_irl";
    public static final String        METHOD_PS_SET_BLOB_IIL             = "ps_setBlob_irl";
    public static final String        METHOD_PS_SET_NCLOB_IRL            = "ps_nclob_irl";
    public static final String        METHOD_PS_SET_SQLXML               = "ps_setSQLXML";
    public static final String        METHOD_PS_SET_OBJECT_IOII          = "ps_setObject_ioii";
    public static final String        METHOD_PS_SET_ASCIISTREAM_IIL      = "ps_setAsciiStream_iil";
    public static final String        METHOD_PS_SET_BINARYSTREAM_IIL     = "ps_setBinaryStream_iil";
    public static final String        METHOD_PS_SET_CHARACTERSTREAM_IRL  = "ps_setCharacterStream_irl";
    public static final String        METHOD_PS_SET_ASCIISTREAM_II       = "ps_setAsciiStream_ii";
    public static final String        METHOD_PS_SET_BINARYSTRAM_II       = "ps_setBinaryStream_ii";
    public static final String        METHOD_PS_SET_CHARACTERSTREAM_IR   = "ps_setCharacterStream_ir";
    public static final String        METHOD_PS_SET_NCHARACTERSTREAM_IR  = "ps_setNCharacterStream_ir";
    public static final String        METHOD_PS_SET_CLOB_IR              = "ps_setClob_ir";
    public static final String        METHOD_PS_SET_BLOB_II              = "ps_setBlob_ii";
    public static final String        METHOD_PS_SET_NCLOB_IR             = "ps_setNClob_ir";

    public static final String        METHOD_SET_FETCH_DIRECTION         = "setFetchDirection";
    public static final String        METHOD_SET_POOLABLE                = "setPoolable";
    public static final String        METHOD_SET_MAX_FIELD_SIZE          = "setMaxFieldSize";
    public static final String        METHOD_SET_MAX_ROWS                = "setMaxRows";
    public static final String        METHOD_SET_ESCAPE_PROCESSING       = "setEscapeProcessing";
    public static final String        METHOD_SET_QUERY_TIMEOUT           = "setQueryTimeout";
    public static final String        METHOD_SET_CURSOR_NAME             = "setCursorName";
    public static final String        METHOD_SET_FETCH_SIZE              = "setFetchSize";

    public MethodRegistry() {
        init();
    }

    private void init() {
        try {
            methodRegistry.put(METHOD_DS_GET_CONNECTION,
                DataSource.class.getDeclaredMethod("getConnection"));
            methodRegistry.put(METHOD_DS_GET_CONNECTION_SS,
                DataSource.class.getDeclaredMethod("getConnection", String.class, String.class));
            methodRegistry.put(METHOD_EXECUTE0,
                Statement.class.getDeclaredMethod("execute", String.class));
            methodRegistry.put(METHOD_EXECUTE1,
                Statement.class.getDeclaredMethod("execute", String.class, int.class));
            methodRegistry.put(METHOD_EXECUTE2,
                Statement.class.getDeclaredMethod("execute", String.class, int[].class));
            methodRegistry.put(METHOD_EXECUTE3,
                Statement.class.getDeclaredMethod("execute", String.class, String[].class));
            methodRegistry.put(METHOD_EXECUTE_QUERY,
                Statement.class.getDeclaredMethod("executeQuery", String.class));
            methodRegistry.put(METHOD_EXECUTE_UPDATE0,
                Statement.class.getDeclaredMethod("executeUpdate", String.class));
            methodRegistry.put(METHOD_EXECUTE_UPDATE1,
                Statement.class.getDeclaredMethod("executeUpdate", String.class, int.class));
            methodRegistry.put(METHOD_EXECUTE_UPDATE2,
                Statement.class.getDeclaredMethod("executeUpdate", String.class, int[].class));
            methodRegistry.put(METHOD_EXECUTE_UPDATE3,
                Statement.class.getDeclaredMethod("executeUpdate", String.class, String[].class));
            methodRegistry.put(METHOD_EXECUTE_BATCH,
                Statement.class.getDeclaredMethod("executeBatch"));
            methodRegistry.put(METHOD_ADD_BATCH,
                Statement.class.getDeclaredMethod("addBatch", String.class));

            methodRegistry.put(METHOD_PS_EXECUTE_QUERY,
                PreparedStatement.class.getDeclaredMethod("executeQuery"));
            methodRegistry.put(METHOD_PS_EXECUTE_UPDATE,
                PreparedStatement.class.getDeclaredMethod("executeUpdate"));
            methodRegistry.put(METHOD_PS_EXECUTE,
                PreparedStatement.class.getDeclaredMethod("execute"));
            methodRegistry.put(METHOD_PS_ADD_BATCH,
                PreparedStatement.class.getDeclaredMethod("addBatch"));

            methodRegistry.put(METHOD_PS_SET_NULL_II,
                PreparedStatement.class.getDeclaredMethod("setNull", int.class, int.class));
            methodRegistry.put(METHOD_PS_SET_NULL_IIS, PreparedStatement.class.getDeclaredMethod(
                "setNull", int.class, int.class, String.class));
            methodRegistry.put(METHOD_PS_SET_BOOLEAN,
                PreparedStatement.class.getDeclaredMethod("setBoolean", int.class, boolean.class));
            methodRegistry.put(METHOD_PS_SET_BYTE,
                PreparedStatement.class.getDeclaredMethod("setByte", int.class, byte.class));
            methodRegistry.put(METHOD_PS_SET_SHORT,
                PreparedStatement.class.getDeclaredMethod("setShort", int.class, short.class));
            methodRegistry.put(METHOD_PS_SET_INT,
                PreparedStatement.class.getDeclaredMethod("setInt", int.class, int.class));
            methodRegistry.put(METHOD_PS_SET_LONG,
                PreparedStatement.class.getDeclaredMethod("setLong", int.class, long.class));
            methodRegistry.put(METHOD_PS_SET_STRING,
                PreparedStatement.class.getDeclaredMethod("setString", int.class, String.class));
            methodRegistry.put(METHOD_PS_SET_FLOAT,
                PreparedStatement.class.getDeclaredMethod("setFloat", int.class, float.class));
            methodRegistry.put(METHOD_PS_SET_DOUBLE,
                PreparedStatement.class.getDeclaredMethod("setDouble", int.class, double.class));
            methodRegistry.put(METHOD_PS_SET_BIGDECIMAL, PreparedStatement.class.getDeclaredMethod(
                "setBigDecimal", int.class, BigDecimal.class));
            methodRegistry.put(METHOD_PS_SET_BYTES,
                PreparedStatement.class.getDeclaredMethod("setBytes", int.class, byte[].class));
            methodRegistry.put(METHOD_PS_SET_DATE_ID,
                PreparedStatement.class.getDeclaredMethod("setDate", int.class, Date.class));
            methodRegistry.put(METHOD_PS_SET_DATE_IDC, PreparedStatement.class.getDeclaredMethod(
                "setDate", int.class, Date.class, Calendar.class));
            methodRegistry.put(METHOD_PS_SET_TIME_IT,
                PreparedStatement.class.getDeclaredMethod("setTime", int.class, Time.class));
            methodRegistry.put(METHOD_PS_SET_TIME_ITC, PreparedStatement.class.getDeclaredMethod(
                "setTime", int.class, Time.class, Calendar.class));
            methodRegistry.put(METHOD_PS_SET_TIMESTAMP_IT, PreparedStatement.class
                .getDeclaredMethod("setTimestamp", int.class, Timestamp.class));
            methodRegistry.put(METHOD_PS_SET_TIMESTAMP_ITC, PreparedStatement.class
                .getDeclaredMethod("setTimestamp", int.class, Timestamp.class, Calendar.class));
            methodRegistry.put(METHOD_PS_SET_ASCIISTREAM_III, PreparedStatement.class
                .getDeclaredMethod("setAsciiStream", int.class, InputStream.class, int.class));
            methodRegistry.put(METHOD_PS_SET_ASCIISTREAM_II, PreparedStatement.class
                .getDeclaredMethod("setAsciiStream", int.class, InputStream.class));
            methodRegistry.put(METHOD_PS_SET_ASCIISTREAM_IIL, PreparedStatement.class
                .getDeclaredMethod("setAsciiStream", int.class, InputStream.class, long.class));
            methodRegistry.put(METHOD_PS_SET_UNICODESTREAM, PreparedStatement.class
                .getDeclaredMethod("setUnicodeStream", int.class, InputStream.class, int.class));
            methodRegistry.put(METHOD_PS_SET_BINARYSTRAM_II, PreparedStatement.class
                .getDeclaredMethod("setBinaryStream", int.class, InputStream.class));
            methodRegistry.put(METHOD_PS_SET_BINARYSTREAM_III, PreparedStatement.class
                .getDeclaredMethod("setBinaryStream", int.class, InputStream.class, int.class));
            methodRegistry.put(METHOD_PS_SET_BINARYSTREAM_IIL, PreparedStatement.class
                .getDeclaredMethod("setBinaryStream", int.class, InputStream.class, long.class));
            methodRegistry.put(METHOD_PS_CLEAR_PARAMETERS,
                PreparedStatement.class.getDeclaredMethod("clearParameters"));
            methodRegistry.put(METHOD_PS_SET_OBJECT_IO,
                PreparedStatement.class.getDeclaredMethod("setObject", int.class, Object.class));
            methodRegistry.put(METHOD_PS_SET_OBJECT_IOI, PreparedStatement.class.getDeclaredMethod(
                "setObject", int.class, Object.class, int.class));
            methodRegistry.put(METHOD_PS_SET_OBJECT_IOII, PreparedStatement.class
                .getDeclaredMethod("setObject", int.class, Object.class, int.class, int.class));
            methodRegistry.put(METHOD_PS_SET_CHARACTERSTREAM_IR, PreparedStatement.class
                .getDeclaredMethod("setCharacterStream", int.class, Reader.class));
            methodRegistry.put(METHOD_PS_SET_CHARACTERSTREAM_IRI, PreparedStatement.class
                .getDeclaredMethod("setCharacterStream", int.class, Reader.class, int.class));
            methodRegistry.put(METHOD_PS_SET_CHARACTERSTREAM_IRL, PreparedStatement.class
                .getDeclaredMethod("setCharacterStream", int.class, Reader.class, long.class));
            methodRegistry.put(METHOD_PS_SET_REF,
                PreparedStatement.class.getDeclaredMethod("setRef", int.class, Ref.class));
            methodRegistry.put(METHOD_PS_SET_BLOB_IB,
                PreparedStatement.class.getDeclaredMethod("setBlob", int.class, Blob.class));
            methodRegistry.put(METHOD_PS_SET_BLOB_II,
                PreparedStatement.class.getDeclaredMethod("setBlob", int.class, InputStream.class));
            methodRegistry.put(METHOD_PS_SET_BLOB_IIL, PreparedStatement.class.getDeclaredMethod(
                "setBlob", int.class, InputStream.class, long.class));
            methodRegistry.put(METHOD_PS_SET_CLOB_IC,
                PreparedStatement.class.getDeclaredMethod("setClob", int.class, Clob.class));
            methodRegistry.put(METHOD_PS_SET_CLOB_IR,
                PreparedStatement.class.getDeclaredMethod("setClob", int.class, Reader.class));
            methodRegistry.put(METHOD_PS_SET_CLOB_IRL, PreparedStatement.class.getDeclaredMethod(
                "setClob", int.class, Reader.class, long.class));
            methodRegistry.put(METHOD_PS_SET_ARRAY,
                PreparedStatement.class.getDeclaredMethod("setArray", int.class, Array.class));
            methodRegistry.put(METHOD_PS_SET_URL,
                PreparedStatement.class.getDeclaredMethod("setURL", int.class, URL.class));
            methodRegistry.put(METHOD_PS_SET_ROWID,
                PreparedStatement.class.getDeclaredMethod("setRowId", int.class, RowId.class));
            methodRegistry.put(METHOD_PS_SET_NSTRING,
                PreparedStatement.class.getDeclaredMethod("setNString", int.class, String.class));
            methodRegistry.put(METHOD_PS_SET_NCHARACTERSTREAM_IR, PreparedStatement.class
                .getDeclaredMethod("setNCharacterStream", int.class, Reader.class));
            methodRegistry.put(METHOD_PS_SET_NCHARACTERSTREAM_IRL, PreparedStatement.class
                .getDeclaredMethod("setNCharacterStream", int.class, Reader.class, long.class));
            methodRegistry.put(METHOD_PS_SET_NCLOB_IN,
                PreparedStatement.class.getDeclaredMethod("setNClob", int.class, NClob.class));
            methodRegistry.put(METHOD_PS_SET_NCLOB_IR,
                PreparedStatement.class.getDeclaredMethod("setNClob", int.class, Reader.class));
            methodRegistry.put(METHOD_PS_SET_NCLOB_IRL, PreparedStatement.class.getDeclaredMethod(
                "setClob", int.class, Reader.class, long.class));
            methodRegistry.put(METHOD_PS_SET_SQLXML,
                PreparedStatement.class.getDeclaredMethod("setSQLXML", int.class, SQLXML.class));

            methodRegistry.put(METHOD_SET_FETCH_DIRECTION,
                Statement.class.getDeclaredMethod("setFetchDirection", int.class));
            methodRegistry.put(METHOD_SET_POOLABLE,
                Statement.class.getDeclaredMethod("setPoolable", boolean.class));
            methodRegistry.put(METHOD_SET_MAX_FIELD_SIZE,
                Statement.class.getDeclaredMethod("setMaxFieldSize", int.class));
            methodRegistry.put(METHOD_SET_MAX_ROWS,
                Statement.class.getDeclaredMethod("setMaxRows", int.class));
            methodRegistry.put(METHOD_SET_ESCAPE_PROCESSING,
                Statement.class.getDeclaredMethod("setEscapeProcessing", boolean.class));
            methodRegistry.put(METHOD_SET_QUERY_TIMEOUT,
                Statement.class.getDeclaredMethod("setQueryTimeout", int.class));
            methodRegistry.put(METHOD_SET_CURSOR_NAME,
                Statement.class.getDeclaredMethod("setCursorName", String.class));
            methodRegistry.put(METHOD_SET_FETCH_SIZE,
                Statement.class.getDeclaredMethod("setFetchSize", int.class));
        } catch (Exception e) {
            SelfLog.error("init methodRegistry failed.", e);
            throw new RuntimeException(e);
        }
    }

    public Method getMethod(String methodName) {
        return methodRegistry.get(methodName);
    }
}