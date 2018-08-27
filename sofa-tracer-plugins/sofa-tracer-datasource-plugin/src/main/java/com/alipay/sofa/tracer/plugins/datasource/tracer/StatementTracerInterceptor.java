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

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.registry.ExtendFormat;
import com.alipay.sofa.tracer.plugins.datasource.Interceptor;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @sicne 2.2.0
 */
public class StatementTracerInterceptor implements Interceptor {

    private DataSourceClientTracer clientTracer;

    public void setClientTracer(DataSourceClientTracer clientTracer) {
        this.clientTracer = clientTracer;
    }

    @Override
    public Object intercept(Chain chain) throws Exception {
        long start = System.currentTimeMillis();
        String resultCode = DataSourceClientTracer.RESULT_CODE_SUCCESS;
        try {
            clientTracer.startTrace(chain.getOriginalSql());
            return chain.proceed();
        } catch (Exception e) {
            resultCode = DataSourceClientTracer.RESULT_CODE_FAILED;
            throw e;
        } finally {
            clientTracer.endTrace(System.currentTimeMillis() - start, resultCode);
        }
    }
}