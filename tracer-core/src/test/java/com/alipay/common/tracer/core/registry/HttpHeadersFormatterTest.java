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
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * HttpHeadersFormatter Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>July 8, 2017</pre>
 */
public class HttpHeadersFormatterTest {

    private RegistryExtractorInjector<TextMap> registryExtractorInjector;

    @Before
    public void before() throws Exception {
        registryExtractorInjector = TracerFormatRegistry.getRegistry(Format.Builtin.HTTP_HEADERS);
        assertTrue(registryExtractorInjector instanceof HttpHeadersFormatter);
    }

    /**
     * Method: getFormatType()
     */
    @Test
    public void testGetFormatType() throws Exception {
        assertSame(Format.Builtin.HTTP_HEADERS, registryExtractorInjector.getFormatType());
    }

    /**
     * Method: encodedValue(String value)
     * <p>
     * Method: decodedValue(String value)
     */
    @Test
    public void testEncodedValue() throws Exception {
        SofaTracerSpanContext spanContext = SofaTracerSpanContext.rootStart();
        Map<String, String> baggage = new HashMap<>();
        baggage.put("key", "value");
        baggage.put("key1", "value1");
        baggage.put("key2", "value2");
        spanContext.addBizBaggage(baggage);
        //
        final TextMap carrier = new TextMap() {
            Map<String, String> carr = new HashMap<>();

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
        SofaTracerSpanContext extractContext = this.registryExtractorInjector.extract(carrier);
        // traceId spanId sampled
        extractContext.equals(spanContext);
        assertTrue("Extract baggage : " + extractContext.getBizBaggage(),
            baggage.equals(extractContext.getBizBaggage()));
    }

}
