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
package com.alipay.common.tracer.core.reporter.common;

import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.appender.self.Timestamp;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.span.CommonLogSpan;
import com.alipay.common.tracer.core.tags.SpanTags;
import com.alipay.common.tracer.core.utils.StringUtils;

import java.io.IOException;

/**
 * CommonSpanEncoder
 * <p>
 * 客户端错误和服务端错误均打印在同一个文件中
 *
 * 注意是无状态实例:多个日志打印共用的一个实例
 *
 * @author yangguanchao
 * @since 2017/06/27
 */
public class CommonSpanEncoder implements SpanEncoder<CommonLogSpan> {

    @Override
    public String encode(CommonLogSpan commonLogSpan) throws IOException {
        if (commonLogSpan.getSofaTracerSpanContext() == null) {
            return StringUtils.EMPTY_STRING;
        }
        SofaTracerSpanContext spanContext = commonLogSpan.getSofaTracerSpanContext();
        XStringBuilder xsb = new XStringBuilder();
        //报告开始的时间作为打印的时间,不存在完成时间
        xsb.append(Timestamp.format(commonLogSpan.getStartTime()))
            //保证构造common也携带过来
            .append(commonLogSpan.getTagsWithStr().get(SpanTags.CURR_APP_TAG.getKey()))
            .append(spanContext.getTraceId()).append(spanContext.getSpanId());

        this.appendStr(xsb, commonLogSpan);
        return xsb.toString();
    }

    private void appendStr(XStringBuilder xsb, CommonLogSpan commonLogSpan) {
        int slotSize = commonLogSpan.getSlots().size();

        for (int i = 0; i < slotSize; i++) {
            if (i + 1 != slotSize) {
                xsb.append(commonLogSpan.getSlots().get(i));
            } else {
                xsb.appendRaw(commonLogSpan.getSlots().get(i));
            }
        }
        xsb.appendRaw(StringUtils.NEWLINE);
    }
}
