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
package initialize;

import com.alipay.common.tracer.core.listener.SpanReportListenerHolder;
import com.alipay.sofa.tracer.plugins.jaeger.initialize.JaegerReportRegisterBean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * JaegerReportRegisterBeanTest
 * @author: zhaochen
 */
public class JaegerReportRegisterBeanTest {
    private ClassPathXmlApplicationContext applicationContext;

    @Before
    public void init() {
        applicationContext = new ClassPathXmlApplicationContext("spring-bean.xml");
    }

    @Test
    public void testAfterPropertiesSet() {
        Object jaegerReportRegisterBean = applicationContext.getBean("jaegerReportRegisterBean");
        Assert.assertTrue(jaegerReportRegisterBean instanceof JaegerReportRegisterBean);
        Assert.assertTrue(SpanReportListenerHolder.getSpanReportListenersHolder().size() > 0);

    }
}
