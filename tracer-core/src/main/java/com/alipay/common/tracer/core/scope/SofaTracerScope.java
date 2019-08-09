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

import io.opentracing.Scope;
import io.opentracing.Span;

/**
 * @see io.opentracing.util.ThreadLocalScope
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/8/3 1:39 PM
 * @since:
 **/
public class SofaTracerScope implements Scope {

    private final SofaTracerScopeManager scopeManager;
    private final Span                   wrapped;
    private final boolean                finishOnClose;
    private final SofaTracerScope        toRestore;

    SofaTracerScope(SofaTracerScopeManager scopeManager, Span wrapped) {
        this(scopeManager, wrapped, false);
    }

    SofaTracerScope(SofaTracerScopeManager scopeManager, Span wrapped, boolean finishOnClose) {
        this.scopeManager = scopeManager;
        this.wrapped = wrapped;
        this.finishOnClose = finishOnClose;
        this.toRestore = scopeManager.tlsScope.get();
        scopeManager.tlsScope.set(this);
    }

    @Override
    public void close() {
        if (this.scopeManager.tlsScope.get() == this) {
            if (this.finishOnClose) {
                this.wrapped.finish();
            }

            this.scopeManager.tlsScope.set(this.toRestore);
        }
    }

    @Override
    public Span span() {
        return this.wrapped;
    }
}
