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
 * HttpClientDigestEncoder
 *
 * @author yangguanchao
 * @since 2018/08/07
 */
public class HttpClientDigestEncoder extends AbstractDigestSpanEncoder {

    @Override
    public String encode(SofaTracerSpan span) throws IOException {

        XStringBuilder xsb = new XStringBuilder();
        xsb.reset();
        //time
        xsb.append(Timestamp.format(span.getEndTime()));
        appendSlot(xsb, span);
        return xsb.toString();
    }

    private void appendSlot(XStringBuilder xsb, SofaTracerSpan sofaTracerSpan) {

        SofaTracerSpanContext context = sofaTracerSpan.getSofaTracerSpanContext();
        Map<String, String> tagWithStr = sofaTracerSpan.getTagsWithStr();
        Map<String, Number> tagWithNumber = sofaTracerSpan.getTagsWithNumber();
        //appName
        xsb.append(tagWithStr.get(CommonSpanTags.LOCAL_APP));
        //TraceId
        xsb.append(context.getTraceId());
        //RpcId
        xsb.append(context.getSpanId());
        //Request URL
        xsb.append(tagWithStr.get(CommonSpanTags.REQUEST_URL));
        //Request method
        xsb.append(tagWithStr.get(CommonSpanTags.METHOD));
        //Http status
        xsb.append(tagWithStr.get(CommonSpanTags.RESULT_CODE));
        Number requestSize = tagWithNumber.get(CommonSpanTags.REQ_SIZE);
        //Request Body bytes
        xsb.append((requestSize == null ? 0L : requestSize.longValue()) + SofaTracerConstant.BYTE);
        Number responseSize = tagWithNumber.get(CommonSpanTags.RESP_SIZE);
        //Response Body bytes
        xsb.append((responseSize == null ? 0L : responseSize.longValue()) + SofaTracerConstant.BYTE);
        //cost
        xsb.append((sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime())
                   + SofaTracerConstant.MS);
        //thread
        xsb.append(tagWithStr.get(CommonSpanTags.CURRENT_THREAD_NAME));
        //target appName
        xsb.append(tagWithStr.get(CommonSpanTags.REMOTE_APP));
        //sys baggage
        xsb.append(context.getSysBaggage());
        //baggage
        xsb.appendEnd(context.getBizBaggage());
    }

}