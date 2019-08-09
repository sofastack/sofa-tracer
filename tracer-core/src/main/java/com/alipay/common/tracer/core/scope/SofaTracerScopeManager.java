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
package com.alipay.common.tracer.core.scope;

import com.alipay.common.tracer.core.span.SofaTracerSpan;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

/**
 * @see io.opentracing.util.ThreadLocalScopeManager
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/8/3 1:38 PM
 * @since:
 **/
public class SofaTracerScopeManager implements ScopeManager {

    final ThreadLocal<SofaTracerScope> tlsScope = new ThreadLocal();

    public SofaTracerScopeManager() {
    }

    @Override
    public Scope activate(Span span, boolean finishOnClose) {
        return new SofaTracerScope(this, span, finishOnClose);
    }

    @Override
    public Scope activate(Span span) {
        if (span == null) {
            return null;
        }
        if (!(span instanceof SofaTracerSpan)) {
            throw new IllegalArgumentException(
                "Span must be an instance of SofaTracerSpan, but was " + span.getClass());
        }
        return new SofaTracerScope(this, span);
    }

    @Override
    public Scope active() {
        return this.tlsScope.get();
    }

    @Override
    public Span activeSpan() {
        Scope scope = this.tlsScope.get();
        return scope == null ? null : scope.span();
    }

}
