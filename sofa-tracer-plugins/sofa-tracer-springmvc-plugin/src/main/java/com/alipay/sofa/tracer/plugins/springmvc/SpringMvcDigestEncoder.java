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
package com.alipay.sofa.tracer.plugins.springmvc;

import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.appender.self.Timestamp;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.io.IOException;
import java.util.Map;

/**
 * SpringMvcDigestEncoder
 *
 * @author yangguanchao
 * @since 2018/04/30
 */
public class SpringMvcDigestEncoder extends AbstractDigestSpanEncoder {

    @Override
    public String encode(SofaTracerSpan span) throws IOException {

        XStringBuilder xsb = new XStringBuilder();
        xsb.reset();
        //日志打印时间
        xsb.append(Timestamp.format(span.getEndTime()));
        appendSlot(xsb, span);
        return xsb.toString();
    }

    private void appendSlot(XStringBuilder xsb, SofaTracerSpan sofaTracerSpan) {

        SofaTracerSpanContext context = sofaTracerSpan.getSofaTracerSpanContext();
        Map<String, String> tagWithStr = sofaTracerSpan.getTagsWithStr();
        Map<String, Number> tagWithNumber = sofaTracerSpan.getTagsWithNumber();
        //当前应用名
        xsb.append(tagWithStr.get(CommonSpanTags.LOCAL_APP));
        //TraceId
        xsb.append(context.getTraceId());
        //RpcId
        xsb.append(context.getSpanId());
        //请求 URL
        xsb.append(tagWithStr.get(CommonSpanTags.REQUEST_URL));
        //请求方法
        xsb.append(tagWithStr.get(CommonSpanTags.METHOD));
        //Http 状态码
        xsb.append(tagWithStr.get(CommonSpanTags.RESULT_CODE));
        Number requestSize = tagWithNumber.get(CommonSpanTags.REQ_SIZE);
        //Request Body 大小 单位为byte
        xsb.append((requestSize == null ? 0L : requestSize.longValue()) + SofaTracerConstant.BYTE);
        Number responseSize = tagWithNumber.get(CommonSpanTags.RESP_SIZE);
        //Response Body 大小，单位为byte
        xsb.append((responseSize == null ? 0L : responseSize.longValue()) + SofaTracerConstant.BYTE);
        //请求耗时（MS）
        xsb.append((sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime())
                   + SofaTracerConstant.MS);
        xsb.append(tagWithStr.get(CommonSpanTags.CURRENT_THREAD_NAME));
        //穿透数据放在最后
        xsb.appendEnd(baggageSerialized(context));
    }

}