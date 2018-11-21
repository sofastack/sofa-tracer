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
package com.alipay.sofa.tracer.spring.zipkin;

import com.alipay.common.tracer.core.listener.SpanReportListenerHolder;
import com.alipay.sofa.tracer.spring.zipkin.initialize.ZipkinReportRegisterBean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * ZipkinReportRegisterBeanTest
 *
 * @version 1.0
 * @author: guolei.sgl
 * @since: 18/11/21 下午5:54
 **/
public class ZipkinReportRegisterBeanTest {

    private ClassPathXmlApplicationContext applicationContext;

    @Before
    public void init() {
        applicationContext = new ClassPathXmlApplicationContext("spring-bean.xml");
    }

    @Test
    public void testAfterPropertiesSet() {
        Object zipkinReportRegisterBean = applicationContext.getBean("zipkinReportRegisterBean");
        Assert.assertTrue(zipkinReportRegisterBean instanceof ZipkinReportRegisterBean);
        Assert.assertTrue(SpanReportListenerHolder.getSpanReportListenersHolder().size() > 0);
    }
}
