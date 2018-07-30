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
import io.opentracing.propagation.Format;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @description: [test for UnsupportedFormatException]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/7/30
 */
public class UnsupportedFormatExceptionTest {

    private Format<?> format;
    private String    exceptionCondition = null;

    @Before
    public void init() {
        format = Format.Builtin.TEXT_MAP;
    }

    @Test
    public void test_UnsupportedFormatException() {
        try {
            test_throw();
        } catch (UnsupportedFormatException e) {
            Assert.assertEquals("Builtin.TEXT_MAP", e.getMessage());
        }
    }

    private void test_throw() throws UnsupportedFormatException {
        if (StringUtils.isBlank(exceptionCondition)) {
            throw new UnsupportedFormatException(format);
        }
    }
}