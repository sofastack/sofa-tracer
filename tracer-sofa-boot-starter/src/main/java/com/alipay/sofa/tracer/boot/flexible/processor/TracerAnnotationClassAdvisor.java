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

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import javax.annotation.PostConstruct;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/8/9 3:17 PM
 * @since:
 **/
public class TracerAnnotationClassAdvisor extends AbstractPointcutAdvisor implements
                                                                         BeanFactoryAware {

    private Advice      advice;

    private Pointcut    pointcut;

    private BeanFactory beanFactory;

    @PostConstruct
    public void init() {
        this.pointcut = new TracerAnnotationClassPointcut();
        this.advice = new SofaTracerIntroductionInterceptor();
        if (this.advice instanceof BeanFactoryAware) {
            ((BeanFactoryAware) this.advice).setBeanFactory(this.beanFactory);
        }
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

}
