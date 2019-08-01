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
package com.alipay.common.tracer.core.middleware.parent;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

/**
 *
 * @author luoguimu123
 * @version $Id: AbstractDigestSpanEncoder.java, v 0.1 August 28, 2017 10:23 AM luoguimu123 Exp $
 */
public abstract class AbstractDigestSpanEncoder implements SpanEncoder<SofaTracerSpan> {

    /**
     * System transparent transmission of data
     * @param spanContext span context
     * @return String
     */
    protected String baggageSystemSerialized(SofaTracerSpanContext spanContext) {
        return spanContext.getSysSerializedBaggage();
    }

    /**
     * Business transparent transmission of data
     * @param spanContext span context
     * @return
     */
    protected String baggageSerialized(SofaTracerSpanContext spanContext) {
        return spanContext.getBizSerializedBaggage();
    }

}