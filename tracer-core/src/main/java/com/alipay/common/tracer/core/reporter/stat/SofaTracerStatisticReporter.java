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

import com.alipay.common.tracer.core.reporter.stat.model.StatKey;
import com.alipay.common.tracer.core.reporter.stat.model.StatValues;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.util.Map;

/**
 * SofaTracerStatisticReporter
 * <p>
 * 参考: {com.alipay.common.tracer.tracer.StatTracer}
 *
 * @author yangguanchao
 * @since 2017/06/26
 */
public interface SofaTracerStatisticReporter {

    /***
     * 获取周期时间,即多长时间调度一次
     * @return 一次统计数据的周期时间
     */
    long getPeriodTime();

    /***
     * 获取统计类型额唯一标示
     * @return 统计名称(全局唯一标识)
     */
    String getStatTracerName();

    /***
     * 向槽中更新数据 前面是唯一的key，后面是数值列 统计计算会对不同key的数值列进行加和
     *
     * @param sofaTracerSpan span 上下文
     */
    void reportStat(SofaTracerSpan sofaTracerSpan);

    /***
     * 切换当前下标并返回切换前的统计数据
     * @return 当前时间段内的统计数据
     */
    Map<StatKey, StatValues> shiftCurrentIndex();

    /**
     * 当该方法被调用，说明已经过去了一个周期，要判断是否已经过去了足够的周期，是否需要flush
     *
     * @return true 统计数据可以打印,框架会调用 {@link SofaTracerStatisticReporter#print}
     */
    boolean shouldPrintNow();

    /***
     * 打印,即可以打印到本地磁盘,也可以上报到远程服务器
     * @param statKey 统计关键字
     * @param values 要打印的值
     */
    void print(StatKey statKey, long[] values);

    /***
     * 关闭打印能力
     */
    void close();
}
