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
package com.alipay.sofa.tracer.plugins.rocketmq.repoters;

import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.reporter.stat.model.StatKey;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.TracerUtils;

import java.util.Map;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/9/1 5:40 PM
 * @since:
 **/
public class RocketMQConsumeStatReporter extends AbstractSofaTracerStatisticReporter {

    public RocketMQConsumeStatReporter(String statTracerName, String rollingPolicy,
                                       String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    @Override
    public void doReportStat(SofaTracerSpan sofaTracerSpan) {
        Map<String, String> tagsWithStr = sofaTracerSpan.getTagsWithStr();
        StatKey statKey = new StatKey();
        String localApp = tagsWithStr.get(CommonSpanTags.LOCAL_APP);
        String operationName = sofaTracerSpan.getOperationName();
        String topic = tagsWithStr.get(CommonSpanTags.MSG_TOPIC);
        String consumerGroup = tagsWithStr.get("consumerGroup");
        //method name
        statKey.setKey(buildString(new String[] { localApp, operationName, consumerGroup, topic }));
        //success
        String resultCode = tagsWithStr.get(CommonSpanTags.RESULT_CODE);
        boolean success = isWebHttpClientSuccess(resultCode);
        statKey.setResult(success ? SofaTracerConstant.STAT_FLAG_SUCCESS
            : SofaTracerConstant.STAT_FLAG_FAILS);
        statKey.setEnd(buildString(new String[] { TracerUtils.getLoadTestMark(sofaTracerSpan) }));
        //pressure mark
        statKey.setLoadTest(TracerUtils.isLoadTest(sofaTracerSpan));
        //value the count and duration
        long duration = sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime();
        long[] values = new long[] { 1, duration };
        //reserve
        this.addStat(statKey, values);
    }
}
