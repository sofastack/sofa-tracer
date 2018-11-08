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

import java.util.HashMap;
import java.util.Map;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @since 2.2.0
 */
public class DataSourceTracerState {
    private static final int          NEW                  = 0;
    private static final int          PROCESSING_FIRST_SQL = 1;
    private int                       state;

    private final Map<String, Object> traceState           = new HashMap<String, Object>(8);

    DataSourceTracerState() {
        state = NEW;
        submit(DataSourceTracerKeys.START_TIME, System.currentTimeMillis());
    }

    public void submit(String key, Object value) {
        traceState.put(key, value);
    }

    public Object getValue(String key) {
        return traceState.get(key);
    }

    public int getState() {
        return state;
    }

    public void connectionEstablished() {
        Long startTime = (Long) traceState.get(DataSourceTracerKeys.START_TIME);
        submit(DataSourceTracerKeys.CONNECTION_ESTABLISH_COST, System.currentTimeMillis()
                                                               - startTime);
    }

    public void propagate() {
        state = state + 1;
    }

    public boolean isProcessingFirstSql() {
        return state == PROCESSING_FIRST_SQL;
    }
}
