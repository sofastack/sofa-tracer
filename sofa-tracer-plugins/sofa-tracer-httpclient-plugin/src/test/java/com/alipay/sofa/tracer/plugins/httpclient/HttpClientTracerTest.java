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
package com.alipay.sofa.tracer.plugins.httpclient;

import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.httpclient.base.AbstractTestBase;
import com.alipay.sofa.tracer.plugins.httpclient.base.client.HttpClientInstance;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * HttpClientTracer Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>08/08/2018</pre>
 */
public class HttpClientTracerTest extends AbstractTestBase {

    /**
     * Method: getHttpClientTracerSingleton()
     */
    @Test
    public void testHttpClientTracer() throws Exception {
        HttpClientTracer httpClientTracer = HttpClientTracer.getHttpClientTracerSingleton();
        HttpClientTracer httpClientTracer1 = HttpClientTracer.getHttpClientTracerSingleton();
        assertEquals(httpClientTracer, httpClientTracer1);
        String httpGetUrl = urlHttpPrefix;
        String path = "/httpclient";
        String responseStr = HttpClientInstance.getSofaRouterHttpClientTemplateSingleton(10 * 1000)
            .executeGet(httpGetUrl + path);
        assertFalse(StringUtils.isBlank(responseStr));

    }
}
