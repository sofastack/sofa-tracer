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
package com.alipay.sofa.tracer.plugins.reactor;

import com.alipay.common.tracer.core.span.SofaTracerSpan;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author sx
 */
public class SofaTracerReactorTransformer<T> implements Function<Publisher<T>, Publisher<T>> {
    private final Runnable                                    startSpan;
    private final BiFunction<SofaTracerSpan, Throwable, Void> finishSpan;

    public SofaTracerReactorTransformer(Runnable startSpan,
                                        BiFunction<SofaTracerSpan, Throwable, Void> finishSpan) {
        this.startSpan = startSpan;
        this.finishSpan = finishSpan;
    }

    @Override
    public Publisher<T> apply(Publisher<T> publisher) {
        if (publisher instanceof Mono) {
            return new MonoSofaOperator<>((Mono<T>) publisher, this.startSpan, this.finishSpan);
        }

        if (publisher instanceof Flux) {
            return new FluxSofaOperator<>((Flux<T>) publisher, this.startSpan, this.finishSpan);
        }

        throw new IllegalStateException("Publisher type is not supported: "
                                        + publisher.getClass().getCanonicalName());
    }

}
