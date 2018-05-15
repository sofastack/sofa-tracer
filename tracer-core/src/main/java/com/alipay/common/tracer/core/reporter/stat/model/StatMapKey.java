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
package com.alipay.common.tracer.core.reporter.stat.model;

import java.util.HashMap;
import java.util.Map;

/**
 * StatMapKey
 *
 * @author yangguanchao
 * @since 2018/05/15
 */
public class StatMapKey extends StatKey {

    /**
     * 统计信息的 Key
     */
    private Map<String, String> keyMap = new HashMap<String, String>();

    public Map<String, String> getKeyMap() {
        return keyMap;
    }

    public void addKey(String key, String value) {
        if (key != null && value != null) {
            keyMap.put(key, value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StatMapKey)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        StatMapKey that = (StatMapKey) o;

        return getKeyMap().equals(that.getKeyMap());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getKeyMap().hashCode();
        return result;
    }
}
