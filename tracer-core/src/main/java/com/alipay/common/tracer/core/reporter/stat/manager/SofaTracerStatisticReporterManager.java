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

import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.reporter.stat.SofaTracerStatisticReporter;
import com.alipay.common.tracer.core.reporter.stat.model.StatKey;
import com.alipay.common.tracer.core.reporter.stat.model.StatValues;
import com.alipay.common.tracer.core.utils.StringUtils;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SofaTracerStatisticReporterManager
 * <p>
 * 固定时间周期的 Reporter，一个时钟周期对应一个实例,初始化之后就会启动周期
 *
 * @author yangguanchao
 * @since  2017/06/26
 */
public class SofaTracerStatisticReporterManager {

    /**
     * 阈值，如果统计日志的数据(map格式)key个数超过该值，则清空map,非 final 为了可测性
     */
    public static int                                CLEAR_STAT_KEY_THRESHOLD = 5000;

    /**
     * 默认输出周期 60 秒
     */
    static public final long                         DEFAULT_CYCLE_SECONDS    = 60;

    static final AtomicInteger                       THREAD_NUMBER            = new AtomicInteger(0);
    /***
     * 每一个固定周期调度都会有这样的一个实例
     */
    private Map<String, SofaTracerStatisticReporter> statReporters            = new ConcurrentHashMap<String, SofaTracerStatisticReporter>();

    /***
     * 周期时间,默认 {@link SofaTracerStatisticReporterManager#DEFAULT_CYCLE_SECONDS}=60 s
     */
    private long                                     cycleTime;

    private ScheduledExecutorService                 executor;

    SofaTracerStatisticReporterManager() {
        this(DEFAULT_CYCLE_SECONDS);
    }

    SofaTracerStatisticReporterManager(final long cycleTime) {
        this.cycleTime = cycleTime;
        this.executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

            public Thread newThread(Runnable r) {
                final Thread thread = new Thread(r, "Tracer-TimedAppender-"
                                                    + THREAD_NUMBER.incrementAndGet() + "-"
                                                    + cycleTime);
                thread.setDaemon(true);
                return thread;
            }
        });
        start();
    }

    private void start() {
        executor.scheduleAtFixedRate(new StatReporterPrinter(), 0, cycleTime, TimeUnit.SECONDS);
    }

    /***
     * 根据名称获取统计 Reporter 实例
     * @param statTracerName 统计日志 tracer 名称
     * @return 统计实现
     */
    public SofaTracerStatisticReporter getStatTracer(String statTracerName) {
        if (StringUtils.isBlank(statTracerName)) {
            return null;
        }
        return statReporters.get(statTracerName);
    }

    /***
     * 保存统计 Reporter 实例
     * @param statisticReporter 要保存的统计 Reporter 实例
     */
    public synchronized void addStatReporter(SofaTracerStatisticReporter statisticReporter) {
        if (statisticReporter == null) {
            return;
        }
        String statTracerName = statisticReporter.getStatTracerName();
        if (statReporters.containsKey(statTracerName)) {
            return;
        }
        statReporters.put(statTracerName, statisticReporter);
    }

    public Map<String, SofaTracerStatisticReporter> getStatReporters() {
        return statReporters;
    }

    class StatReporterPrinter implements Runnable {

        public void run() {
            SofaTracerStatisticReporter st = null;
            try {
                // 此任务默认 60 秒执行一次
                for (SofaTracerStatisticReporter statTracer : statReporters.values()) {
                    if (statTracer.shouldPrintNow()) {
                        st = statTracer;
                        // 切换下标并获取过去一段时间的statDatas
                        Map<StatKey, StatValues> statDatas = statTracer.shiftCurrentIndex();
                        for (Map.Entry<StatKey, StatValues> e : statDatas.entrySet()) {
                            StatKey statKeys = e.getKey();
                            StatValues values = e.getValue();
                            // 打印日志
                            long tobePrint[] = values.getCurrentValue();
                            // 当计数大于0的时候才打印
                            if (tobePrint[0] > 0) {
                                statTracer.print(statKeys, tobePrint);
                            }
                            // 更新槽中值，清除掉已打印内容
                            values.clear(tobePrint);// 这里必须保证传入的参数是print过程中使用的数组的值
                        }
                        // 如果该统计日志的key的数量大于阈值，表示key可能有带可变参数，因此清空掉防止占用太多内存
                        if (statDatas.size() > CLEAR_STAT_KEY_THRESHOLD) {
                            statDatas.clear();
                        }
                    }
                }
            } catch (Throwable t) {
                if (st != null) {
                    SelfLog.error("统计日志<" + st.getStatTracerName() + ">flush失败", t);
                }
            }

        }
    }
}
