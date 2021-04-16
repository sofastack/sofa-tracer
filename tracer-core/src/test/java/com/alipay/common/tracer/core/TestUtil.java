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
package com.alipay.common.tracer.core;

import com.alipay.common.tracer.core.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * @author qilong.zql
 * @since 2.2.2
 */
public class TestUtil {

    public static boolean compareSlotMap(String a, String b) {
        Map<String, String> aMap = new HashMap<>();
        StringUtils.stringToMap(a, aMap);
        Map<String, String> bMap = new HashMap<>();
        StringUtils.stringToMap(b, bMap);
        if (aMap.size() != bMap.size()) {
            return false;
        }
        for (String aKey : aMap.keySet()) {
            if (!aMap.get(aKey).equals(bMap.get(aKey))) {
                return false;
            }
        }
        return true;
    }

    public static void periodicallyAssert(Runnable assertion, long timeout) {
        for (int i = 0; i < timeout; i++) {
            try {
                assertion.run();
                return;
            } catch (AssertionError error) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
        }

        assertion.run();
    }

}