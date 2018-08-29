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
package com.alipay.sofa.tracer.plugins.datasource.utils;

import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.datasource.tracer.Endpoint;

import java.lang.reflect.Method;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @since 2.2.0
 */
public class DataSourceUtils {

    public static final String DS_DRUID_CLASS      = "com.alibaba.druid.pool.DruidDataSource";

    public static final String DS_DBCP_CLASS       = "org.apache.commons.dbcp.BasicDataSource";

    public static final String DS_C3P0_CLASS       = "com.mchange.v2.c3p0.ComboPooledDataSource";

    public static final String DS_TOMCAT_CLASS     = "org.apache.tomcat.jdbc.pool.DataSource";

    public static final String DS_HIKARI_CLASS     = "com.zaxxer.hikari.HikariDataSource";

    public static final String DS_REVISABLE_CLASS  = "com.alipay.sofa.dbp.discovery.RevisableDataSource";

    public static final String METHOD_GET_URL      = "getUrl";
    public static final String METHOD_SET_URL      = "setUrl";

    public static final String METHOD_GET_JDBC_URL = "getJdbcUrl";
    public static final String METHOD_SET_JDBC_URL = "setJdbcUrl";

    public static boolean isDruidDataSource(Object dataSource) {
        return isTargetDataSource(DS_DRUID_CLASS, dataSource);
    }

    public static boolean isDbcpDataSource(Object dataSource) {
        return isTargetDataSource(DS_DBCP_CLASS, dataSource);
    }

    public static boolean isC3p0DataSource(Object dataSource) {
        return isTargetDataSource(DS_C3P0_CLASS, dataSource);
    }

    public static boolean isTomcatDataSource(Object dataSource) {
        return isTargetDataSource(DS_TOMCAT_CLASS, dataSource);
    }

    public static boolean isHikariDataSource(Object dataSource) {
        return isTargetDataSource(DS_HIKARI_CLASS, dataSource);
    }

    public static boolean isRevisableDataSource(Object dataSource) {
        return isTargetDataSource(DS_REVISABLE_CLASS, dataSource);
    }

    public static String getJdbcUrl(Object dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource is null");
        }
        Method getUrlMethod;
        try {
            if (isDruidDataSource(dataSource) || isDbcpDataSource(dataSource)
                || isTomcatDataSource(dataSource) || isRevisableDataSource(dataSource)) {
                getUrlMethod = dataSource.getClass().getMethod(METHOD_GET_URL);
            } else if (isC3p0DataSource(dataSource) || isHikariDataSource(dataSource)) {
                getUrlMethod = dataSource.getClass().getMethod(METHOD_GET_JDBC_URL);
            } else {
                throw new RuntimeException("cannot resolve dataSource type: " + dataSource);
            }
            return (String) getUrlMethod.invoke(dataSource);
        } catch (Exception e) {
            throw new RuntimeException("invoke method getUrl failed", e);
        }
    }

    public static void setJdbcUrl(Object dataSource, String url) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource is null");
        }
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("url is null");
        }
        Method setUrlMethod;
        try {
            if (isDruidDataSource(dataSource) || isDbcpDataSource(dataSource)
                || isTomcatDataSource(dataSource) || isRevisableDataSource(dataSource)) {
                setUrlMethod = dataSource.getClass().getMethod(METHOD_SET_URL, String.class);
            } else if (isC3p0DataSource(dataSource) || isHikariDataSource(dataSource)) {
                setUrlMethod = dataSource.getClass().getMethod(METHOD_SET_JDBC_URL, String.class);
            } else {
                throw new RuntimeException("cannot resolve dataSource type: " + dataSource);
            }
            setUrlMethod.invoke(dataSource, url);
        } catch (Exception e) {
            throw new RuntimeException("cannot getUrl", e);
        }
    }

    public static boolean isTargetDataSource(String className, Object dataSource) {
        if (dataSource == null) {
            return false;
        }
        Class<?> clazz;
        try {
            clazz = Class.forName(className, true, DataSourceUtils.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            return false;
        }
        return clazz.isAssignableFrom(dataSource.getClass());
    }

    // TODO only support mysql and oracle for now
    public static Endpoint getEndpointFromConnectionURL(final String connectionURL) {
        Endpoint endpoint = new Endpoint();
        String host = null;
        int port = 0;
        try {
            if (connectionURL.contains("jdbc:oracle:thin:@//")) {
                int start = "jdbc:oracle:thin:@//".length()
                            + connectionURL.indexOf("jdbc:oracle:thin:@//");
                int hostEnd = connectionURL.indexOf(':', start);
                int portEnd = connectionURL.indexOf('/', hostEnd + 1);
                host = connectionURL.substring(start, hostEnd);
                if (portEnd > 0) {
                    port = Integer.parseInt(connectionURL.substring(hostEnd + 1, portEnd));
                } else {
                    port = Integer.parseInt(connectionURL.substring(hostEnd + 1));
                }
            } else if (connectionURL.contains("jdbc:oracle:thin:@")) {
                int start = "jdbc:oracle:thin:@".length()
                            + connectionURL.indexOf("jdbc:oracle:thin:@");
                int hostEnd = connectionURL.indexOf(':', start);
                int portEnd = connectionURL.indexOf(':', hostEnd + 1);
                host = connectionURL.substring(start, hostEnd);
                if (portEnd > 0) {
                    port = Integer.parseInt(connectionURL.substring(hostEnd + 1, portEnd));
                } else {
                    port = Integer.parseInt(connectionURL.substring(hostEnd + 1));
                }
            } else if (connectionURL.contains("jdbc:h2:")) {
                int start = connectionURL.indexOf("jdbc:h2:");
                host = connectionURL.substring(start);
                port = -1;
            } else if (connectionURL.indexOf("://") > 0) {
                int start = connectionURL.indexOf("://") + 3;
                int hostEnd = connectionURL.indexOf(':', start);
                int portEnd = connectionURL.indexOf('/', hostEnd + 1);
                host = connectionURL.substring(start, hostEnd);
                if (portEnd > 0) {
                    port = Integer.parseInt(connectionURL.substring(hostEnd + 1, portEnd));
                } else {
                    port = Integer.parseInt(connectionURL.substring(hostEnd + 1));
                }
            } else {
                throw new IllegalArgumentException("only support mysql and oracle connectionURL");
            }
        } catch (Throwable t) {
            throw new RuntimeException("connectionURL maybe invalid: " + connectionURL, t);
        }
        endpoint.setHost(host);
        endpoint.setPort(port);
        return endpoint;
    }
}