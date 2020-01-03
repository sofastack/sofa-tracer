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
package com.alipay.sofa.tracer.boot.springcloud.configuration.hystrix;

import com.alipay.sofa.tracer.plugins.springcloud.instruments.feign.SofaTracerFeignClient;
import feign.Client;
import feign.Feign;
import feign.Retryer;
import feign.hystrix.HystrixFeign;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/12/31 1:20 PM
 * @since:
 **/
public class SofaTracerHystrixFeignBuilder {

    private SofaTracerHystrixFeignBuilder() {
    }

    public static Feign.Builder builder() {
        return HystrixFeign.builder().retryer(Retryer.NEVER_RETRY).client(client());
    }

    private static Client client() {
        return new SofaTracerFeignClient(new Client.Default(null, null));
    }
}
