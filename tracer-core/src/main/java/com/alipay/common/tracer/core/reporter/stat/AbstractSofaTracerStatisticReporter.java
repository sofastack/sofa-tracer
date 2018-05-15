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
package com.alipay.common.tracer.core.reporter.stat;

import com.alipay.common.tracer.core.appender.TraceAppender;
import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.appender.file.LoadTestAwareAppender;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.appender.self.Timestamp;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.reporter.stat.manager.SofaTracerStatisticReporterCycleTimesManager;
import com.alipay.common.tracer.core.reporter.stat.manager.SofaTracerStatisticReporterManager;
import com.alipay.common.tracer.core.reporter.stat.model.StatKey;
import com.alipay.common.tracer.core.reporter.stat.model.StatValues;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.AssertUtils;
import com.alipay.common.tracer.core.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AbstractSofaTracerStatisticReporter
 *
 * @author yangguanchao
 * @since 2017/06/26
 */
public abstract class AbstractSofaTracerStatisticReporter implements SofaTracerStatisticReporter {

    /**
     * 默认的周期为0(从0开始)，即输出间隔时间是一个周期时间（一个周期多长时间是可以设置的，默认是60s）,
     * {@link com.alipay.common.tracer.core.reporter.stat.manager.SofaTracerStatisticReporterManager#DEFAULT_CYCLE_SECONDS}
     */
    public static final int            DEFAULT_CYCLE = 0;

    /**
     * 用来控制初始化槽时的并发
     */
    private static final ReentrantLock initLock      = new ReentrantLock(false);

    /***
     * 输出拼接器
     */
    private static XStringBuilder      buffer        = new XStringBuilder();

    /***
     * 是否关闭统计日志打印,默认不关闭
     */
    protected AtomicBoolean            isClosePrint  = new AtomicBoolean(false);

    /***
     * 输出器
     */
    protected TraceAppender            appender      = null;

    /**
     * 统计日志的名称
     */
    protected String                   statTracerName;

    /***
     * 周期时间,一次统计数据的周期时间,单位:秒
     */
    private long                       periodTime;

    /**
     * 滚动策略
     */
    private String                     rollingPolicy;

    /**
     * 日志保留天数
     */
    private String                     logReserveConfig;

    /**
     * 输出周期间隔,单位:次数
     */
    private int                        printCycle    = 0;
    /**
     * 当前已被计数的周期数,单位:次数
     */
    private long                       countCycle    = 0;
    /**
     * "统计数据"滚动数组
     */
    private Map<StatKey, StatValues>[] statDatasPair = new ConcurrentHashMap[2];
    /**
     * "统计数据"滚动数组的当前下标
     */
    private int                        currentIndex  = 0;

    /**
     * 统计数据
     */
    protected Map<StatKey, StatValues> statDatas;

    public AbstractSofaTracerStatisticReporter(String statTracerName, String rollingPolicy,
                                               String logReserveConfig) {
        this(statTracerName, SofaTracerStatisticReporterManager.DEFAULT_CYCLE_SECONDS,
            DEFAULT_CYCLE, rollingPolicy, logReserveConfig);
    }

    public AbstractSofaTracerStatisticReporter(String statTracerName, long periodTime,
                                               int outputCycle, String rollingPolicy,
                                               String logReserveConfig) {

        AssertUtils.hasText(statTracerName, "Statistics tracer name cat't be empty.");
        this.statTracerName = statTracerName;
        //周期时间:单位秒
        this.periodTime = this.globalConfiguredCycleTime(periodTime);
        this.printCycle = outputCycle;
        this.rollingPolicy = rollingPolicy;
        this.logReserveConfig = logReserveConfig;
        for (int i = 0; i < 2; i++) {
            this.statDatasPair[i] = new ConcurrentHashMap<StatKey, StatValues>(100);
        }
        this.statDatas = statDatasPair[currentIndex];
        //注册定时任务并启动
        SofaTracerStatisticReporterCycleTimesManager.registerStatReporter(this);
    }

    /****
     * 获取统计日志的输出时间间隔
     * @param defaultCycle 默认间隔 60s
     * @return 统计日志的时间间隔
     */
    private long globalConfiguredCycleTime(long defaultCycle) {
        long cycleTime = defaultCycle;
        try {
            String statLogInterval = SofaTracerConfiguration
                .getProperty(SofaTracerConfiguration.STAT_LOG_INTERVAL);
            if (StringUtils.isNotBlank(statLogInterval)) {
                cycleTime = Long.parseLong(statLogInterval);
            }
        } catch (Exception e) {
            SelfLog.error("Parse stat log interval configure error", e);
        }
        SelfLog.warn(this.getStatTracerName() + " configured "
                     + SofaTracerConfiguration.STAT_LOG_INTERVAL + "=" + cycleTime
                     + " second and default cycle=" + defaultCycle);
        return cycleTime;
    }

    @Override
    public long getPeriodTime() {
        return this.periodTime;
    }

    @Override
    public String getStatTracerName() {
        return this.statTracerName;
    }

    @Override
    public void reportStat(SofaTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null) {
            return;
        }
        if (this.appender == null) {
            synchronized (this) {
                if (this.appender == null) {
                    this.appender = LoadTestAwareAppender
                        .createLoadTestAwareTimedRollingFileAppender(statTracerName, rollingPolicy,
                            logReserveConfig);
                }
            }
        }
        this.doReportStat(sofaTracerSpan);
    }

    /***
     * 执行统计操作,并调用 {@link AbstractSofaTracerStatisticReporter#addStat}
     * @param sofaTracerSpan 要被统计的 span
     */
    public abstract void doReportStat(SofaTracerSpan sofaTracerSpan);

    /**
     * 默认只提供累加的统计方法
     *
     * 向槽中更新数据 前面是唯一的key，后面是数值列 统计计算会对不同key的数值列进行加和
     *
     * @param keys   被统计 key 的唯一标示
     * @param values 被统计的值
     */
    protected void addStat(StatKey keys, long... values) {
        StatValues oldValues = statDatas.get(keys);
        if (oldValues == null) {
            // 初始化过程，需要加锁和二次判空
            initLock.lock();
            try {
                oldValues = statDatas.get(keys);
                // 新增一个key，先判断是否超出最大key上限
                if (null == oldValues) {
                    // 本次是第一次创建该槽，创建完毕设置第一次的统计值，即可返回
                    oldValues = new StatValues(values);
                    statDatas.put(keys, oldValues);
                    return;
                }
            } finally {
                initLock.unlock();
            }
        }
        // 已有其他线程创建过槽，合并新数据
        if (oldValues != null) {
            oldValues.update(values);
        }
    }

    /**
     * 切换当前下标并返回切换前的统计数据
     */
    @Override
    public Map<StatKey, StatValues> shiftCurrentIndex() {
        Map<StatKey, StatValues> last = statDatasPair[currentIndex];
        currentIndex = 1 - currentIndex;
        statDatas = statDatasPair[currentIndex];
        return last;
    }

    /**
     * 返回当前被统计的数据
     *
     * @return 当前被统计的数据
     */
    public Map<StatKey, StatValues> getStatData() {
        return new HashMap<StatKey, StatValues>(statDatas);
    }

    /**
     * 获取另一组非当前正在统计的数据,总共:两组数据进行统计和打印
     *
     * @return 非当前正在统计的数据
     */
    public Map<StatKey, StatValues> getOtherStatData() {
        return new HashMap<StatKey, StatValues>(statDatasPair[1 - currentIndex]);
    }

    @Override
    public boolean shouldPrintNow() {
        return 0 == (countCycle >= printCycle ? countCycle = 0 : ++countCycle);
    }

    @Override
    public void print(StatKey statKey, long[] values) {
        if (this.isClosePrint.get()) {
            //关闭统计日志输出
            return;
        }
        buffer.reset();
        buffer.append(Timestamp.currentTime()).append(statKey.getKey());
        int i = 0;
        for (; i < values.length - 1; i++) {
            buffer.append(values[i]);
        }
        buffer.append(values[i]);
        buffer.append(statKey.getResult());
        buffer.appendEnd(statKey.getEnd());
        try {
            if (appender instanceof LoadTestAwareAppender) {
                ((LoadTestAwareAppender) appender).append(buffer.toString(), statKey.isLoadTest());
            } else {
                appender.append(buffer.toString());
            }
            // 这里强制刷一次
            appender.flush();
        } catch (Throwable t) {
            SelfLog.error("统计日志<" + statTracerName + ">输出异常", t);
        }
    }

    @Override
    public void close() {
        this.isClosePrint.set(true);
    }

    public AtomicBoolean getIsClosePrint() {
        return isClosePrint;
    }

    public void setIsClosePrint(AtomicBoolean isClosePrint) {
        if (isClosePrint == null) {
            return;
        }
        this.isClosePrint.set(isClosePrint.get());
    }

    protected String buildString(String[] keys) {
        XStringBuilder sb = new XStringBuilder();
        int i;
        for (i = 0; i < keys.length - 1; i++) {
            sb.append(keys[i] == null ? "" : keys[i]);
        }
        sb.appendRaw(keys[i] == null ? "" : keys[i]);
        return sb.toString();
    }
}
