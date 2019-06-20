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
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

/**
 * keep sofa tracer span across different CoreSubscriber
 *
 * @author xiang.sheng
 */
public class SofaTracerReactorSubscriber<T> extends InheritableBaseSubscriber<T> {
    public static String                                      SOFA_TRACER_CONTEXT_KEY = "sofa-tracer-context-key";

    private final CoreSubscriber<? super T>                   actual;
    private final Runnable                                    spanStartRunnable;
    private final BiFunction<SofaTracerSpan, Throwable, Void> spanFinishRunnable;

    private SofaTracerSpanContainer                           sofaTracerSpanContainer = new SofaTracerSpanContainer();

    private final AtomicBoolean                               entryExited             = new AtomicBoolean(
                                                                                          false);
    /**
     * Mono or Flux
     *
     * @see #hookOnNext(Object)
     */
    private final boolean                                     unary;

    public SofaTracerReactorSubscriber(CoreSubscriber<? super T> actual,
                                       Runnable spanStartRunnable,
                                       BiFunction<SofaTracerSpan, Throwable, Void> spanFinishRunnable,
                                       boolean unary) {
        this.actual = actual;
        this.spanStartRunnable = spanStartRunnable;
        this.spanFinishRunnable = spanFinishRunnable;
        this.unary = unary;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Context currentContext() {
        if (sofaTracerSpanContainer == null || entryExited.get()) {
            return actual.currentContext();
        }

        if (sofaTracerSpanContainer == null) {
            return actual.currentContext();
        }

        return actual.currentContext().put(SOFA_TRACER_CONTEXT_KEY, sofaTracerSpanContainer);
    }

    private void runOnSofaTracerSpan(Runnable runnable) {
        SofaTracerBarrier.runOnSofaTracerSpan(runnable, this.sofaTracerSpanContainer);
    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        recoverFromContext();
        runOnSofaTracerSpan(this.spanStartRunnable);
        actual.onSubscribe(this);
    }

    private void recoverFromContext() {
        if (actual.currentContext().hasKey(SOFA_TRACER_CONTEXT_KEY)) {
            // recover from existed container
            this.sofaTracerSpanContainer = actual.currentContext().get(SOFA_TRACER_CONTEXT_KEY);
        }
    }

    @Override
    protected void hookOnNext(T value) {
        if (isDisposed()) {
            tryCompleteEntry(null);
            return;
        }
        runOnSofaTracerSpan(() -> actual.onNext(value));

        if (unary) {
            // For some cases of unary operator (Mono), we have to do this during onNext hook.
            // e.g. this kind of order: onSubscribe() -> onNext() -> cancel() -> onComplete()
            // the onComplete hook will not be executed so we'll need to complete the entry in advance.
            tryCompleteEntry(null);
        }
    }

    @Override
    protected void hookOnComplete() {
        tryCompleteEntry(null);
        actual.onComplete();
    }

    @Override
    protected boolean shouldCallErrorDropHook() {
        // When flow control triggered or stream terminated, the incoming
        // deprecated exceptions should be dropped implicitly, so we'll not call the `onErrorDropped` hook.
        return !entryExited.get();
    }

    @Override
    protected void hookOnError(Throwable t) {
        tryCompleteEntry(t);
        actual.onError(t);
    }

    @Override
    protected void hookOnCancel() {
        super.hookOnCancel();
    }

    private boolean tryCompleteEntry(Throwable throwable) {
        if (this.sofaTracerSpanContainer != null && this.sofaTracerSpanContainer.isPresent()
                && entryExited.compareAndSet(false, true)) {
            runOnSofaTracerSpan(() -> spanFinishRunnable.apply(sofaTracerSpanContainer.get(), throwable));
            return true;
        }
        return false;
    }
}
