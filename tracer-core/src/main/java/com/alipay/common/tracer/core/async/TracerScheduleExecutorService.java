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

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Extended timing thread pool for SOFATracer
 *
 * @author luoguimu123
 * @version $Id: TracerScheduleExecutorService.java, v 0.1 June 22, 2017 11:43 AM luoguimu123 Exp $
 */
public class TracerScheduleExecutorService extends TracedExecutorService implements
                                                                        ScheduledExecutorService {


    public TracerScheduleExecutorService(ScheduledExecutorService delegate,
                                         SofaTracer tracer) {
        super(delegate, tracer, true);
    }
    public TracerScheduleExecutorService(ScheduledExecutorService delegate,
                                         SofaTracer tracer, boolean flag) {
        super(delegate, tracer, flag);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        SofaTracerSpan sofaTracerSpan = createSpan("schedule");
        try{
            SofaTracerSpan toActivate = sofaTracerSpan != null ? sofaTracerSpan : (SofaTracerSpan) tracer.activeSpan();
            return getScheduledExecutorService().schedule(tracer.activeSpan() == null ? command :
                    new SofaTracerRunnable(command, tracer),delay, unit);
        }finally {
            if(sofaTracerSpan!=null){
                sofaTracerSpan.finish();
            }
        }
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        SofaTracerSpan sofaTracerSpan = createSpan("schedule");
        try{
            SofaTracerSpan toActivate = sofaTracerSpan != null ? sofaTracerSpan : (SofaTracerSpan) tracer.activeSpan();
            return getScheduledExecutorService().schedule(tracer.activeSpan() == null ? callable :
                    new SofaTracerCallable<>(callable, tracer),delay, unit);
        }finally {
            if(sofaTracerSpan!=null){
                sofaTracerSpan.finish();
            }
        }

    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period,
                                                  TimeUnit unit) {
        SofaTracerSpan sofaTracerSpan = createSpan("scheduleAtFixedRate");
        try{
            SofaTracerSpan toActivate = tracer.activeSpan() != null ? sofaTracerSpan : (SofaTracerSpan) tracer.activeSpan();
            return getScheduledExecutorService().scheduleAtFixedRate(toActivate == null ? command :
                    new SofaTracerRunnable(command, tracer),initialDelay, period,unit);
        }finally {
            if(sofaTracerSpan!=null){
                sofaTracerSpan.finish();
            }
        }
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay,
                                                     long delay, TimeUnit unit) {
        SofaTracerSpan sofaTracerSpan = createSpan("scheduleWithFixedDelay");
        try{
            SofaTracerSpan toActivate = sofaTracerSpan != null ? sofaTracerSpan : (SofaTracerSpan) tracer.activeSpan();
            return getScheduledExecutorService().scheduleWithFixedDelay(toActivate == null ? command :
                    new SofaTracerRunnable(command, tracer),initialDelay, delay,unit);
        }finally {
            if(sofaTracerSpan!=null){
                sofaTracerSpan.finish();
            }
        }

    }

    private ScheduledExecutorService getScheduledExecutorService() {
        return (ScheduledExecutorService) this.delegate;
    }

}