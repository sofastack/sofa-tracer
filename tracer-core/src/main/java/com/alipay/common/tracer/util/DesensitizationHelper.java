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
package com.alipay.common.tracer.util;

import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;

import java.util.ServiceLoader;

/**
 * Desensitization Helper.
 * <p>
 * {@link DesensitizationHelper#desensitize(String)} can only reach 7000 OPS (1 thread) measured by JMH in my
 * Mac Pro.
 * <p> All our {@link com.alipay.common.tracer.core.appender.encoder.SpanEncoder}s are running in the same single thread.
 * Thus performance is seriously damaged. Developers should only enable desensitization with this concern.
 * <p> If you want to enable desensitization, you need to add <em>antmasking-scan</em> dependency with compile
 * scope manually.
 * <p>created at 2021/3/26
 *
 * @author xiangfeng.xzc
 */
public class DesensitizationHelper {
    public static final String    ENABLED_KEY           = "tracer_desens_enabled";
    public static final String    ENABLED_DEFAULT_VALUE = "false";
    /**
     * Cool down log
     */
    private static final CoolDown COOL_DOWN             = new CoolDown(60000L, 10);
    static volatile Desensitizer  desensitizer;

    static {
        try {
            ServiceLoader<Desensitizer> loader = ServiceLoader.load(Desensitizer.class,
                DesensitizationHelper.class.getClassLoader());

            Desensitizer first = null;
            for (Desensitizer d : loader) {
                if (first == null) {
                    first = d;
                }
                String msg = String.format("Find Desensitizer impl: %s %s", d.getClass(),
                    d.toString());
                SelfLog.info(msg);
            }

            // use first
            desensitizer = first;
        } catch (Throwable e) {
            SelfLog.warn("Fail to find class ScanAndDesensUtil, desensitization is disabled", e);
        }
    }

    /**
     * Set a user defined desensitizer.
     *
     * @param desensitizer
     */
    public static void setDesensitizer(Desensitizer desensitizer) {
        DesensitizationHelper.desensitizer = desensitizer;
    }

    /**
     * Desensitize str
     *
     * @param str
     * @return
     */
    public static String desensitize(String str) {
        // disabled return original str
        if (!enabled()) {
            return str;
        }
        Desensitizer desensitizer = DesensitizationHelper.desensitizer;
        if (desensitizer == null) {
            return str;
        }

        try {
            return desensitizer.desensitize(str);
        } catch (Throwable e) {
            if (COOL_DOWN.tryAcquire()) {
                SelfLog.error("fail to desensitize: " + str, e);
            }
            return str;
        }
    }

    private static boolean enabled() {
        // This property may change at runtime. So we have to check every time.
        return desensitizer != null && //
               "true".equals(SofaTracerConfiguration
                   .getProperty(ENABLED_KEY, ENABLED_DEFAULT_VALUE));
    }

}
