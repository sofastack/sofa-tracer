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

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * BinaryFormater Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>June 24, 2017</pre>
 */
public class BinaryFormaterTest {

    private RegistryExtractorInjector<ByteBuffer> registryExtractorInjector;

    @Before
    public void before() throws Exception {
        registryExtractorInjector = TracerFormatRegistry
            .getRegistry(ExtendFormat.Builtin.BYTE_BUFFER);
        assertTrue(registryExtractorInjector instanceof BinaryFormater);
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testStringBytesLength() throws Exception {
        String info = "hello";
        byte[] bytes = info.getBytes(SofaTracerConstant.DEFAULT_UTF8_CHARSET);
        Assert.assertTrue("Info Length : " + info.length() + ",bytes Length : " + bytes.length,
            bytes.length == info.length());
    }

    /**
     * Method: getFormatType()
     */
    @Test
    public void testGetFormatType() throws Exception {
        assertEquals(ExtendFormat.Builtin.BYTE_BUFFER, registryExtractorInjector.getFormatType());
    }

    /**
     * Put our spanContext in the data header
     * Method: inject(SofaTracerSpanContext spanContext, ByteBuffer carrier)
     * <p>
     * Method: extract(ByteBuffer carrier)
     */
    @Test
    public void testExtractCarrier() throws Exception {
        ByteBuffer carrierNull = null;
        SofaTracerSpanContext extractContext = this.registryExtractorInjector.extract(carrierNull);
        assertTrue(extractContext.toString(),
            extractContext.getSpanId().equals(SofaTracer.ROOT_SPAN_ID));

        ByteBuffer carrier = ByteBuffer.allocate(4);
        SofaTracerSpanContext extractContextAllo = this.registryExtractorInjector.extract(carrier);
        assertEquals(extractContextAllo.toString(), SofaTracer.ROOT_SPAN_ID,
            extractContextAllo.getSpanId());
    }

    /**
     * Put our spanContext in the data header
     * Method: inject(SofaTracerSpanContext spanContext, ByteBuffer carrier)
     * <p>
     * Method: extract(ByteBuffer carrier)
     */
    @Test
    public void testExtract() throws Exception {
        SofaTracerSpanContext spanContext = SofaTracerSpanContext.rootStart();
        Map<String, String> baggage = new HashMap<>();
        baggage.put("key", "value");
        baggage.put("key1", "value1");
        baggage.put("key2", "value2");
        spanContext.addBizBaggage(baggage);
        //inject
        //200 bytes
        ByteBuffer carrier = ByteBuffer.allocate(400);
        this.registryExtractorInjector.inject(spanContext, carrier);
        //extract
        SofaTracerSpanContext extractContext = this.registryExtractorInjector.extract(carrier);
        //traceid spanId sampled
        extractContext.equals(spanContext);
        assertTrue("Extract baggage : " + extractContext.getBizBaggage(),
            baggage.equals(extractContext.getBizBaggage()));
    }

    /**
     * Put our spanContext bytecode in the middle of the data
     * Method: inject(SofaTracerSpanContext spanContext, ByteBuffer carrier)
     * <p>
     * Method: extract(ByteBuffer carrier)
     */
    @Test
    public void testExtractInject() throws Exception {
        SofaTracerSpanContext spanContext = SofaTracerSpanContext.rootStart();
        Map<String, String> baggage = new HashMap<>();
        baggage.put("key", "value");
        baggage.put("key1", "value1");
        baggage.put("key2", "value2");
        spanContext.addBizBaggage(baggage);
        Map<String, String> sysBaggage = new HashMap<>();
        sysBaggage.put("sys1", "value1");
        sysBaggage.put("sys2", "value2");
        sysBaggage.put("sys3", "value3");
        spanContext.addSysBaggage(sysBaggage);

        //inject
        //200 bytes
        ByteBuffer carrier = ByteBuffer.allocate(400);
        //Put some data in advance to test
        String header = "index_=testSOFATracerInject";
        byte[] headerBytes = header.getBytes(SofaTracerConstant.DEFAULT_UTF8_CHARSET);
        carrier.put(headerBytes);
        this.registryExtractorInjector.inject(spanContext, carrier);
        //extract
        SofaTracerSpanContext extractContext = this.registryExtractorInjector.extract(carrier);
        //traceid spanId sampled
        extractContext.equals(spanContext);
        assertTrue("Extract baggage : " + extractContext.getBizBaggage(),
            baggage.equals(extractContext.getBizBaggage()));
        assertTrue("Extract System baggage : " + extractContext.getSysBaggage(),
            sysBaggage.equals(extractContext.getSysBaggage()));
    }

    /**
     * Note: This test case does not pass, only supports heap memory does not support off-heap memory
     * Method: inject(SofaTracerSpanContext spanContext, ByteBuffer carrier)
     * <p>
     *
     * Method: extract(ByteBuffer carrier)
     */
    @Test
    public void testDirectByteBufferExtractInjectException() throws Exception {
        SofaTracerSpanContext spanContext = SofaTracerSpanContext.rootStart();
        Map<String, String> baggage = new HashMap<String, String>();
        baggage.put("key", "value");
        baggage.put("key1", "value1");
        baggage.put("key2", "value2");
        spanContext.addBizBaggage(baggage);
        //inject
        //200 bytes
        ByteBuffer carrier = ByteBuffer.allocateDirect(200);
        this.registryExtractorInjector.inject(spanContext, carrier);
        //extract
        boolean isException = false;
        try {
            this.registryExtractorInjector.extract(carrier);
        } catch (UnsupportedOperationException exception) {
            isException = true;
        }
        assertTrue(isException);
    }
}
