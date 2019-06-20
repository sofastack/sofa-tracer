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
package com.alipay.common.tracer.core.reactor;

import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import reactor.core.publisher.Mono;

import static com.alipay.common.tracer.core.reactor.SofaTracerReactorSubscriber.SOFA_TRACER_CONTEXT_KEY;

/**
 * within reactor mode, context is passed by ReactorContext
 * in some case, we still need to use code based on thread local with
 * reactor codes, this class try to unpack context from ReactorContext to
 * thread local and recover status when left reactor context
 *
 * @author xiang.sheng
 */
public class SofaTracerBarrier {
    public static void runOnSofaTracerSpan(Runnable runnable,
                                           SofaTracerSpanContainer sofaTracerSpanContainer) {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan backupSofaTracerSpan = sofaTraceContext.pop();
        SofaTracerSpan current = null;

        if (sofaTracerSpanContainer.isPresent()) {
            current = sofaTracerSpanContainer.get();
            sofaTraceContext.push(current);
        } else {
            sofaTraceContext.clear();
        }

        try {
            runnable.run();
        } finally {
            /*
             * may create new sofa tracer span in runnable,
             * detect it and keep it
             */
            SofaTracerSpan newSpan = sofaTraceContext.getCurrentSpan();
            if (newSpan != null && !newSpan.equals(current)) {
                sofaTracerSpanContainer.set(newSpan);
            }

            /*
             * after runnable run, current span is null but
             * the span controlled by reactor context is not null,
             *
             * that mean, the runnable `f` have finished span
             * so clear context
             */
            if (newSpan == null && current != null) {
                sofaTracerSpanContainer.clear();
            }

            if (backupSofaTracerSpan == null) {
                sofaTraceContext.clear();
            } else {
                sofaTraceContext.push(backupSofaTracerSpan);
            }

        }

    }

    public static Mono<SofaTracerSpanContainer> withSofaTracerContainer()  {
        return  Mono.subscriberContext().map(c -> {
            if (c.hasKey(SOFA_TRACER_CONTEXT_KEY)) {
                return c.get(SOFA_TRACER_CONTEXT_KEY);
            } else {
                return new SofaTracerSpanContainer();
            }
        }).transform(new SofaTracerReactorTransformer<>(
                () -> {}, (s, e) -> null
        ));
    }
}
