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
package com.alipay.sofa.tracer.plugins.zipkin.initialize;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.listener.SpanReportListener;
import com.alipay.common.tracer.core.listener.SpanReportListenerHolder;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.zipkin.ZipkinSofaTracerRestTemplateCustomizer;
import com.alipay.sofa.tracer.plugins.zipkin.ZipkinSofaTracerSpanRemoteReporter;
import com.alipay.sofa.tracer.plugins.zipkin.properties.ZipkinProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * ZipkinReportRegisterBean to parse properties and register zipkin report listeners
 *
 * @author guolei.sgl
 * @since v2.3.0
 */
public class ZipkinReportRegisterBean implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        // if do not match report condition,it will be return right now
        boolean enabled = false;
        String enabledStr = SofaTracerConfiguration
            .getProperty(ZipkinProperties.ZIPKIN_IS_ENABLED_KEY);
        if (StringUtils.isNotBlank(enabledStr) && "true".equalsIgnoreCase(enabledStr)) {
            enabled = true;
        }
        if (!enabled) {
            return;
        }

        boolean gzipped = false;
        String gzippedStr = SofaTracerConfiguration
            .getProperty(ZipkinProperties.ZIPKIN_IS_GZIPPED_KEY);
        if (StringUtils.isNotBlank(gzippedStr) && "true".equalsIgnoreCase(gzippedStr)) {
            gzipped = true;
        }

        RestTemplate restTemplate = new RestTemplate();
        ZipkinSofaTracerRestTemplateCustomizer zipkinSofaTracerRestTemplateCustomizer = new ZipkinSofaTracerRestTemplateCustomizer(
            gzipped);
        zipkinSofaTracerRestTemplateCustomizer.customize(restTemplate);
        String baseUrl = SofaTracerConfiguration.getProperty(ZipkinProperties.ZIPKIN_BASE_URL_KEY);
        SpanReportListener spanReportListener = new ZipkinSofaTracerSpanRemoteReporter(
            restTemplate, baseUrl);
        List<SpanReportListener> spanReportListenerList = new ArrayList<SpanReportListener>();
        spanReportListenerList.add(spanReportListener);
        SpanReportListenerHolder.addSpanReportListeners(spanReportListenerList);
    }
}
