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

import io.opentracing.propagation.TextMap;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author qilong.zql
 * @since 2.2.0
 */
public class DataSourceClientRequestCarrier implements TextMap {

    private final Map<String, String> tagsWithStr = new LinkedHashMap<String, String>();

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return tagsWithStr.entrySet().iterator();
    }

    @Override
    public void put(String key, String value) {
        tagsWithStr.put(key, value);
    }
}