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

import com.alipay.common.tracer.core.span.SofaTracerSpan;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;

import java.util.function.BiFunction;

/**
 * operator for flux operation
 *
 * @author sx
 */
public class FluxSofaOperator<T> extends FluxOperator<T, T> {
    private final Runnable                                    spanStartRunnable;
    private final BiFunction<SofaTracerSpan, Throwable, Void> spanFinishRunnable;

    /**
     * Build a {@link FluxOperator} wrapper around the passed parent {@link Publisher}
     *
     * @param source the {@link Publisher} to decorate
     */
    protected FluxSofaOperator(Flux<? extends T> source, Runnable spanStartRunnable,
                               BiFunction<SofaTracerSpan, Throwable, Void> spanFinishRunnable) {
        super(source);

        this.spanStartRunnable = spanStartRunnable;
        this.spanFinishRunnable = spanFinishRunnable;
    }

    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        source.subscribe(new SofaTracerReactorSubscriber<>(actual, this.spanStartRunnable,
            this.spanFinishRunnable, false));
    }
}
