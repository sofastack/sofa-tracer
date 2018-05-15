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
package com.alipay.common.tracer.core.reporter.stat.manager;

import com.alipay.common.tracer.core.reporter.stat.SofaTracerStatisticReporter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SofaTracerStatisticReporterCycleTimesManager
 *
 * @author yangguanchao
 * @since 2017/06/22
 */
public class SofaTracerStatisticReporterCycleTimesManager {

    private final static Map<Long, SofaTracerStatisticReporterManager> cycleTimesManager = new ConcurrentHashMap<Long, SofaTracerStatisticReporterManager>();

    public static Map<Long, SofaTracerStatisticReporterManager> getCycleTimesManager() {
        return cycleTimesManager;
    }

    /***
     * 周期:秒
     * 需要统一掉
     * @param statisticReporter 周期统计时间
     */
    public static void registerStatReporter(SofaTracerStatisticReporter statisticReporter) {
        SofaTracerStatisticReporterManager sofaTracerStatisticReporterManager = SofaTracerStatisticReporterCycleTimesManager
            .getSofaTracerStatisticReporterManager(statisticReporter.getPeriodTime());
        if (sofaTracerStatisticReporterManager != null) {
            sofaTracerStatisticReporterManager.addStatReporter(statisticReporter);
        }
    }

    /***
     * 定时任务以此为入口:获取指定周期时间的定时任务
     * @param cycleTime 周期时间单位：秒
     * @return SofaTracerStatisticReporterManager 固定周期的任务管理器
     */
    public static SofaTracerStatisticReporterManager getSofaTracerStatisticReporterManager(Long cycleTime) {
        if (cycleTime == null) {
            return null;
        }
        if (cycleTime <= 0) {
            return null;
        }
        SofaTracerStatisticReporterManager existedManager = cycleTimesManager.get(cycleTime);
        if (existedManager == null) {
            synchronized (cycleTimesManager) {
                if (cycleTimesManager.get(cycleTime) == null) {
                    existedManager = new SofaTracerStatisticReporterManager(cycleTime);
                    cycleTimesManager.put(cycleTime, existedManager);
                }
            }
        }
        return existedManager;
    }
}
