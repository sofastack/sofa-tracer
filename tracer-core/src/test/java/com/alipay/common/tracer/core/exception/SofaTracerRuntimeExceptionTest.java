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
package com.alipay.common.tracer.core.exception;

import com.alipay.common.tracer.core.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @description: [test for SofaTracerRuntimeExceptionTest]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/7/30
 */
public class SofaTracerRuntimeExceptionTest {

    private String exceptionCondition = "";

    @Test
    public void test_SofaTracerRuntimeException() {
        try {
            test_throw_1();
        } catch (SofaTracerRuntimeException e) {
            //test toString ;same as e.getMessage()
            Assert.assertEquals("SofaTracerRuntimeException with message", e.toString());
        }

        try {
            test_throw_2();
        } catch (SofaTracerRuntimeException e) {
            Assert.assertEquals("SofaTracerRuntimeException with message", e.getMessage());
            Assert.assertEquals("with throwable", e.getCause().getMessage());
        }
    }

    private void test_throw_1() throws SofaTracerRuntimeException {
        if (StringUtils.isBlank(exceptionCondition)) {
            throw new SofaTracerRuntimeException("SofaTracerRuntimeException with message");
        }
    }

    private void test_throw_2() throws SofaTracerRuntimeException {
        if (StringUtils.isBlank(exceptionCondition)) {
            throw new SofaTracerRuntimeException("SofaTracerRuntimeException with message",
                new Throwable("with throwable"));
        }
    }

}