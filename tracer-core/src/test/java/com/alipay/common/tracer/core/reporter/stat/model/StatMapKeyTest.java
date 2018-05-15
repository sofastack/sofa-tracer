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
package com.alipay.common.tracer.core.reporter.stat.model;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * StatMapKey Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>五月 15, 2018</pre>
 */
public class StatMapKeyTest {

    /**
     * Method: addKey(String key, String value)
     */
    @Test
    public void testAddKey() throws Exception {
        StatMapKey statMapKey = new StatMapKey();
        statMapKey.addKey("key1", "value1");
        statMapKey.addKey("key2", "value2");
        statMapKey.addKey("key3", "value3");
        statMapKey.addKey("key4", "value4");
        statMapKey.addKey("key5", "value5");
        //
        StatMapKey statMapKey1 = new StatMapKey();
        statMapKey1.addKey("key3", "value3");
        statMapKey1.addKey("key4", "value4");
        statMapKey1.addKey("key5", "value5");
        statMapKey1.addKey("key1", "value1");
        statMapKey1.addKey("key2", "value2");
        assertEquals(statMapKey.hashCode(), statMapKey1.hashCode());
        assertEquals(statMapKey, statMapKey1);
        assertEquals(statMapKey.getKeyMap(), statMapKey1.getKeyMap());
        //
        StatMapKey statMapKey2 = new StatMapKey();
        statMapKey2.addKey("key3", "value3");
        statMapKey2.addKey("key4", "value4");
        statMapKey2.addKey("key5", "value5");
        statMapKey2.addKey("key1", "value1");
        assertFalse(statMapKey2.equals(statMapKey1));
        //
        statMapKey.setEnd("f");
        statMapKey.setResult("Y");
        statMapKey.setLoadTest(false);
        assertFalse(statMapKey.equals(statMapKey1));
        //
        statMapKey1.setEnd("t");
        statMapKey1.setResult("Y");
        statMapKey1.setLoadTest(false);
        assertFalse(statMapKey.equals(statMapKey1));
        //
        statMapKey1.setEnd("f");
        statMapKey1.setResult("Y");
        statMapKey1.setLoadTest(false);
        assertTrue(statMapKey.equals(statMapKey1));
    }
}
