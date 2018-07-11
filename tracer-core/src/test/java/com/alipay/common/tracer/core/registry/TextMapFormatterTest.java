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
package com.alipay.common.tracer.core.registry;

import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.registry.propagation.B3Propagation;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TextMapFormatter Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>七月 8, 2017</pre>
 */
public class TextMapFormatterTest {

    private RegistryExtractorInjector<TextMap> registryExtractorInjector;

    @Before
    public void before() throws Exception {
        registryExtractorInjector = TracerFormatRegistry.getRegistry(Format.Builtin.TEXT_MAP);
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getFormatType()
     */
    @Test
    public void testGetFormatType() throws Exception {
        assertEquals(Format.Builtin.TEXT_MAP, registryExtractorInjector.getFormatType());
    }

    /**
     * Method: encodedValue(String value)
     * Method: decodedValue(String value)
     */
    @Test
    public void testEncodedValue() throws Exception {
        SofaTracerSpanContext spanContext = SofaTracerSpanContext.rootStart();
        Map<String, String> baggage = new HashMap<String, String>();
        baggage.put("key", "value");
        baggage.put("key1", "value1");
        baggage.put("key2", "value2");
        spanContext.addBizBaggage(baggage);
        //
        final TextMap carrier = new TextMap() {

            Map<String, String> carr = new HashMap<String, String>();

            @Override
            public Iterator<Map.Entry<String, String>> iterator() {
                return carr.entrySet().iterator();
            }

            @Override
            public void put(String key, String value) {
                carr.put(key, value);
            }

            @Override
            public String toString() {
                return "$classname{" + "carr=" + carr + '}';
            }
        };
        this.registryExtractorInjector.inject(spanContext, carrier);
        //carrier
        SofaTracerSpanContext extractContext = this.registryExtractorInjector.extract(carrier);
        //traceid spanId sampled
        extractContext.equals(spanContext);
        assertTrue("Extract : " + extractContext, baggage.equals(extractContext.getBizBaggage()));
    }

    public class Carrier4Test implements TextMap {
        Map<String, String> carr = new HashMap<String, String>();

        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            return carr.entrySet().iterator();
        }

        @Override
        public void put(String key, String value) {
            carr.put(key, value);
        }

        @Override
        public String toString() {
            return "$classname{" + "carr=" + carr + '}';
        }

        public String get(String key) {
            return carr.get(key);
        }

        public void remove(String key) {
            carr.remove(key);
        }
    };

    /**
     * Method: encodedValue(String value)
     * Method: decodedValue(String value)
     */
    @Test
    public void testEncodedValueB3() throws Exception {
        SofaTracerSpanContext spanContext = SofaTracerSpanContext.rootStart();
        Map<String, String> baggage = new HashMap<String, String>();
        baggage.put("key", "value");
        baggage.put("key1", "value1");
        baggage.put("key2", "value2");
        spanContext.addBizBaggage(baggage);

        Carrier4Test carrier = new Carrier4Test();
        this.registryExtractorInjector.inject(spanContext, carrier);
        assertEquals(spanContext.getTraceId(), carrier.get(B3Propagation.TRACE_ID_KEY_HEAD));
        assertEquals(spanContext.getSpanId(), carrier.get(B3Propagation.SPAN_ID_KEY_HEAD));
        assertEquals(spanContext.getParentId(), carrier.get(B3Propagation.PARENT_SPAN_ID_KEY_HEAD));

        //remove sofa trace context header from carrier, to test B3 extract logic
        carrier.remove(RegistryExtractorInjector.FORMATER_KEY_HEAD);
        SofaTracerSpanContext extractContext = this.registryExtractorInjector.extract(carrier);
        extractContext.equals(spanContext);
        assertTrue("Extract : " + extractContext, baggage.equals(extractContext.getBizBaggage()));
    }
}
