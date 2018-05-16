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
package com.alipay.common.tracer.core.span;

import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.utils.AssertUtils;
import com.alipay.common.tracer.core.utils.StringUtils;

/**
 * SofaTracerSpanReferenceRelationship
 * <p>
 * {@link io.opentracing.References}
 *
 * @author yangguanchao
 * @since  2017/06/17
 */
public class SofaTracerSpanReferenceRelationship {

    private SofaTracerSpanContext sofaTracerSpanContext;

    /***
     * {@link io.opentracing.References}
     */
    private String                referenceType;

    public SofaTracerSpanReferenceRelationship(SofaTracerSpanContext sofaTracerSpanContext,
                                               String referenceType) {
        AssertUtils.isTrue(sofaTracerSpanContext != null,
            "SofaTracerSpanContext can't be null in SofaTracerSpanReferenceRelationship");
        AssertUtils.isTrue(!StringUtils.isBlank(referenceType), "ReferenceType can't be null");
        this.sofaTracerSpanContext = sofaTracerSpanContext;
        this.referenceType = referenceType;
    }

    public SofaTracerSpanContext getSofaTracerSpanContext() {
        return sofaTracerSpanContext;
    }

    public void setSofaTracerSpanContext(SofaTracerSpanContext sofaTracerSpanContext) {
        this.sofaTracerSpanContext = sofaTracerSpanContext;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }
}
