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
package com.alipay.sofa.tracer.boot.springmvc.processor;

import com.alipay.common.tracer.core.reactor.SofaTracerReactorSubscriber;
import org.reactivestreams.Publisher;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

import java.util.function.Function;

/**
 * @author xiang.sheng
 */
public class HookRegisteringBeanDefinitionRegistryPostProcessor implements
                                                               BeanDefinitionRegistryPostProcessor {
    public static final String SOFA_TRACE_REACTOR_KEY = "sofa-tracer-reactor";

    public static void resetHooks() {
        Hooks.resetOnEachOperator(SOFA_TRACE_REACTOR_KEY);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
                                                                                  throws BeansException {
        // ignore
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
                                                                                   throws BeansException {
        setupHooks();
    }

    private void setupHooks() {
        Hooks.onEachOperator(SOFA_TRACE_REACTOR_KEY, SofaReactor.scopePassingSpanOperator());
    }

    public static class SofaReactor {

        /**
         * Return a span operator pointcut, This can be used in
         * reactor via {@link reactor.core.publisher.Flux#transform(Function)},
         * {@link reactor.core.publisher.Mono#transform(Function)},
         * {@link reactor.core.publisher.Hooks#onLastOperator(Function)} or
         * {@link reactor.core.publisher.Hooks#onLastOperator(Function)}. The Span operator
         * pointcut will pass the Scope of the Span without ever creating any new spans.
         * @param <T> an arbitrary type that is left unchanged by the span operator
         * @return a new lazy span operator pointcut
         */
        @SuppressWarnings("unchecked")
        public static <T> Function<? super Publisher<T>, ? extends Publisher<T>> scopePassingSpanOperator() {
            return Operators.liftPublisher((p, sub) -> {
                // if Flux/Mono #just, #empty, #error
                if (p instanceof Fuseable.ScalarCallable) {
                    return sub;
                }
                // rest of the logic unchanged...
                return scopePassingSpanSubscription(sub, (p instanceof Mono));
            });
        }

        static <T> CoreSubscriber<? super T> scopePassingSpanSubscription(CoreSubscriber<? super T> sub, boolean unary) {
            return new SofaTracerReactorSubscriber<>(
                    sub, () -> {}, (s, e) -> null, unary);
        }
    }

}
