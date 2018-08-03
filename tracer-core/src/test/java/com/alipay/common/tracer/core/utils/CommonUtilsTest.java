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
package com.alipay.common.tracer.core.utils;

import com.alipay.common.tracer.core.generator.TraceIdGenerator;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;

import static junit.framework.TestCase.assertEquals;

public class CommonUtilsTest {
    @Test
    public void hexToDualLong() throws Exception {
        String traceIdOrig;
        String traceIdResult;
        Random rand = new Random();
        BigInteger traceIdNum;
        //Testing process random long long large than 120 bits
        for (int i = 0; i < 10000; i++) {
            traceIdNum = new BigInteger(125, 100, rand);
            traceIdOrig = traceIdNum.toString(16);
            long[] ids = CommonUtils.hexToDualLong(traceIdOrig);
            traceIdResult = String.format("%x%016x", ids[0], ids[1]);//.replaceFirst("^0+(?!$)", "");
            assertEquals(traceIdOrig, traceIdResult);
        }
        //Testing process sofa generated traceId
        for (int i = 0; i < 10000; i++) {
            traceIdOrig = TraceIdGenerator.generate();
            long[] ids = CommonUtils.hexToDualLong(traceIdOrig);
            // avoid hexadecimal high bit lost 0
            traceIdResult = String.format("%x%016x", ids[0], ids[1]);//.replaceFirst("^0+(?!$)", "");
            int diffLen = traceIdOrig.length() - traceIdResult.length();
            while (diffLen-- > 0) {
                traceIdResult = "0" + traceIdResult;
            }
            assertEquals(traceIdOrig, traceIdResult);
        }
    }

    @Test
    public void hexToLong() throws Exception {
        String traceIdOrig;
        String traceIdResult;
        Random rand = new Random();
        BigInteger traceIdNum;
        //Testing process random long long large than 120 bits
        for (int i = 0; i < 10000; i++) {
            traceIdNum = new BigInteger(64, 100, rand);
            traceIdOrig = traceIdNum.toString(16);
            long id = CommonUtils.hexToLong(traceIdOrig);
            traceIdResult = String.format("%x", id);
            assertEquals(traceIdOrig, traceIdResult);
        }
    }
}
