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
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * TracerFormatRegistry Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>July 8, 2017</pre>
 */
public class TracerFormatRegistryTest {

    /**
     * Method: register(Format<T> format, RegistryExtractorInjector<T> extractor)
     */
    @Test
    public void testRegister() throws Exception {
        Registry registry = new Registry();
        TracerFormatRegistry.register(io.opentracing.propagation.Format.Builtin.BINARY, registry);
        Registry registry1 = (Registry) TracerFormatRegistry
            .getRegistry(io.opentracing.propagation.Format.Builtin.BINARY);
        assertTrue(registry == registry1);

        //Restore the registry affected by the test
        BinaryFormater binaryFormater = new BinaryFormater();
        TracerFormatRegistry.register(io.opentracing.propagation.Format.Builtin.BINARY,
            binaryFormater);
    }

    class Registry implements RegistryExtractorInjector {
        @Override
        public io.opentracing.propagation.Format getFormatType() {
            return null;
        }

        @Override
        public SofaTracerSpanContext extract(Object carrier) {
            return null;
        }

        @Override
        public void inject(SofaTracerSpanContext spanContext, Object carrier) {

        }
    }
}
