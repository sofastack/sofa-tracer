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
package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.lang.Runnable;

public class TracedExecutorService implements ExecutorService {

    protected final ExecutorService  delegate;
    protected final SofaTraceContext traceContext;

    public TracedExecutorService(ExecutorService delegate) {
        this(delegate, SofaTraceContextHolder.getSofaTraceContext());
    }

    public TracedExecutorService(ExecutorService delegate, SofaTraceContext traceContext) {
        this.delegate = delegate;
        this.traceContext = traceContext;
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return delegate.submit(new SofaTracerCallable<T>(task, traceContext));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return delegate.submit(new SofaTracerRunnable(task, traceContext), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return delegate.submit(new SofaTracerRunnable(task, traceContext));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
                                                                                 throws InterruptedException {
        return delegate.invokeAll(wrapTracerCallableCollection(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
                                         TimeUnit unit) throws InterruptedException {
        return delegate.invokeAll(wrapTracerCallableCollection(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException,
                                                                   ExecutionException {
        return delegate.invokeAny(wrapTracerCallableCollection(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                                                                                                throws InterruptedException,
                                                                                                ExecutionException,
                                                                                                TimeoutException {
        return delegate.invokeAny(wrapTracerCallableCollection(tasks), timeout, unit);
    }

    @Override
    public void execute(final java.lang.Runnable command) {
        delegate.execute(new SofaTracerRunnable(command, traceContext));
    }

    private <T> Collection<? extends Callable<T>> wrapTracerCallableCollection(Collection<? extends Callable<T>> originalCollection) {
        Collection<Callable<T>> collection = new ArrayList<java.util.concurrent.Callable<T>>(
            originalCollection.size());
        for (Callable<T> c : originalCollection) {
            collection.add(new SofaTracerCallable<T>(c, traceContext));
        }
        return collection;
    }
}
