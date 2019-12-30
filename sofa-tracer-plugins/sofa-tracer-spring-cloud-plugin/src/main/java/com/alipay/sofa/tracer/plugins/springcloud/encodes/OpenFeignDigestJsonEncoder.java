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
package com.alipay.sofa.tracer.plugins.springcloud.encodes;

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import io.opentracing.tag.Tags;

import java.util.Map;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 3:30 PM
 * @since:
 **/
public class OpenFeignDigestJsonEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SofaTracerSpan span) {

        Map<String, String> tagWithStr = span.getTagsWithStr();
        Map<String, Number> tagWithNum = span.getTagsWithNumber();

        jsb.append(CommonSpanTags.REQUEST_URL, tagWithStr.get(CommonSpanTags.REQUEST_URL));
        jsb.append(CommonSpanTags.METHOD, tagWithStr.get(CommonSpanTags.METHOD));

        if (StringUtils.isNotBlank(tagWithStr.get(Tags.ERROR.getKey()))) {
            jsb.append(Tags.ERROR.getKey(), tagWithStr.get(Tags.ERROR.getKey()));
        } else {
            jsb.append(Tags.ERROR.getKey(), StringUtils.EMPTY_STRING);
        }

        Number requestSize = tagWithNum.get(CommonSpanTags.REQ_SIZE);
        jsb.append(CommonSpanTags.REQ_SIZE, (requestSize == null ? 0L : requestSize.longValue()));
        Number responseSize = tagWithNum.get(CommonSpanTags.RESP_SIZE);
        jsb.append(CommonSpanTags.RESP_SIZE, (responseSize == null ? 0L : responseSize.longValue()));

        //target appName
        jsb.append(CommonSpanTags.REMOTE_HOST, tagWithStr.get(CommonSpanTags.REMOTE_HOST));
        jsb.append(CommonSpanTags.REMOTE_PORT, tagWithStr.get(CommonSpanTags.REMOTE_PORT));
    }
}
