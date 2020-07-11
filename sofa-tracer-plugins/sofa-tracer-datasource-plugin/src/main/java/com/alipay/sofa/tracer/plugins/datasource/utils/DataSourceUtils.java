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

import com.alipay.common.tracer.core.utils.AssertUtils;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.datasource.tracer.Endpoint;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @author chenchen
 * @since 2.2.0
 */
public class DataSourceUtils {

    public static final String DS_DRUID_CLASS      = "com.alibaba.druid.pool.DruidDataSource";

    public static final String DS_DBCP_CLASS       = "org.apache.commons.dbcp.BasicDataSource";

    public static final String DS_C3P0_CLASS       = "com.mchange.v2.c3p0.ComboPooledDataSource";

    public static final String DS_TOMCAT_CLASS     = "org.apache.tomcat.jdbc.pool.DataSource";

    public static final String DS_HIKARI_CLASS     = "com.zaxxer.hikari.HikariDataSource";

    public static final String METHOD_GET_URL      = "getUrl";
    public static final String METHOD_SET_URL      = "setUrl";

    public static final String METHOD_GET_JDBC_URL = "getJdbcUrl";
    public static final String METHOD_SET_JDBC_URL = "setJdbcUrl";

    public static final String ORACLE_PREFIX_THIN  = "jdbc:oracle:thin:";
    public static final int    ORACLE_DEFAULT_PORT = 1521;

    public static boolean isDruidDataSource(Object dataSource) {
        return isTargetDataSource(DS_DRUID_CLASS, dataSource);
    }

    public static boolean isDruidDataSource(String clazzType) {
        return !StringUtils.isBlank(clazzType) && DS_DRUID_CLASS.equals(clazzType);
    }

    public static boolean isDbcpDataSource(Object dataSource) {
        return isTargetDataSource(DS_DBCP_CLASS, dataSource);
    }

    public static boolean isDbcpDataSource(String clazzType) {
        return !StringUtils.isBlank(clazzType) && DS_DBCP_CLASS.equals(clazzType);
    }

    public static boolean isC3p0DataSource(Object dataSource) {
        return isTargetDataSource(DS_C3P0_CLASS, dataSource);
    }

    public static boolean isC3p0DataSource(String clazzType) {
        return !StringUtils.isBlank(clazzType) && DS_C3P0_CLASS.equals(clazzType);
    }

    public static boolean isTomcatDataSource(Object dataSource) {
        return isTargetDataSource(DS_TOMCAT_CLASS, dataSource);
    }

    public static boolean isTomcatDataSource(String clazzType) {
        return !StringUtils.isBlank(clazzType) && DS_TOMCAT_CLASS.equals(clazzType);
    }

    public static boolean isHikariDataSource(Object dataSource) {
        return isTargetDataSource(DS_HIKARI_CLASS, dataSource);
    }

    public static boolean isHikariDataSource(String clazzType) {
        return !StringUtils.isBlank(clazzType) && DS_HIKARI_CLASS.equals(clazzType);
    }

    public static String getJdbcUrl(Object dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource is null");
        }
        Method getUrlMethod;
        try {
            if (isDruidDataSource(dataSource) || isDbcpDataSource(dataSource)
                || isTomcatDataSource(dataSource)) {
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
                || isTomcatDataSource(dataSource)) {
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

    public static String getTomcatJdbcUrlKey() {
        return "url";
    }

    public static String getDbcpJdbcUrlKey() {
        return "url";
    }

    public static String getDruidJdbcUrlKey() {
        return "url";
    }

    public static String getC3p0JdbcUrlKey() {
        return "jdbcUrl";
    }

    public static String getHikariJdbcUrlKey() {
        return "jdbcUrl";
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

    public static List<Endpoint> getEndpointsFromConnectionURL(final String connectionURL) {
        String currentUri = StringUtils.EMPTY_STRING;
        List<Endpoint> endpoints = null;
        if (StringUtils.isNotBlank(connectionURL) && connectionURL.startsWith(ORACLE_PREFIX_THIN)) {
            currentUri = connectionURL.substring(ORACLE_PREFIX_THIN.length());
            // parse endpoints by tns name.
            endpoints = parseEndpointByTnsName(currentUri);
            if (endpoints == null || endpoints.size() == 0 || null == endpoints.get(0)
                || StringUtils.isBlank(endpoints.get(0).getHost())) {
                // easy tns or others db url be resolve. it's a single endpoint.
                Endpoint singleEndpoint = getEndpointFromConnectionURL(connectionURL);
                if (StringUtils.isBlank(singleEndpoint.getHost()) || 0 == singleEndpoint.getPort()) {
                    throw new IllegalArgumentException("check your connectionURL: " + connectionURL);
                }
                endpoints = Collections.singletonList(singleEndpoint);
            }
            return endpoints;
        }
        return Collections.emptyList();
    }

    private static List<Endpoint> parseEndpointByTnsName(final String url) {
        int beginIndex = url.indexOf("DESCRIPTION");
        if (beginIndex == -1) {
            return null;
        }
        //multiple virtual IPs point to the same database.
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        Endpoint currEndpoint = null;
        do {
            int hostStartIndex = url.indexOf("HOST", beginIndex);
            if (hostStartIndex == -1) {
                break;
            }
            int equalStartIndex = url.indexOf("=", hostStartIndex);
            int hostEndIndex = url.indexOf(")", hostStartIndex);
            String host = url.substring(equalStartIndex + 1, hostEndIndex);

            int port = ORACLE_DEFAULT_PORT;
            int portStartIndex = url.indexOf("PORT", hostEndIndex);
            int portEndIndex = url.length();
            if (portStartIndex != -1) {
                int portEqualStartIndex = url.indexOf("=", portStartIndex);
                portEndIndex = url.indexOf(")", portEqualStartIndex);
                port = Integer
                    .parseInt(url.substring(portEqualStartIndex + 1, portEndIndex).trim());
            }
            currEndpoint = new Endpoint();
            currEndpoint.setHost(host.trim());
            currEndpoint.setPort(port);
            endpoints.add(currEndpoint);
            beginIndex = portEndIndex;
        } while (true);
        return endpoints;
    }

    // TODO only support mysql, oracle, h2 and sqlServer for now
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
                host = connectionURL;
                port = -1;
            } else if (connectionURL.indexOf("://") > 0) {
                int start = connectionURL.indexOf("://") + 3;
                int hostEnd = connectionURL.indexOf(':', start);
                int portEnd = connectionURL.indexOf('/', hostEnd + 1);
                if (portEnd == -1) {
                    // SQL Server
                    portEnd = connectionURL.indexOf(';', hostEnd + 1);
                }
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

    public static String resolveDbTypeFromUrl(String url) {
        AssertUtils.isTrue(!StringUtils.isBlank(url), "Jdbc url must not be empty!");
        int start = url.indexOf("jdbc:") + "jdbc:".length();
        if (start < "jdbc:".length()) {
            throw new InvalidParameterException("jdbc url is invalid!");
        }
        int end = url.indexOf(":", start);
        if (end < 0) {
            throw new InvalidParameterException("jdbc url is invalid!");
        }
        String dbType = url.substring(start, end);
        // SQL Server 2000
        if ("microsoft".equals(dbType)) {
            start = end + 1;
            end = url.indexOf(":", start);
            if (end < 0) {
                throw new InvalidParameterException("jdbc url is invalid!");
            }
            return url.substring(start, end);
        } else {
            return dbType;
        }
    }

    public static String resolveDatabaseFromUrl(String url) {
        AssertUtils.isTrue(!StringUtils.isBlank(url), "Jdbc url must not be empty!");
        if ("sqlserver".equals(resolveDbTypeFromUrl(url))) {
            String[] segments = url.split(";");
            for (String segment : segments) {
                if (segment.toLowerCase().contains("databasename=")) {
                    int start = segment.toLowerCase().indexOf("databasename=")
                                + "databasename=".length();
                    return segment.substring(start).trim();
                }
            }
            throw new InvalidParameterException("jdbc url is invalid!");
        }

        int start = url.lastIndexOf("/");
        if (start < 0) {
            /**
             * oracle sid format，{@see jdbc:oracle:thin:@host:port:SID}
             */
            if ("oracle".equals(resolveDbTypeFromUrl(url))) {
                start = url.lastIndexOf(":");
            } else {
                throw new InvalidParameterException("jdbc url is invalid!");
            }
        }
        int end = url.indexOf("?", start);
        if (end != -1) {
            return url.substring(start + 1, end);
        }
        // tns service name.
        String oracleServiceName = url.substring(start + 1);
        if (StringUtils.isNotBlank(oracleServiceName) && oracleServiceName.contains("DESCRIPTION")) {
            final int idxServiceName = url.indexOf("SERVICE_NAME");
            final int startService = url.indexOf('=', idxServiceName) + 1;
            final int endService = url.indexOf(")", startService);
            final String serviceName = url.substring(startService, endService);
            if (StringUtils.isBlank(serviceName)) {
                throw new IllegalArgumentException("Check your tns service name!");
            }
            return serviceName.trim();
        }
        //others
        return url.substring(start + 1);
    }
}