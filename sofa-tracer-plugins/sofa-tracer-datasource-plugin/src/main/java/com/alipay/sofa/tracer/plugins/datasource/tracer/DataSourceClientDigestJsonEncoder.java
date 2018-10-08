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
package com.alipay.sofa.tracer.plugins.datasource.tracer;

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.self.Timestamp;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import java.io.IOException;
import java.util.Map;

/**
 * @author shusong.yss
 * @author qilong.zql
 * @since 2.2.0
 */
public class DataSourceClientDigestJsonEncoder extends AbstractDigestSpanEncoder {
    @Override
    public String encode(SofaTracerSpan span) throws IOException {
        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder();
        jsonStringBuilder.appendBegin("time", Timestamp.format(span.getEndTime()));
        appendSlot(jsonStringBuilder, span);
        return jsonStringBuilder.toString();
    }

    private void appendSlot(JsonStringBuilder jsonStringBuilder, SofaTracerSpan sofaTracerSpan) {
        SofaTracerSpanContext context = sofaTracerSpan.getSofaTracerSpanContext();
        Map<String, String> tagWithStr = sofaTracerSpan.getTagsWithStr();
        Map<String, Number> tagsWithLong = sofaTracerSpan.getTagsWithNumber();
        // app name
        jsonStringBuilder
            .append(CommonSpanTags.LOCAL_APP, tagWithStr.get(CommonSpanTags.LOCAL_APP));
        //TraceId
        jsonStringBuilder.append("traceId", context.getTraceId());
        //SpanId
        jsonStringBuilder.append("spanId", context.getSpanId());
        //schema
        jsonStringBuilder.append(DataSourceTracerKeys.DATABASE_NAME,
            tagWithStr.get(DataSourceTracerKeys.DATABASE_NAME));
        //sql
        jsonStringBuilder
            .append(DataSourceTracerKeys.SQL, tagWithStr.get(DataSourceTracerKeys.SQL));
        //result
        jsonStringBuilder.append(DataSourceTracerKeys.RESULT_CODE,
            tagWithStr.get(DataSourceTracerKeys.RESULT_CODE));
        //total cost time
        jsonStringBuilder.append(DataSourceTracerKeys.TOTAL_TIME, sofaTracerSpan.getEndTime()
                                                                  - sofaTracerSpan.getStartTime()
                                                                  + SofaTracerConstant.MS);
        //db connection established cost time
        jsonStringBuilder.append(DataSourceTracerKeys.CONNECTION_ESTABLISH_COST,
            tagsWithLong.get(DataSourceTracerKeys.CONNECTION_ESTABLISH_COST)
                    + SofaTracerConstant.MS);
        //db cost time
        jsonStringBuilder.append(DataSourceTracerKeys.DB_EXECUTE_COST,
            tagsWithLong.get(DataSourceTracerKeys.DB_EXECUTE_COST) + SofaTracerConstant.MS);
        //db type
        jsonStringBuilder.append(DataSourceTracerKeys.DATABASE_TYPE,
            tagWithStr.get(DataSourceTracerKeys.DATABASE_TYPE));
        //db connection(ip:port)
        jsonStringBuilder.append(DataSourceTracerKeys.DATABASE_ENDPOINT,
            tagWithStr.get(DataSourceTracerKeys.DATABASE_ENDPOINT));
        //thread name
        jsonStringBuilder.append(CommonSpanTags.CURRENT_THREAD_NAME,
            tagWithStr.get(CommonSpanTags.CURRENT_THREAD_NAME));
        this.appendBaggage(jsonStringBuilder, context);
    }

    private void appendBaggage(JsonStringBuilder jsonStringBuilder,
                               SofaTracerSpanContext sofaTracerSpanContext) {
        //baggage
        jsonStringBuilder.appendEnd("baggage", baggageSerialized(sofaTracerSpanContext));
    }
}