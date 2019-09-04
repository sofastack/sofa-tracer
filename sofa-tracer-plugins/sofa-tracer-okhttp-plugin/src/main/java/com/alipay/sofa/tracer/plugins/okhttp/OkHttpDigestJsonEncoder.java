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
package com.alipay.sofa.tracer.plugins.okhttp;

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.util.Map;

/**
 *
 * @author xianglong.chen
 * @since 2019/1/17 13:28
 */
public class OkHttpDigestJsonEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SofaTracerSpan span) {

        Map<String, String> tagWithStr = span.getTagsWithStr();
        Map<String, Number> tagWithNum = span.getTagsWithNumber();
        //URL
        jsb.append(CommonSpanTags.REQUEST_URL, tagWithStr.get(CommonSpanTags.REQUEST_URL));
        //POST/GET
        jsb.append(CommonSpanTags.METHOD, tagWithStr.get(CommonSpanTags.METHOD));
        //Http status code
        jsb.append(CommonSpanTags.RESULT_CODE, tagWithStr.get(CommonSpanTags.RESULT_CODE));
        Number requestSize = tagWithNum.get(CommonSpanTags.REQ_SIZE);
        //Request Body bytes length
        jsb.append(CommonSpanTags.REQ_SIZE, (requestSize == null ? 0L : requestSize.longValue()));
        Number responseSize = tagWithNum.get(CommonSpanTags.RESP_SIZE);
        //Response Body bytes length
        jsb.append(CommonSpanTags.RESP_SIZE, (responseSize == null ? 0L : responseSize.longValue()));
        jsb.append(CommonSpanTags.REMOTE_APP, tagWithStr.get(CommonSpanTags.REMOTE_APP));
    }

}