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
package com.alipay.common.tracer.core.appender.info;

import com.alipay.common.tracer.core.appender.TraceAppender;
import com.alipay.common.tracer.core.appender.file.TimedRollingFileAppender;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.utils.TracerUtils;

import java.io.IOException;

/**
 * StaticInfoLog
 *
 * @author yangguanchao
 * @since 2017/07/02
 */
public class StaticInfoLog {

    static private TraceAppender appender;

    public synchronized static void logStaticInfo() {
        try {
            if (appender == null) {
                appender = new TimedRollingFileAppender("static-info.log", true);
            }
            String log = TracerUtils.getPID() + ",";
            log = log + (TracerUtils.getInetAddress() + ",");
            log = log + (TracerUtils.getCurrentZone() + ",");
            log = log + (TracerUtils.getDefaultTimeZone());
            appender.append(log + "\n");
            appender.flush();
        } catch (IOException e) {
            SelfLog.error("", e);
        }
    }
}
