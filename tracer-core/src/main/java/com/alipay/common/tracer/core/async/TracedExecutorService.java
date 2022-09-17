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

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TracedExecutorService extends TracedExecutor implements ExecutorService {

    protected final ExecutorService  delegate;

    public TracedExecutorService(ExecutorService delegate, SofaTracer tracer) {
        this(delegate, tracer, true);

    }
    public TracedExecutorService(ExecutorService delegate, SofaTracer tracer, boolean traceWithActiveSpanOnly) {
        super(delegate, tracer, traceWithActiveSpanOnly);
        this.delegate = delegate;

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
        SofaTracerSpan sofaTracerSpan = createSpan("submit");
        try{
            SofaTracerSpan toActivate = sofaTracerSpan != null ? sofaTracerSpan : (SofaTracerSpan) tracer.activeSpan();
            return delegate.submit(toActivate == null ? task :
                    new SofaTracerCallable<>(task, tracer));
        }finally {
            if(sofaTracerSpan!=null){
                sofaTracerSpan.finish();
            }
        }
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        SofaTracerSpan sofaTracerSpan = createSpan("submit");
        try{
            SofaTracerSpan toActivate = sofaTracerSpan != null ? sofaTracerSpan : (SofaTracerSpan) tracer.activeSpan();
            return delegate.submit(toActivate == null ? task :
                    new SofaTracerRunnable(task, tracer),result);
        }finally {
            if(sofaTracerSpan!=null){
                sofaTracerSpan.finish();
            }
        }

    }

    @Override
    public Future<?> submit(Runnable task) {
        SofaTracerSpan sofaTracerSpan = createSpan("submit");
        try{
            SofaTracerSpan toActivate = sofaTracerSpan != null ? sofaTracerSpan : (SofaTracerSpan) tracer.activeSpan();
            return delegate.submit(toActivate == null ? task :
                    new SofaTracerRunnable(task, tracer));
        }finally {
            if(sofaTracerSpan!=null){
                sofaTracerSpan.finish();
            }
        }
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
                                                                                 throws InterruptedException {
        SofaTracerSpan sofaTracerSpan = createSpan("invokeAll");
        try{
            SofaTracerSpan toActivate = sofaTracerSpan != null ? sofaTracerSpan : (SofaTracerSpan) tracer.activeSpan();
            return delegate.invokeAll(wrapTracerCallableCollection(tasks));
        } finally {
            if (sofaTracerSpan != null) {
                sofaTracerSpan.finish();
            }
        }
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
                                         TimeUnit unit) throws InterruptedException {
        SofaTracerSpan sofaTracerSpan = createSpan("invokeAll");
        try{
            SofaTracerSpan toActivate = sofaTracerSpan != null ? sofaTracerSpan : (SofaTracerSpan) tracer.activeSpan();
            return delegate.invokeAll(wrapTracerCallableCollection(tasks),timeout, unit);
        } finally {
            if (sofaTracerSpan != null) {
                sofaTracerSpan.finish();
            }
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException,
                                                                   ExecutionException {
        SofaTracerSpan sofaTracerSpan = createSpan("invokeAny");
        try{
            SofaTracerSpan toActivate = sofaTracerSpan != null ? sofaTracerSpan : (SofaTracerSpan) tracer.activeSpan();
            return delegate.invokeAny(wrapTracerCallableCollection(tasks));
        } finally {
            if (sofaTracerSpan != null) {
                sofaTracerSpan.finish();
            }
        }
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                                                                                                throws InterruptedException,
                                                                                                ExecutionException,
                                                                                                TimeoutException {
        SofaTracerSpan sofaTracerSpan = createSpan("invokeAny");
        try{
            SofaTracerSpan toActivate = sofaTracerSpan != null ? sofaTracerSpan : (SofaTracerSpan) tracer.activeSpan();
            return delegate.invokeAny(wrapTracerCallableCollection(tasks),timeout,unit);
        } finally {
            if (sofaTracerSpan != null) {
                sofaTracerSpan.finish();
            }
        }
    }


    private <T> Collection<? extends Callable<T>> wrapTracerCallableCollection(Collection<? extends Callable<T>> originalCollection) {
        Collection<Callable<T>> collection = new ArrayList<java.util.concurrent.Callable<T>>(
            originalCollection.size());
        for (Callable<T> c : originalCollection) {
            collection.add(new SofaTracerCallable<T>(c, tracer));
        }
        return collection;
    }
}
