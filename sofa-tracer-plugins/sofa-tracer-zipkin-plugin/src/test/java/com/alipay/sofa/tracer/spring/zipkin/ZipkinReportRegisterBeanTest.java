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
public class ZipkinReportRegisterBeanTest{

    private ClassPathXmlApplicationContext applicationContext ;

    @Before
    public void init(){
        applicationContext = new ClassPathXmlApplicationContext("spring-bean.xml");
    }
    @Test
    public void testAfterPropertiesSet(){
        Object zipkinReportRegisterBean = applicationContext.getBean("zipkinReportRegisterBean");
        Assert.assertTrue(zipkinReportRegisterBean instanceof ZipkinReportRegisterBean);
        Assert.assertTrue(SpanReportListenerHolder.getSpanReportListenersHolder().size() > 0);
    }
}
