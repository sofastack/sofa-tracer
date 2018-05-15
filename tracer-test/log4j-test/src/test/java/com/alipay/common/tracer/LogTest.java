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
package com.alipay.common.tracer;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * 将控制台的输出重定向到项目目录下的output.txt中方便之后对文件中的内容读取并测试
 * @author luoguimu123
 * @version $Id: LogTest.java, v 0.1 2017年08月05日 上午7:59 luoguimu123 Exp $
 */
public class LogTest {

    private final String     tracerType = "SofaTracerSpanTest";

    private SofaTracer       sofaTracer;

    private SofaTracerSpan   sofaTracerSpan;

    private FileOutputStream fos;

    @Before
    public void setup() throws Exception {
        fos = new FileOutputStream("output.log");
        System.setOut(new PrintStream(fos));

        sofaTracer = new SofaTracer.Builder(tracerType).withTag("tracer",
            "SofaTraceContextHolderTest").build();
        //span
        sofaTracerSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("SofaTracerSpanTest").start();
        SofaTraceContextHolder.getSofaTraceContext().push(sofaTracerSpan);

    }

    @Test
    public void testLogback() throws IOException {
        Logger logger = LoggerFactory.getLogger(LogTest.class);
        logger.info("ssss");
        logger.info("hello world");

        SofaTracerSpan span = (SofaTracerSpan) this.sofaTracer.buildSpan("testCloneInstance")
            .asChildOf(sofaTracerSpan).start();
        span.setParentSofaTracerSpan(sofaTracerSpan);
        SofaTraceContextHolder.getSofaTraceContext().push(span);

        logger.info("childinfo1");

        SofaTraceContextHolder.getSofaTraceContext().pop();

        span.close();

        SofaTraceContextHolder.getSofaTraceContext().pop();

        logger.info("return to parent span");

        sofaTracerSpan.close();

        sofaTracer.close();
        fos.flush();
        fos.close();
        Scanner scanner = new Scanner(new FileInputStream("output.log"));
        String line1 = scanner.nextLine();
        String line2 = scanner.nextLine();
        String line3 = scanner.nextLine();
        String line4 = scanner.nextLine();

        Assert.assertTrue(line1 != null);
        Assert.assertTrue(line1.contains("ssss"));
        Assert.assertTrue(line1.contains(sofaTracerSpan.getSofaTracerSpanContext().getTraceId()));
        Assert.assertTrue(line1.contains("lgm"));

        Assert.assertTrue(line2 != null);
        Assert.assertTrue(line2.contains("hello world"));
        Assert.assertTrue(line2.contains(sofaTracerSpan.getSofaTracerSpanContext().getTraceId()));
        Assert.assertTrue(line2.contains("lgm"));

        Assert.assertTrue(line3 != null);
        Assert.assertTrue(line3.contains("childinfo1"));
        Assert.assertTrue(line3.contains(span.getSofaTracerSpanContext().getTraceId()));
        Assert.assertTrue(line3.contains("lgm"));

        Assert.assertTrue(line4 != null);
        Assert.assertTrue(line4.contains("return to parent span"));
        Assert.assertTrue(line4.contains(sofaTracerSpan.getSofaTracerSpanContext().getTraceId()));
        Assert.assertTrue(line4.contains("lgm"));

    }

}