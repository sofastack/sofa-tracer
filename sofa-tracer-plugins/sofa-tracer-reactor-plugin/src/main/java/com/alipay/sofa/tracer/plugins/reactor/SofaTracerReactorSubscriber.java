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

import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
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

    private final CoreSubscriber<? super T>                   actual;
    private final Runnable                                    startSpan;
    private final BiFunction<SofaTracerSpan, Throwable, Void> finishSpan;

    private SofaTracerSpan                                    sofaTracerSpan;

    private final AtomicBoolean                               entryExited = new AtomicBoolean(false);
    private final boolean                                     unary;

    public SofaTracerReactorSubscriber(CoreSubscriber<? super T> actual, Runnable startSpan,
                                       BiFunction<SofaTracerSpan, Throwable, Void> finishSpan,
                                       boolean unary) {
        this.actual = actual;
        this.startSpan = startSpan;
        this.finishSpan = finishSpan;
        this.unary = unary;
    }

    @Override
    public Context currentContext() {
        if (sofaTracerSpan == null || entryExited.get()) {
            return actual.currentContext();
        }

        if (sofaTracerSpan == null) {
            return actual.currentContext();
        }

        return actual.currentContext().put(SofaTracerReactorConstants.SOFA_TRACER_CONTEXT_KEY,
            sofaTracerSpan);
    }

    private void runOnSofaTracerSpan(Runnable f) {
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan backupSofaTracerSpan = sofaTraceContext.pop();

        if (this.sofaTracerSpan == null) {
            sofaTraceContext.clear();
        } else {
            sofaTraceContext.push(this.sofaTracerSpan);
        }

        try {
            f.run();
        } finally {
            /*
             * may create new sofa tracer span in runnable,
             * detect it and keep it
             */
            SofaTracerSpan newSpan = sofaTraceContext.getCurrentSpan();
            if (newSpan != null && !newSpan.equals(this.sofaTracerSpan)) {
                this.sofaTracerSpan = newSpan;
            }

            if (backupSofaTracerSpan == null) {
                sofaTraceContext.clear();
            } else {
                sofaTraceContext.push(backupSofaTracerSpan);
            }

        }

    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        runOnSofaTracerSpan(this.startSpan);
        actual.onSubscribe(this);
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
        if (this.sofaTracerSpan != null && entryExited.compareAndSet(false, true)) {
            runOnSofaTracerSpan(() -> finishSpan.apply(sofaTracerSpan, throwable));
            return true;
        }
        return false;
    }
}
