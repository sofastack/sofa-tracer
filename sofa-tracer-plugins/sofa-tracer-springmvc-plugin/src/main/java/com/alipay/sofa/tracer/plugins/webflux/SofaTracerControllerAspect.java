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
package com.alipay.sofa.tracer.plugins.webflux;

import com.alipay.common.tracer.core.reactor.SofaTracerBarrier;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.web.reactive.HandlerResult;
import reactor.core.publisher.Mono;

/**
 * in webflux, every request
 *
 * @author xiang.sheng
 */
@Aspect
public class SofaTracerControllerAspect {

    @Pointcut("execution(* org.springframework.web.reactive.HandlerAdapter.handle(..))")
    private void invokePointCut() {
    }

    @Around("invokePointCut()")
    public Object wrapReactorContext(ProceedingJoinPoint pjp) {
        return SofaTracerBarrier.withSofaTracerContainer().flatMap(c -> {
                    try {
                        //noinspection unchecked
                        return (Mono<HandlerResult>) pjp.proceed();
                    } catch (Throwable throwable) {
                        return Mono.error(throwable);
                    }
                }
        );
    }
}
