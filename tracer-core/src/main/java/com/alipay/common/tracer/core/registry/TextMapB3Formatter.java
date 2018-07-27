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

import com.alipay.common.tracer.core.utils.StringUtils;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

/**
 * TextMapFormatter
 *
 * @author yangguanchao
 * @since 2017/06/23
 */
public class TextMapB3Formatter extends AbstractTextB3Formatter {

    @Override
    public Format<TextMap> getFormatType() {
        return ExtendFormat.Builtin.B3_TEXT_MAP;
    }

    @Override
    protected String encodedValue(String value) {
        if (StringUtils.isBlank(value)) {
            return StringUtils.EMPTY_STRING;
        }
        return value;
    }

    @Override
    protected String decodedValue(String value) {
        if (StringUtils.isBlank(value)) {
            return StringUtils.EMPTY_STRING;
        }
        return value;
    }
}
