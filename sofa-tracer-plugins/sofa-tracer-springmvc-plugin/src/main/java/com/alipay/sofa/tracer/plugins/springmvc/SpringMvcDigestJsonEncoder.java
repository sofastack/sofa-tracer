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
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import io.opentracing.tag.Tags;

import java.util.Map;

/**
 * SpringMvcDigestEncoder
 *
 * @author yangguanchao
 * @since 2018/05/14
 */
public class SpringMvcDigestJsonEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SofaTracerSpan span) {

        Map<String, String> tagWithStr = span.getTagsWithStr();
        Map<String, Number> tagWithNum = span.getTagsWithNumber();
        //Request URL
        jsb.append(CommonSpanTags.REQUEST_URL, tagWithStr.get(CommonSpanTags.REQUEST_URL));
        //Request method
        jsb.append(CommonSpanTags.METHOD, tagWithStr.get(CommonSpanTags.METHOD));
        Number requestSize = tagWithNum.get(CommonSpanTags.REQ_SIZE);
        //Request Body Size (byte)
        jsb.append(CommonSpanTags.REQ_SIZE, (requestSize == null ? 0L : requestSize.longValue()));
        Number responseSize = tagWithNum.get(CommonSpanTags.RESP_SIZE);
        //Response Body Size，(byte)
        jsb.append(CommonSpanTags.RESP_SIZE, (responseSize == null ? 0L : responseSize.longValue()));
        //error flag and error msg.
        if (StringUtils.isNotBlank(tagWithStr.get(Tags.ERROR.getKey()))) {
            jsb.append(Tags.ERROR.getKey(), tagWithStr.get(Tags.ERROR.getKey()));
        } else {
            jsb.append(Tags.ERROR.getKey(), StringUtils.EMPTY_STRING);
        }
    }
}