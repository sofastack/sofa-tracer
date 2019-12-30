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
package com.alipay.sofa.tracer.plugins.springcloud.carriers;

import feign.Request;
import io.opentracing.propagation.TextMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 5:09 PM
 * @since:
 **/
public class FeignRequestCarrier implements TextMap {

    private Request request;

    public FeignRequestCarrier(Request request) {
        if (request == null) {
            throw new NullPointerException("Headers request should not be null!");
        }
        this.request = request;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(String key, String val) {
        if (request != null) {
            Collection<String> vals = request.headers().get(key);
            vals.clear();
            vals.add(val);
            request.headers().put(key, vals);
        }
    }
}
