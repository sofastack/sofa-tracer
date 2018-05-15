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
package com.alipay.common.tracer.core.reporter.facade;

import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AbstractDiskReporter
 *
 * 持久化抽象类,摘要持久化和统计持久化
 * @author yangguanchao
 * @since 2017/07/14
 */
public abstract class AbstractReporter implements Reporter {

    /***
     * 是否关闭摘要日志打印,默认不关闭,关闭意味着关闭摘要也关闭统计
     */
    private AtomicBoolean isClosePrint = new AtomicBoolean(false);

    /***
     * 输出 span
     * @param span 要被输出的 span
     */
    @Override
    public void report(SofaTracerSpan span) {
        if (span == null) {
            return;
        }
        //关闭所有日志打印:关闭摘要和统计
        if (isClosePrint.get()) {
            return;
        }
        this.doReport(span);
    }

    /***
     * 抽象方法具体输出方式落磁盘还会远程上报需要子类实现
     *
     * @param span 要被输出的 span
     */
    public abstract void doReport(SofaTracerSpan span);

    @Override
    public void close() {
        isClosePrint.set(true);
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
}
