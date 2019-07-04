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
import reactor.core.Fuseable;
import reactor.util.context.Context;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

/**
 * keep sofa tracer span across different CoreSubscriber
 *
 * @author xiang.sheng
 */
public class SofaTracerReactorSubscriber<T> extends InheritableBaseSubscriber<T>
                                                                                implements
                                                                                Fuseable.QueueSubscription<T>,
                                                                                Subscription {
    public static String                                      SOFA_TRACER_CONTEXT_KEY = "sofa-tracer-context-key";

    private final CoreSubscriber<? super T>                   actual;
    private final Runnable                                    wrapSofaRunnable;
    private final BiFunction<SofaTracerSpan, Throwable, Void> wrapSofaBiFunction;

    private SofaTracerSpanContainer                           sofaTracerSpanContainer = new SofaTracerSpanContainer();
    private Context                                           context;

    private final AtomicBoolean                               entryExited             = new AtomicBoolean(
                                                                                          false);
    /**
     * Mono or Flux
     *
     * @see #hookOnNext(Object)
     */
    private final boolean                                     unary;

    public SofaTracerReactorSubscriber(CoreSubscriber<? super T> actual,
                                       Runnable wrapSofaRunnable,
                                       BiFunction<SofaTracerSpan, Throwable, Void> wrapSofaBiFunction,
                                       boolean unary) {
        this.actual = actual;
        this.context = this.actual.currentContext();
        this.wrapSofaRunnable = wrapSofaRunnable;
        this.wrapSofaBiFunction = wrapSofaBiFunction;
        this.unary = unary;

        recoverFromContext();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Context currentContext() {
        return this.context;
    }

    private void runOnSofaTracerSpan(Runnable runnable) {
        SofaTracerBarrier.runWithSofaTracerSpan(runnable, this.sofaTracerSpanContainer);
    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        runOnSofaTracerSpan(this.wrapSofaRunnable);
        runOnSofaTracerSpan(() -> actual.onSubscribe(subscription));
    }

    @Override
    public void request(long n) {
        runOnSofaTracerSpan(() -> super.request(n));
    }

    private void recoverFromContext() {
        if (this.context.hasKey(SOFA_TRACER_CONTEXT_KEY)) {
            this.sofaTracerSpanContainer = this.context.get(SOFA_TRACER_CONTEXT_KEY);
        } else {
            this.context = this.context.put(SOFA_TRACER_CONTEXT_KEY, this.sofaTracerSpanContainer);
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
            runOnSofaTracerSpan(() -> wrapSofaBiFunction.apply(sofaTracerSpanContainer.get(), throwable));
            return true;
        }
        return false;
    }

    @Override
    public T poll() {
        return null;
    }

    @Override
    public int requestFusion(int i) {
        // always negotiate to no fusion
        return Fuseable.NONE;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void clear() {
        // NO-OP
    }
}
