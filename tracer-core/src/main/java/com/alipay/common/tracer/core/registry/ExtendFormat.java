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

import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

public interface ExtendFormat<C> extends Format<C> {
    final class Builtin<C> implements ExtendFormat<C> {
        private final String name;

        private Builtin(String name) {
            this.name = name;
        }

        public final static Format<TextMap> B3_TEXT_MAP     = new ExtendFormat.Builtin<TextMap>(
                                                                "B3_TEXT_MAP");
        public final static Format<TextMap> B3_HTTP_HEADERS = new ExtendFormat.Builtin<TextMap>(
                                                                "B3_HTTP_HEADERS");

        @Override
        public String toString() {
            return ExtendFormat.Builtin.class.getSimpleName() + "." + name;
        }
    }
}
