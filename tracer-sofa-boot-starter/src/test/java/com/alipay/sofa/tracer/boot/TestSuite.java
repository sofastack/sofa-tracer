package com.alipay.sofa.tracer.boot;

import com.alipay.sofa.tracer.boot.config.ConfigurationTest;
import com.alipay.sofa.tracer.boot.datasource.DataSourceTracerDisableTest;
import com.alipay.sofa.tracer.boot.opentracing.profiles.init.InitProfileTracerTest;
import com.alipay.sofa.tracer.boot.springmvc.SpringMvcFilterJsonOutputTest;
import com.alipay.sofa.tracer.boot.springmvc.SpringMvcFilterTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/26 8:13 PM
 * @since:
 **/
@Suite.SuiteClasses({
        InitProfileTracerTest.class,
        DataSourceTracerDisableTest.class,
        ConfigurationTest.class,
        SpringMvcFilterTest.class,
        SpringMvcFilterJsonOutputTest.class
})
@RunWith(Suite.class)
public class TestSuite {

}
