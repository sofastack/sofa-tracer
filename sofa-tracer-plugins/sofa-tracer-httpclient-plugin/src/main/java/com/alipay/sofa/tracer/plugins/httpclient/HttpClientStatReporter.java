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
package com.alipay.sofa.tracer.plugins.httpclient;

import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.reporter.stat.model.StatKey;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.TracerUtils;

import java.util.Map;

/**
 * HttpClientStatReporter
 *
 * @author yangguanchao
 * @since 2018/08/07
 */
public class HttpClientStatReporter extends AbstractSofaTracerStatisticReporter {

    public HttpClientStatReporter(String statTracerName, String rollingPolicy,
                                  String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    @Override
    public void doReportStat(SofaTracerSpan sofaTracerSpan) {
        Map<String, String> tagsWithStr = sofaTracerSpan.getTagsWithStr();
        StatKey statKey = new StatKey();
        statKey
            .setKey(buildString(new String[] { tagsWithStr.get(CommonSpanTags.LOCAL_APP),
                    tagsWithStr.get(CommonSpanTags.REQUEST_URL),
                    tagsWithStr.get(CommonSpanTags.METHOD) }));
        String resultCode = tagsWithStr.get(CommonSpanTags.RESULT_CODE);
        boolean success = (resultCode != null && resultCode.length() > 0 && (resultCode.charAt(0) == '1'
                                                                             || resultCode
                                                                                 .charAt(0) == '2' || resultCode
            .trim().equals("302")));
        statKey.setResult(success ? "Y" : "N");
        statKey.setEnd(buildString(new String[] { TracerUtils.getLoadTestMark(sofaTracerSpan) }));
        //pressure mark
        statKey.setLoadTest(TracerUtils.isLoadTest(sofaTracerSpan));

        //count and cost
        long duration = sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime();
        long values[] = new long[] { 1, duration };
        this.addStat(statKey, values);
    }
}