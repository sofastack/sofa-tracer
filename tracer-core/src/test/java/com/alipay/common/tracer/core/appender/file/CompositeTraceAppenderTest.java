package com.alipay.common.tracer.core.appender.file;

import com.alipay.common.tracer.core.appender.TraceAppender;
import com.alipay.common.tracer.core.appender.TracerLogRootDaemon;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @description: [描述文本]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/7/27
 */
public class CompositeTraceAppenderTest {

    private static final String                 COMPOSITE_TEST_FILE_NAME = "composite-test.log";
    private TimedRollingFileAppender            timedRollingFileAppender;
    private PathMatchingResourcePatternResolver resolver               = new PathMatchingResourcePatternResolver();

    CompositeTraceAppender compositeTraceAppender;

    @Before
    public void init() throws IOException {
        timedRollingFileAppender = new TimedRollingFileAppender(COMPOSITE_TEST_FILE_NAME,
                AbstractRollingFileAppender.DEFAULT_BUFFER_SIZE, true, "'.'yyyy-MM-dd.HH:mm:ss");
        compositeTraceAppender = new CompositeTraceAppender();
        MockTraceAppender mockTracerAppender = new MockTraceAppender();
        compositeTraceAppender.putAppender("timedRollingFileAppender",timedRollingFileAppender);
        compositeTraceAppender.putAppender("mockTracerAppender",mockTracerAppender);
    }
    @Test
    public void getAppender() {
        TraceAppender timedRollingFileAppender = compositeTraceAppender.getAppender("timedRollingFileAppender");
        TraceAppender mockTracerAppender = compositeTraceAppender.getAppender("mockTracerAppender");
        Assert.assertEquals(timedRollingFileAppender.hashCode(),timedRollingFileAppender.hashCode());
        Assert.assertEquals(mockTracerAppender.hashCode(),mockTracerAppender.hashCode());
    }

    @Test
    public void flush() {

    }

    @Test
    public void append() throws IOException {
        compositeTraceAppender.cleanup();
        compositeTraceAppender.append("test compositeTraceAppender");
        compositeTraceAppender.flush();
        Resource[] resources = resolver.getResources("file:" + TracerLogRootDaemon.LOG_FILE_DIR
                + File.separator + COMPOSITE_TEST_FILE_NAME);
        Assert.assertTrue(resources.length==1);
    }

    @Test
    public void cleanup() {
    }


    class MockTraceAppender implements TraceAppender{

        @Override
        public void flush() throws IOException {

        }

        @Override
        public void append(String log) throws IOException {

        }

        @Override
        public void cleanup() {

        }
    }
}