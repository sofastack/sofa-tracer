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
import com.alipay.common.tracer.core.reactor.base.AbstractTestBase;
import com.alipay.common.tracer.core.reactor.base.ReactorTracer;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import org.junit.Assert;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Fuseable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.alipay.common.tracer.core.reactor.SofaTracerReactorSubscriber.SOFA_TRACER_CONTEXT_KEY;

public class MonoSofaOperatorTest extends AbstractTestBase {
    @Test
    public void testMonoSofaOperator() throws Exception {
        StepVerifier.create(
                Mono.just(2)
                        .log()
                        .transform(new SofaTracerReactorTransformer<>(
                                () -> this.startSpan("testMonoSofaOperator"),
                                this::finishSpan))
                        .map(i -> {
                            SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
                            return sofaTraceContext.getCurrentSpan().getOperationName();
                        })
        ).expectNext("testMonoSofaOperator").verifyComplete();
    }

    @Test
    public void testFluxSofaOperator() throws Exception {
        StepVerifier.create(
                Flux.just(1, 2, 3)
                        .log()
                        .transform(new SofaTracerReactorTransformer<>(
                                () -> this.startSpan("testMonoSofaOperator"),
                                this::finishSpan))
                        .map(i -> {
                            SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
                            return sofaTraceContext.getCurrentSpan().getOperationName();
                        })
        ).expectNext("testMonoSofaOperator")
                .expectNext("testMonoSofaOperator")
                .expectNext("testMonoSofaOperator")
                .verifyComplete();
    }

    @Test
    public void testMonoSofaOperatorWithSpanChange() throws Exception {
        // make sure it have span before reactor run
        startSpan("testMonoSofaOperator-global");

        StepVerifier.create(
                Mono.just(2)
                        .log()
                        .transform(new SofaTracerReactorTransformer<>(
                                () -> this.startSpan("testMonoSofaOperator"),
                                this::finishSpan))
                        .map(i -> {
                            SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
                            return sofaTraceContext.getCurrentSpan().getOperationName();
                        })
        ).expectNext("testMonoSofaOperator").verifyComplete();

        // after reactor run, global span should restore
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        Assert.assertEquals(
                sofaTraceContext
                        .getCurrentSpan().getOperationName(),
                "testMonoSofaOperator-global"
        );
    }

    public static <T> Function<? super Publisher<T>, ? extends Publisher<T>> scopePassingSpanOperator() {
        return Operators.liftPublisher((p, sub) -> {
            // if Flux/Mono #just, #empty, #error
            if (p instanceof Fuseable.ScalarCallable) {
                return sub;
            }

            return new SofaTracerReactorSubscriber<>(
                    sub, () -> {
            }, (s, e) -> null, (p instanceof Mono));
        });
    }

    class TestSupplier implements Supplier<Mono<String>> {

        @Override
        public Mono<String> get() {
            return Mono.just(1).map(i -> {
                SofaTraceContext sofaTraceContext = SofaTraceContextHolder
                        .getSofaTraceContext();
                return sofaTraceContext.getCurrentSpan().getOperationName();
            });
        }
    }

    class TestDirectSupplier implements Supplier<Mono<String>> {

        @Override
        public Mono<String> get() {
            SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
            return Mono.just(sofaTraceContext.getCurrentSpan().getOperationName());
        }
    }

    @Test
    public void testWithSofaTracerContainer() throws Exception {
        startSpan("testMonoSofaOperator-global");
        StepVerifier.create(
                Mono.just(2)
                        .log()
                        .then(
                                Mono
                                        .defer(() ->
                                                SofaTracerBarrier.withSofaTracerContainer().map(c -> {
                                                    SofaTraceContext sofaTraceContext = SofaTraceContextHolder
                                                            .getSofaTraceContext();
                                                    return sofaTraceContext.getCurrentSpan().getOperationName();
                                                })).log()

                        )
                        .transform(new SofaTracerReactorTransformer<>(
                                () -> this.startSpan("testMonoSofaOperator"),
                                this::finishSpan))

        ).expectNext("testMonoSofaOperator").verifyComplete();

        // after reactor run, global span should restore
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        Assert.assertEquals(
                sofaTraceContext
                        .getCurrentSpan().getOperationName(),
                "testMonoSofaOperator-global"
        );
    }

    @Test
    public void testMonoSofaOperatorWithDefer() throws Exception {

        // make sure it have span before reactor run
        startSpan("testMonoSofaOperator-global");
        Hooks.onEachOperator("test", scopePassingSpanOperator());
        StepVerifier.create(
                Mono.just(2)
                        .log()
                        .then(
                                Mono.defer(new TestSupplier()).log()
                        )
                        .transform(new SofaTracerReactorTransformer<>(
                                () -> this.startSpan("testMonoSofaOperator"),
                                this::finishSpan))

        ).expectNext("testMonoSofaOperator").verifyComplete();

        // after reactor run, global span should restore
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        Assert.assertEquals(
                sofaTraceContext
                        .getCurrentSpan().getOperationName(),
                "testMonoSofaOperator-global"
        );
    }

    @Test
    public void testMonoSofaOperatorWithDeferDirectSupply() throws Exception {
        // make sure it have span before reactor run
        startSpan("testMonoSofaOperator-global");
        Hooks.onEachOperator("test", scopePassingSpanOperator());
        Hooks.onOperatorDebug();
        StepVerifier.create(
                Mono.just(2)
                        .log()
                        .then(
                                Mono.defer(new TestDirectSupplier()).log()
                        )
                        .transform(new SofaTracerReactorTransformer<>(
                                () -> this.startSpan("testMonoSofaOperator"),
                                this::finishSpan))

        ).expectNext("testMonoSofaOperator-global").verifyComplete();

        // after reactor run, global span should restore
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        Assert.assertEquals(
                sofaTraceContext
                        .getCurrentSpan().getOperationName(),
                "testMonoSofaOperator-global"
        );
    }

    private void startSpan(String operationName) {
        ReactorTracer reactorTracer = ReactorTracer.getReactorTracerSingleton();
        SofaTracerSpan springMvcSpan = reactorTracer.serverReceive();
        springMvcSpan.setOperationName(operationName);
        springMvcSpan.setTag(CommonSpanTags.LOCAL_APP, "");
        springMvcSpan.setTag(CommonSpanTags.REMOTE_APP, "");
        springMvcSpan.setTag(CommonSpanTags.REQUEST_URL, "");
        springMvcSpan.setTag(CommonSpanTags.METHOD, "");
        springMvcSpan.setTag(CommonSpanTags.REQ_SIZE, "");

    }

    private Void finishSpan(SofaTracerSpan sofaTracerSpan, Throwable throwable) {
        ReactorTracer springMvcTracer = ReactorTracer.getReactorTracerSingleton();
        springMvcTracer.serverSend("200");
        return null;
    }

}
