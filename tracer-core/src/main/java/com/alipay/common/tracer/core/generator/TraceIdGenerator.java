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
package com.alipay.common.tracer.core.generator;

import com.alipay.common.tracer.core.utils.TracerUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class TraceIdGenerator {

    private static String        IP_16 = "ffffffff";
    private static AtomicInteger count = new AtomicInteger(1000);

    static {
        try {
            String ipAddress = TracerUtils.getInetAddress();
            if (ipAddress != null) {
                IP_16 = getIP_16(ipAddress);
            }
        } catch (Throwable e) {
            /*
             * empty catch block
             */
        }
    }

    private static String getTraceId(String ip, long timestamp, int nextId) {
        StringBuilder appender = new StringBuilder(30);
        appender.append(ip).append(timestamp).append(nextId).append(TracerUtils.getPID());
        return appender.toString();
    }

    public static String generate() {
        return getTraceId(IP_16, System.currentTimeMillis(), getNextId());
    }

    private static String getIP_16(String ip) {
        String[] ips = ip.split("\\.");
        StringBuilder sb = new StringBuilder();
        for (String column : ips) {
            String hex = Integer.toHexString(Integer.parseInt(column));
            if (hex.length() == 1) {
                sb.append('0').append(hex);
            } else {
                sb.append(hex);
            }

        }
        return sb.toString();
    }

    private static int getNextId() {
        for (;;) {
            int current = count.get();
            int next = (current > 9000) ? 1000 : current + 1;
            if (count.compareAndSet(current, next)) {
                return next;
            }
        }
    }
}
