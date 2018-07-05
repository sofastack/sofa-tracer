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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MicroTimestampTest {
    @Test
    public void testSingleTon() {
        MicroTimestamp instance1 = MicroTimestamp.INSTANCE;
        MicroTimestamp instance2 = MicroTimestamp.INSTANCE;

        assertEquals(instance1, instance2);
        assertTrue(instance1 == instance2);
    }

    @Test
    public void testCurrentMicroSeconds() throws InterruptedException {
        long startMicros = MicroTimestamp.INSTANCE.currentMicroSeconds();
        long endMicros = MicroTimestamp.INSTANCE.currentMicroSeconds();
        assertTrue(startMicros > 0);
        assertTrue(endMicros > 0);
        assertTrue(endMicros >= startMicros);
    }

}
