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
import com.alipay.common.tracer.core.listener.SpanReportListenerHolder;
import com.alipay.sofa.tracer.plugins.skywalking.initialize.SkywalkingReportRegisterBean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * SkywalkingReportRegisterBeanTest
 * @author zhaochen
 */
public class SkywalkingReportRegisterBeanTest {
    private ClassPathXmlApplicationContext applicationContext;

    @Before
    public void init() {
        applicationContext = new ClassPathXmlApplicationContext("spring-bean.xml");
    }

    @Test
    public void testAfterPropertiesSet() {
        Object reportRegisterBean = applicationContext.getBean("SkywalkingReportRegisterBean");
        Assert.assertTrue(reportRegisterBean instanceof SkywalkingReportRegisterBean);
        Assert.assertTrue(SpanReportListenerHolder.getSpanReportListenersHolder().size() > 0);
    }
}
