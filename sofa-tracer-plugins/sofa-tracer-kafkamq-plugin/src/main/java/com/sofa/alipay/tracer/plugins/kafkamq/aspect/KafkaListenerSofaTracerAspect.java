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
package com.sofa.alipay.tracer.plugins.kafkamq.aspect;

import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.sofa.alipay.tracer.plugins.kafkamq.tracers.KafkaMQConsumeTracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * KafkaListenerSofaTracerAspect。
 *
 * @author chenchen6 2020/9/2 00:20
 * @since 3.1.0-SNAPSHOT
 */
@Aspect
public class KafkaListenerSofaTracerAspect {

    private KafkaMQConsumeTracer consumeTracer;

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public void aroundKafkaConsumeAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        //start aop.
        boolean successFlag = true;
        try {
            //cause mq consumption action do not return values.
            joinPoint.proceed();
        } catch (Throwable throwable) {
            successFlag = false;
            throw throwable;
        } finally {
            consumeTracer = KafkaMQConsumeTracer.getKafkaMQConsumeTracerSingleton();
            consumeTracer.serverSend(successFlag ? SofaTracerConstant.RESULT_CODE_SUCCESS
                : SofaTracerConstant.RESULT_CODE_ERROR);
        }
    }

}
