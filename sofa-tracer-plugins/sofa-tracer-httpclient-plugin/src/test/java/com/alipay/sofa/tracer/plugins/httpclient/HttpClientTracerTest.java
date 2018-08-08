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
public class HttpClientTracerTest extends AbstractTestBase{

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
        String responseStr = HttpClientInstance.getSofaRouterHttpClientTemplateSingleton(10 * 1000).executeGet(httpGetUrl + path);
        assertFalse(StringUtils.isBlank(responseStr));

    }
} 
