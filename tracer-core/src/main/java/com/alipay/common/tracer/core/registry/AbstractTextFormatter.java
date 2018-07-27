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
package com.alipay.common.tracer.core.registry;

import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.utils.StringUtils;
import io.opentracing.propagation.TextMap;

import java.util.Map;

/**
 * AbstractTextFormatter
 *
 * @author yangguanchao
 * @since 2017/06/24
 */
public abstract class AbstractTextFormatter implements RegistryExtractorInjector<TextMap> {

    @Override
    public SofaTracerSpanContext extract(TextMap carrier) {
        if (carrier == null) {
            //根节点开始
            return SofaTracerSpanContext.rootStart();
        }
        SofaTracerSpanContext sofaTracerSpanContext = null;
        for (Map.Entry<String, String> entry : carrier) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (StringUtils.isBlank(key)) {
                continue;
            }
            if (FORMATER_KEY_HEAD.equalsIgnoreCase(key) && !StringUtils.isBlank(value)) {
                sofaTracerSpanContext = SofaTracerSpanContext.deserializeFromString(this
                    .decodedValue(value));
            }
        }
        if (sofaTracerSpanContext == null) {
            //根节点开始
            return SofaTracerSpanContext.rootStart();
        }
        return sofaTracerSpanContext;
    }

    @Override
    public void inject(SofaTracerSpanContext spanContext, TextMap carrier) {
        if (carrier == null || spanContext == null) {
            return;
        }
        carrier.put(FORMATER_KEY_HEAD, this.encodedValue(spanContext.serializeSpanContext()));
    }

    /***
     * 对指定的值进行编码
     * @param value 字符串
     * @return 编码后的 value
     */
    protected abstract String encodedValue(String value);

    /***
     * 对指定的值进行解码
     * @param value 字符串
     * @return 编码后的字符串
     */
    protected abstract String decodedValue(String value);
}
