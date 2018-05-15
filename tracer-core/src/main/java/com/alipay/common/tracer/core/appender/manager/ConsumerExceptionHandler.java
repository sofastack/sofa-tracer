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
package com.alipay.common.tracer.core.appender.manager;

import com.alipay.common.tracer.core.appender.self.SynchronizingSelfLog;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.disruptor.ExceptionHandler;

/**
 *
 * @author liangen
 * @version $Id: ConsumerExceptionHandler.java, v 0.1 2017年10月23日 上午10:13 liangen Exp $
 */
public class ConsumerExceptionHandler implements ExceptionHandler<SofaTracerSpanEvent> {
    @Override
    public void handleEventException(Throwable ex, long sequence, SofaTracerSpanEvent event) {
        if (event != null) {
            SofaTracerSpan sofaTracerSpan = event.getSofaTracerSpan();
            SynchronizingSelfLog.error(
                "AsyncConsumer occurs exception during handle SofaTracerSpanEvent, The sofaTracerSpan is["
                        + sofaTracerSpan + "]", ex);
        } else {
            SynchronizingSelfLog
                .error(
                    "AsyncConsumer occurs exception during handle SofaTracerSpanEvent, The sofaTracerSpan is null",
                    ex);

        }
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        SynchronizingSelfLog.error("AsyncConsumer occurs exception on start", ex);

    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        SynchronizingSelfLog.error("Disruptor or AsyncConsumer occurs exception on shutdown", ex);

    }
}