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
///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
package com.alipay.sofa.tracer.boot.springcloud.configuration.stream.integration;

import com.alipay.sofa.tracer.boot.configuration.SofaTracerAutoConfiguration;
import com.alipay.sofa.tracer.boot.springcloud.properties.SofaTracerMessagingProperties;
import com.alipay.sofa.tracer.plugins.springcloud.instruments.stream.SofaTracerChannelInterceptor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.interceptor.GlobalChannelInterceptorWrapper;
import org.springframework.integration.config.GlobalChannelInterceptor;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/11/28 11:02 PM
 * @since:
 **/
@Configuration
@ConditionalOnClass(GlobalChannelInterceptor.class)
@AutoConfigureAfter({ SofaTracerAutoConfiguration.class })
@ConditionalOnProperty(value = "com.alipay.sofa.tracer.springcloud.stream.enabled", matchIfMissing = true)
@EnableConfigurationProperties(SofaTracerMessagingProperties.class)
public class SofaTracerIntegrationAutoConfiguration {

    @Bean
    public GlobalChannelInterceptorWrapper globalChannelInterceptorWrapper(SofaTracerChannelInterceptor interceptor,
                                                                           SofaTracerMessagingProperties properties) {
        GlobalChannelInterceptorWrapper wrapper = new GlobalChannelInterceptorWrapper(interceptor);
        wrapper.setPatterns(properties.getIntegration().getPatterns());
        return wrapper;
    }

    @Bean
    SofaTracerChannelInterceptor traceChannelInterceptor() {
        return SofaTracerChannelInterceptor.create();
    }

}
