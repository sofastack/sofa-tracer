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
package com.alipay.sofa.tracer.boot.test;

import com.alipay.sofa.tracer.plugin.flexible.FlexibleTracer;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/8/10 12:17 AM
 * @since:
 **/
@Aspect
public class SofaTracerAspect {

    private final io.opentracing.Tracer tracer;

    public SofaTracerAspect(io.opentracing.Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * 拦截所有被 @Tracer 注解描述的方法
     */
    @Pointcut("execution(* *(..)) &&  @annotation(com.alipay.sofa.tracer.plugin.flexible.annotations.Tracer)")
    public void cutIn() {
    }

    @Before("cutIn()")
    public void beforeExecute(JoinPoint point) {
        String methodName = point.getSignature().getName();
        if (tracer instanceof FlexibleTracer) {
            ((FlexibleTracer) tracer).beforeInvoke(methodName);
        }
    }

    @AfterReturning("cutIn()")
    public void afterExecute() {
        if (tracer instanceof FlexibleTracer) {
            ((FlexibleTracer) tracer).afterInvoke(null);
        }
    }

    @AfterThrowing(throwing = "ex", pointcut = "cutIn()")
    public void afterThrowing(Throwable ex) {
        if (tracer instanceof FlexibleTracer) {
            ((FlexibleTracer) tracer).afterInvoke(ex.getMessage());
        }
    }
}
