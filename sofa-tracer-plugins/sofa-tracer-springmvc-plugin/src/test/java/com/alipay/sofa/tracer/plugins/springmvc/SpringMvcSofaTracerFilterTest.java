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
package com.alipay.sofa.tracer.plugins.springmvc;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 19/1/30 PM2:19
 * @since:
 **/
public class SpringMvcSofaTracerFilterTest extends AbstractTestBase {

    /**
     * Test that exception information is not printed to tracer-self.log when an exception occurs during the execution of the business Filter chain
     * @throws Exception
     */
    @Test
    public void testFilterFail() throws Exception {

        checkFile();

        final SpringMvcSofaTracerFilter filter = new SpringMvcSofaTracerFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain mockFilterChain = new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
                                                                                                throws IOException,
                                                                                                ServletException {
                throw new RuntimeException("Has got exception...");
            }

            public void init(FilterConfig theConfig) {
            }

            public void destroy() {
            }
        };

        try {
            filter.doFilter(request, response, mockFilterChain);
        } catch (Exception e) {
            String message = e.getMessage();
            Assert.assertTrue(message.contains("Has got exception..."));
        }
        Thread.sleep(500);
        //wait for async output
        File file = new File(logDirectoryPath + File.separator + "tracer-self.log");
        if (file.exists()) {
            List<String> result = FileUtils.readLines(new File(logDirectoryPath + File.separator
                                                               + "tracer-self.log"));
            Assert.assertTrue(result.size() == 1);
        }
        Assert.assertTrue(!file.exists());
    }

    @Test
    public void testFilterSuccess() throws Exception {
        checkFile();

        final SpringMvcSofaTracerFilter filter = new SpringMvcSofaTracerFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain mockFilterChain = new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
                                                                                                throws IOException,
                                                                                                ServletException {

            }

            public void init(FilterConfig theConfig) {
            }

            public void destroy() {
            }
        };

        try {
            filter.doFilter(request, response, mockFilterChain);
        } catch (Exception e) {
            Assert.fail("Failed to execute SpringMvcSofaTracerFilter");
        }
        Thread.sleep(500);
        //wait for async output
        List<String> contents = FileUtils.readLines(new File(logDirectoryPath
                                                             + File.separator
                                                             + SpringMvcLogEnum.SPRING_MVC_DIGEST
                                                                 .getDefaultLogName()));
        Assert.assertTrue(contents.size() == 1);

        //wait for async output
        List<String> result = FileUtils.readLines(new File(logDirectoryPath + File.separator
                                                           + "tracer-self.log"));
        Assert.assertTrue(result.size() == 1);
    }

    private void checkFile() {
        File tracerSelfLog = new File(logDirectoryPath + File.separator + "tracer-self.log");
        if (tracerSelfLog.exists()) {
            tracerSelfLog.delete();
        }
    }
}
