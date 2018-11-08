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

import com.alipay.common.tracer.core.appender.file.TimedRollingFileAppender;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.reporter.stat.manager.SofaTracerStatisticReporterManager;
import com.alipay.common.tracer.core.reporter.stat.model.StatKey;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import org.junit.*;

import java.util.Date;

/**
 * SofaTracerStatisticReporterImpl Tester.
 *
 * @author <guanchao.ygc>
 * @version 1.0
 * @since <pre>七月 2, 2017</pre>
 */
public class SofaTracerStatisticReporterImplTest {

    /***
     * 1s 钟统计一次
     */
    private final long CYCLE_IN_SECONDS = 1;

    static {
        //调整阈值可测性
        SofaTracerStatisticReporterManager.CLEAR_STAT_KEY_THRESHOLD = 5;
        System.setProperty("com.alipay.ldc.zone", "GZ00A");

    }

    @Before
    public void init() {
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.STAT_LOG_INTERVAL, "");
    }

    @AfterClass
    public static void afterCl() {
        SofaTracerStatisticReporterManager.CLEAR_STAT_KEY_THRESHOLD = 5000;
    }

    /**
     * 测试keys太多，定时清空的场景 如单独测试，可以把TracerConfiguration.CLEAR_STAT_KEY_THRESHOLD调小以方便测试
     */
    @Test
    public void testClearKeys() throws InterruptedException {
        String name = "testClearKeys";
        AbstractSofaTracerStatisticReporter statReporter = new AbstractSofaTracerStatisticReporter(
            name, CYCLE_IN_SECONDS, AbstractSofaTracerStatisticReporter.DEFAULT_CYCLE,
            TimedRollingFileAppender.DAILY_ROLLING_PATTERN, "14") {
            @Override
            public void doReportStat(SofaTracerSpan sofaTracerSpan) {
                StatKey keys = new StatKey();
                long values[] = new long[0];
                this.addStat(keys, values);
            }

        };

        // case 1: 切换时没有达到阈值
        for (int i = 0; i < SofaTracerStatisticReporterManager.CLEAR_STAT_KEY_THRESHOLD; i++) {
            StatKey statKey = new StatKey();
            statKey.setKey(String.valueOf(i));
            statReporter.addStat(statKey, i);
        }

        int currentSize = statReporter.getStatData().size();
        while (true) {
            if (statReporter.getStatData().size() != currentSize) {
                break;
            }
            Thread.sleep(100);
        }

        // 此时发生过下标切换
        Assert.assertEquals(0, statReporter.getStatData().size());
        Assert.assertEquals(SofaTracerStatisticReporterManager.CLEAR_STAT_KEY_THRESHOLD,
            statReporter.getOtherStatData().size());

        // case 2: 切换时达到阈值
        for (int i = 0; i <= SofaTracerStatisticReporterManager.CLEAR_STAT_KEY_THRESHOLD; i++) {
            StatKey statKey = new StatKey();
            statKey.setKey(String.valueOf(i));
            statReporter.addStat(statKey, i);
        }

        // 此时应该发生过下标切换
        currentSize = statReporter.getStatData().size();
        while (true) {
            if (statReporter.getStatData().size() != currentSize) {
                break;
            }
            Thread.sleep(100);
        }

        Assert.assertEquals("date2, " + new Date(), 0, statReporter.getOtherStatData().size());
        Assert.assertEquals(SofaTracerStatisticReporterManager.CLEAR_STAT_KEY_THRESHOLD,
            statReporter.getStatData().size());
    }

}