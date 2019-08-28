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
package com.alipay.sofa.tracer.plugins.springcloud.repoters;

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.file.LoadTestAwareAppender;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.appender.self.Timestamp;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.reporter.stat.model.StatKey;
import com.alipay.common.tracer.core.reporter.stat.model.StatMapKey;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.TracerUtils;

import java.util.Map;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 3:31 PM
 * @since:
 **/
public class OpenFeignStatJsonReporter extends AbstractSofaTracerStatisticReporter {

    public OpenFeignStatJsonReporter(String statTracerName, String rollingPolicy,
                                     String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    @Override
    public void doReportStat(SofaTracerSpan sofaTracerSpan) {
        Map<String, String> tagsWithStr = sofaTracerSpan.getTagsWithStr();
        StatMapKey statKey = new StatMapKey();
        statKey.addKey(CommonSpanTags.LOCAL_APP, tagsWithStr.get(CommonSpanTags.LOCAL_APP));
        statKey.addKey(CommonSpanTags.REQUEST_URL, tagsWithStr.get(CommonSpanTags.REQUEST_URL));
        statKey.addKey(CommonSpanTags.METHOD, tagsWithStr.get(CommonSpanTags.METHOD));
        String resultCode = tagsWithStr.get(CommonSpanTags.RESULT_CODE);
        boolean success = (resultCode != null && resultCode.length() > 0 && this
            .isHttpOrMvcSuccess(resultCode));
        statKey.setResult(success ? SofaTracerConstant.STAT_FLAG_SUCCESS
            : SofaTracerConstant.STAT_FLAG_FAILS);
        //pressure mark
        statKey.setLoadTest(TracerUtils.isLoadTest(sofaTracerSpan));
        //end
        statKey.setEnd(TracerUtils.getLoadTestMark(sofaTracerSpan));
        //value the count and duration
        long duration = sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime();
        long[] values = new long[] { 1, duration };
        //reserve
        this.addStat(statKey, values);
    }

    @Override
    public void print(StatKey statKey, long[] values) {

        JsonStringBuilder buffer = new JsonStringBuilder();

        if (this.isClosePrint.get()) {
            return;
        }
        if (!(statKey instanceof StatMapKey)) {
            return;
        }
        StatMapKey statMapKey = (StatMapKey) statKey;
        try {
            buffer.reset();
            buffer.appendBegin();
            buffer.append(CommonSpanTags.TIME, Timestamp.currentTime());
            buffer.append(CommonSpanTags.STAT_KEY, this.statKeySplit(statMapKey));
            buffer.append(CommonSpanTags.COUNT, values[0]);
            buffer.append(CommonSpanTags.TOTAL_COST_MILLISECONDS, values[1]);
            buffer.append(CommonSpanTags.SUCCESS, statMapKey.getResult());
            //pressure
            buffer.appendEnd(CommonSpanTags.LOAD_TEST, statMapKey.getEnd());
            if (appender instanceof LoadTestAwareAppender) {
                ((LoadTestAwareAppender) appender).append(buffer.toString(),
                    statMapKey.isLoadTest());
            } else {
                appender.append(buffer.toString());
            }
            // force print
            appender.flush();
        } catch (Throwable t) {
            SelfLog.error("Stat log <" + statTracerName + "> error!", t);
        }
    }

    private String statKeySplit(StatMapKey statKey) {
        JsonStringBuilder bufferKey = new JsonStringBuilder();
        Map<String, String> keyMap = statKey.getKeyMap();
        bufferKey.appendBegin();
        for (Map.Entry<String, String> entry : keyMap.entrySet()) {
            bufferKey.append(entry.getKey(), entry.getValue());
        }
        bufferKey.appendEnd(false);
        return bufferKey.toString();
    }
}
