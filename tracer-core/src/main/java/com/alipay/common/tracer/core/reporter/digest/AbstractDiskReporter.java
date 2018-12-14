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
package com.alipay.common.tracer.core.reporter.digest;

import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.reporter.facade.AbstractReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;

import java.util.Map;

/**
 * AbstractDiskReporter
 *
 * 持久化抽象类,摘要持久化和统计持久化
 * @author yangguanchao
 * @since 2017/07/14
 */
public abstract class AbstractDiskReporter extends AbstractReporter {

    /***
     * 获取 Reporter 实例类型
     * @return 类型
     */
    @Override
    public String getReporterType() {
        //默认用摘要日志的类型作为 span 的类型
        return this.getDigestReporterType();
    }

    /***
     * 输出 span
     * @param span 要被输出的 span
     */
    @Override
    public void doReport(SofaTracerSpan span) {
        //设置日志类型,方便打印，否则无法正确打印
        span.setLogType(this.getDigestReporterType());
        if (!isDisableDigestLog(span)) {
            //打印摘要日志
            this.digestReport(span);
        }
        //统计日志默认是不关闭的
        this.statisticReport(span);
    }

    /***
     * 获取摘要 Reporter 实例类型
     * @return 类型
     */
    public abstract String getDigestReporterType();

    /***
     * 获取统计 Reporter 实例类型
     * @return 类型
     */
    public abstract String getStatReporterType();

    /***
     * 打印摘要日志
     * @param span 被打印 span
     */
    public abstract void digestReport(SofaTracerSpan span);

    /***
     * 打印统计日志
     * @param span 被统计 span
     */
    public abstract void statisticReport(SofaTracerSpan span);

    protected boolean isDisableDigestLog(SofaTracerSpan span) {
        if (span == null || span.context() == null) {
            return true;
        }
        SofaTracerSpanContext sofaTracerSpanContext = (SofaTracerSpanContext) span.context();
        // sampled is false; this span will not be report
        if (!sofaTracerSpanContext.isSampled()) {
            return true;
        }
        boolean allDisabled = Boolean.TRUE.toString().equalsIgnoreCase(
            SofaTracerConfiguration
                .getProperty(SofaTracerConfiguration.DISABLE_MIDDLEWARE_DIGEST_LOG_KEY));

        if (allDisabled) {
            return true;
        }

        Map<String, String> disableConfiguration = SofaTracerConfiguration
            .getMapEmptyIfNull(SofaTracerConfiguration.DISABLE_DIGEST_LOG_KEY);
        //摘要日志类型
        String logType = StringUtils.EMPTY_STRING + span.getLogType();
        if (StringUtils.isBlank(logType)) {
            //摘要日志类型为空,就不打印了
            return true;
        }
        // rpc-2-jvm特殊处理, 适配rpc2jvm中关闭digest而只打印stat的情况
        if (SofaTracerConstant.RPC_2_JVM_DIGEST_LOG_NAME.equals(logType)) {
            if (Boolean.FALSE.toString().equalsIgnoreCase(
                SofaTracerConfiguration.getProperty("enable_rpc_2_jvm_digest_log"))) {
                return true;
            }
        }
        return Boolean.TRUE.toString().equalsIgnoreCase(disableConfiguration.get(logType));
    }

}
