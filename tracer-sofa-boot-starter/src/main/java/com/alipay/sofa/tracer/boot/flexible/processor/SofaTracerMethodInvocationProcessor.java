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

import com.alipay.sofa.tracer.plugin.flexible.FlexibleTracer;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/8/9 2:51 PM
 * @since:
 **/
public class SofaTracerMethodInvocationProcessor implements SofaMethodInvocationProcessor,
                                                BeanFactoryAware {

    private BeanFactory           beanFactory;

    private io.opentracing.Tracer tracer;

    @Override
    public Object process(MethodInvocation invocation) throws Throwable {
        return proceedUnderSynchronousSpan(invocation);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private Object proceedUnderSynchronousSpan(MethodInvocation invocation) throws Throwable {

        if (tracer() instanceof FlexibleTracer) {
            try {
                ((FlexibleTracer) tracer()).beforeInvoke(invocation.getMethod().getName());
                return invocation.proceed();
            } catch (Exception ex) {
                ((FlexibleTracer) tracer()).afterInvoke(ex.getMessage());
                throw ex;
            } finally {
                ((FlexibleTracer) tracer()).afterInvoke(null);
            }
        } else {
            return invocation.proceed();
        }
    }

    io.opentracing.Tracer tracer() {
        if (this.tracer == null) {
            this.tracer = this.beanFactory.getBean(io.opentracing.Tracer.class);
        }
        return this.tracer;
    }

}
