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
package com.alipay.sofa.tracer.boot.flexible.processor;

import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.sofa.tracer.plugin.flexible.annotations.Tracer;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/8/9 3:05 PM
 * @since:
 **/
public class SofaTracerIntroductionInterceptor implements IntroductionInterceptor, BeanFactoryAware {

    private BeanFactory                   beanFactory;

    private SofaMethodInvocationProcessor sofaMethodInvocationProcessor;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        if (method == null) {
            return invocation.proceed();
        }
        Method mostSpecificMethod = AopUtils.getMostSpecificMethod(method, invocation.getThis()
            .getClass());

        Tracer tracerSpan = findAnnotation(mostSpecificMethod, Tracer.class);
        if (tracerSpan == null) {
            return invocation.proceed();
        }
        return sofaMethodInvocationProcessor().process(invocation);
    }

    @Override
    public boolean implementsInterface(Class<?> aClass) {
        return true;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private SofaMethodInvocationProcessor sofaMethodInvocationProcessor() {
        if (this.sofaMethodInvocationProcessor == null) {
            this.sofaMethodInvocationProcessor = this.beanFactory
                .getBean(SofaMethodInvocationProcessor.class);
        }
        return this.sofaMethodInvocationProcessor;
    }

    private <T extends Annotation> T findAnnotation(Method method, Class<T> clazz) {
        T annotation = AnnotationUtils.findAnnotation(method, clazz);
        if (annotation == null) {
            try {
                annotation = AnnotationUtils.findAnnotation(
                    method.getDeclaringClass().getMethod(method.getName(),
                        method.getParameterTypes()), clazz);
            } catch (NoSuchMethodException | SecurityException ex) {
                SelfLog.warn("Exception occurred while tyring to find the annotation");
            }
        }
        return annotation;
    }
}