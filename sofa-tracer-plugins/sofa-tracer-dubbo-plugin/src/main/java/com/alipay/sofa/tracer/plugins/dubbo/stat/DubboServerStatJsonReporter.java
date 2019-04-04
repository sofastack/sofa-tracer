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
package com.alipay.sofa.tracer.plugins.dubbo.stat;

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.file.LoadTestAwareAppender;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.appender.self.Timestamp;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.reporter.stat.model.StatKey;
import com.alipay.common.tracer.core.reporter.stat.model.StatMapKey;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.TracerUtils;

import java.util.Map;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/2/26 4:23 PM
 * @since:
 **/
public class DubboServerStatJsonReporter extends AbstractSofaTracerStatisticReporter {

    /***
     * print builder
     */
    private static JsonStringBuilder jsonBuffer = new JsonStringBuilder();

    public DubboServerStatJsonReporter(String statTracerName, String rollingPolicy,
                                       String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    @Override
    public void doReportStat(SofaTracerSpan sofaTracerSpan) {
        //tags
        Map<String, String> tagsWithStr = sofaTracerSpan.getTagsWithStr();
        StatMapKey statKey = new StatMapKey();
        String fromApp = tagsWithStr.get(CommonSpanTags.REMOTE_APP);
        String toApp = tagsWithStr.get(CommonSpanTags.LOCAL_APP);
        //service name
        String serviceName = tagsWithStr.get(CommonSpanTags.SERVICE);
        //method name
        String methodName = tagsWithStr.get(CommonSpanTags.METHOD);
        statKey.setKey(buildString(new String[] { fromApp, toApp, serviceName, methodName }));
        String resultCode = tagsWithStr.get(CommonSpanTags.RESULT_CODE);
        statKey.setResult(resultCode.equals("00") ? "Y" : "N");
        statKey.setEnd(buildString(new String[] { getLoadTestMark(sofaTracerSpan) }));
        statKey.setLoadTest(TracerUtils.isLoadTest(sofaTracerSpan));
        statKey.addKey(CommonSpanTags.LOCAL_APP, fromApp);
        statKey.addKey(CommonSpanTags.REMOTE_APP, toApp);
        statKey.addKey(CommonSpanTags.SERVICE, serviceName);
        statKey.addKey(CommonSpanTags.METHOD, methodName);
        //次数和耗时，最后一个耗时是单独打印的字段
        long duration = sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime();
        long[] values = new long[] { 1, duration };
        this.addStat(statKey, values);
    }

    protected String getLoadTestMark(SofaTracerSpan span) {
        if (TracerUtils.isLoadTest(span)) {
            return "T";
        } else {
            return "F";
        }
    }

    @Override
    public void print(StatKey statKey, long[] values) {
        if (this.isClosePrint.get()) {
            //关闭统计日志输出
            return;
        }

        StatMapKey statMapKey = (StatMapKey) statKey;

        jsonBuffer.reset();
        jsonBuffer.appendBegin("time", Timestamp.currentTime());
        jsonBuffer.append("stat.key", this.statKeySplit(statMapKey));
        jsonBuffer.append("count", values[0]);
        jsonBuffer.append("total.cost.milliseconds", values[1]);
        jsonBuffer.append("success", statMapKey.getResult());
        jsonBuffer.appendEnd();
        try {
            if (appender instanceof LoadTestAwareAppender) {
                ((LoadTestAwareAppender) appender).append(jsonBuffer.toString(),
                    statKey.isLoadTest());
            } else {
                appender.append(jsonBuffer.toString());
            }
            // 这里强制刷一次
            appender.flush();
        } catch (Throwable t) {
            SelfLog.error("stat log <" + statTracerName + "> error!", t);
        }
    }

    private String statKeySplit(StatMapKey statKey) {
        JsonStringBuilder jsonBufferKey = new JsonStringBuilder();
        Map<String, String> keyMap = statKey.getKeyMap();
        jsonBufferKey.appendBegin();
        for (Map.Entry<String, String> entry : keyMap.entrySet()) {
            jsonBufferKey.append(entry.getKey(), entry.getValue());
        }
        jsonBufferKey.appendEnd(false);
        return jsonBufferKey.toString();
    }
}
