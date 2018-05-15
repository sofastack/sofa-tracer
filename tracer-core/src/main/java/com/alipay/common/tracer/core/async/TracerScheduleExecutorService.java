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

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 扩展的定时可有trace信息的定时线程池
 * @author luoguimu123
 * @version $Id: TracerScheduleExecutorService.java, v 0.1 2017年06月22日 上午11:43 luoguimu123 Exp $
 */
public class TracerScheduleExecutorService extends TracedExecutorService implements
                                                                        ScheduledExecutorService {

    public TracerScheduleExecutorService(ScheduledExecutorService delegate) {
        super(delegate, SofaTraceContextHolder.getSofaTraceContext());
    }

    public TracerScheduleExecutorService(ScheduledExecutorService delegate,
                                         SofaTraceContext traceContext) {
        super(delegate, traceContext);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        SofaTracerRunnable r = new SofaTracerRunnable(command, this.traceContext);
        return getScheduledExecutorService().schedule(r, delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        SofaTracerCallable c = new SofaTracerCallable(callable, this.traceContext);
        return getScheduledExecutorService().schedule(c, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period,
                                                  TimeUnit unit) {
        SofaTracerRunnable r = new SofaTracerRunnable(command, this.traceContext);
        return getScheduledExecutorService().scheduleAtFixedRate(r, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay,
                                                     long delay, TimeUnit unit) {
        SofaTracerRunnable r = new SofaTracerRunnable(command, this.traceContext);
        return getScheduledExecutorService().scheduleWithFixedDelay(r, initialDelay, delay, unit);
    }

    private ScheduledExecutorService getScheduledExecutorService() {
        return (ScheduledExecutorService) this.delegate;
    }

}