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
package com.alipay.common.tracer.core.utils;

public class MicroTimestamp {

    public static final MicroTimestamp INSTANCE = new MicroTimestamp(); ;

    private volatile long              startDate;
    private volatile long              startNanoseconds;

    private MicroTimestamp() {
        this.startDate = System.currentTimeMillis();
        this.startNanoseconds = System.nanoTime();
    }

    /**
     * retrun value's part of Microsecond not reflect wall time, only use to
     * measurement call duration.
     */
    public long currentMicroSeconds() {
        long nanoSpan = System.nanoTime() - this.startNanoseconds;

        if (nanoSpan < 0) {
            synchronized (this) {
                nanoSpan = System.nanoTime() - this.startNanoseconds;
                if (nanoSpan < 0) {
                    this.startDate = System.currentTimeMillis();
                    this.startNanoseconds = System.nanoTime();
                    nanoSpan = System.nanoTime() - this.startNanoseconds;
                }
            }
        }
        return (this.startDate * 1000 + nanoSpan / 1000);
    }
}
