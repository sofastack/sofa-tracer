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

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.util.Map;

/**
 * SpringMvcDigestEncoder
 *
 * @author yangguanchao
 * @since 2018/04/30
 */
public class SpringMvcDigestEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SofaTracerSpan span) {
        Map<String, String> tagWithStr = span.getTagsWithStr();
        Map<String, Number> tagWithNum = span.getTagsWithNumber();
        //URL
        xsb.append(tagWithStr.get(CommonSpanTags.REQUEST_URL));
        //method
        xsb.append(tagWithStr.get(CommonSpanTags.METHOD));
        // requestSize
        Number requestSize = tagWithNum.get(CommonSpanTags.REQ_SIZE);
        //Request Body bytes
        xsb.append((requestSize == null ? 0L : requestSize.longValue()) + SofaTracerConstant.BYTE);
        // responseSize
        Number responseSize = tagWithNum.get(CommonSpanTags.RESP_SIZE);
        //Response Body bytes
        xsb.append((responseSize == null ? 0L : responseSize.longValue()) + SofaTracerConstant.BYTE);
    }
}