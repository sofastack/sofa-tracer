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
package com.alipay.sofa.tracer.spring.zipkin.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/***
 * PropertiesHolder is a tool to deal zipkin properties
 * @author guolei.sgl
 */
public class PropertiesHolder extends ZipkinProperties {

    private static Properties    props;

    protected static Compression compression = new Compression();

    static {
        // init zipkin properties
        loadProps();
    }

    private static synchronized void loadProps() {
        props = new Properties();
        InputStream in = null;
        try {
            in = PropertiesHolder.class.getClassLoader().getResourceAsStream("zipkin.properties");
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load zipkin.properties", e);
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to close InputStream object.", e);
            }
        }
    }

    public static String getProperty(String key) {
        if (null == props) {
            loadProps();
        }
        return props.getProperty(key);
    }

    public static String getBaseUrl() {
        return getProperty(ZIPKIN_BASE_URL_KEY);
    }

    public static boolean getEnabled() {
        String enabledStr = getProperty(ZIPKIN_IS_ENABLED_KEY);
        return !(enabledStr != null && "false".equals(enabledStr));
    }

    public static boolean getCompressEnabled() {
        return compression.isEnabled();
    }
}
