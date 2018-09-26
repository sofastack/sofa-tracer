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
package com.alipay.sofa.tracer.spring.zipkin;

import com.alipay.sofa.tracer.spring.zipkin.properties.PropertiesHolder;

/**
 * ZipkinValidator : validate
 * @author guolei.sgl
 */
public class ZipkinValidator {

    static protected boolean zipkinV2IsAvailable;
    static protected boolean reporterV2IsAvailable;

    static {
        try {
            zipkinV2IsAvailable = null != Class.forName("zipkin2.Span");
        } catch (Throwable t) {
            zipkinV2IsAvailable = false;
        }

        try {
            reporterV2IsAvailable = null != Class.forName("zipkin2.reporter.AsyncReporter");
        } catch (Throwable t) {
            reporterV2IsAvailable = false;
        }
    }

    /**
     * Whether there is a dependency and configuration
     * required for zipkin reporting
     *
     * @return
     */
    public static boolean isMatchReport() {
        return zipkinV2IsAvailable && reporterV2IsAvailable && PropertiesHolder.getEnabled();
    }
}
