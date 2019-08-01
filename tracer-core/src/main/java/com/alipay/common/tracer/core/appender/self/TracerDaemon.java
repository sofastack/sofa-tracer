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
package com.alipay.common.tracer.core.appender.self;

import com.alipay.common.tracer.core.appender.TraceAppender;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tracer's Daemon thread, mainly to do the cleanup log
 *
 * @author khotyn 15/2/16 PM 4:32
 */
public class TracerDaemon implements Runnable {

    private static final long          ONE_HOUR         = 60 * 60;
    private static AtomicBoolean       running          = new AtomicBoolean(false);
    private static List<TraceAppender> watchedAppenders = new CopyOnWriteArrayList<TraceAppender>();
    private static long                scanInterval     = ONE_HOUR;

    /**
     * Register the Appender being monitored
     *
     * @param traceAppender
     */
    public static void watch(TraceAppender traceAppender) {
        watchedAppenders.add(traceAppender);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (true) {
            try {
                for (TraceAppender traceAppender : watchedAppenders) {
                    traceAppender.cleanup();
                }

                TimeUnit.SECONDS.sleep(scanInterval);
            } catch (Throwable e) {
                SelfLog.error("Error occurred while cleaning up logs.", e);
            }
        }
    }

    /**
     * Adjust the scanning cycle of the Daemon thread for testing convenience
     *
     * @param scanInterval Scan period in seconds
     */
    public static void setScanInterval(long scanInterval) {
        TracerDaemon.scanInterval = scanInterval;
    }

    public static void start() {
        if (running.compareAndSet(false, true)) {
            Thread deleteLogThread = new Thread(new TracerDaemon());
            deleteLogThread.setDaemon(true);
            deleteLogThread.setName("Tracer-Daemon-Thread");
            deleteLogThread.start();
        }
    }
}
