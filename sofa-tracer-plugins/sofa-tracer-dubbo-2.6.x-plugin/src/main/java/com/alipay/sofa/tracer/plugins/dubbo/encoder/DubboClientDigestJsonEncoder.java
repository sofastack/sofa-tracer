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
package com.alipay.sofa.tracer.plugins.dubbo.encoder;

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.dubbo.constants.AttachmentKeyConstants;
import io.opentracing.tag.Tags;

import java.util.Map;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/2/26 3:56 PM
 * @since:
 **/
public class DubboClientDigestJsonEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SofaTracerSpan span) {
        Map<String, String> tagStr = span.getTagsWithStr();
        Map<String, Number> tagNum = span.getTagsWithNumber();

        // protocol
        jsb.append(CommonSpanTags.PROTOCOL, tagStr.get(CommonSpanTags.PROTOCOL));
        // serviceName
        jsb.append(CommonSpanTags.SERVICE, tagStr.get(CommonSpanTags.SERVICE));
        // method
        jsb.append(CommonSpanTags.METHOD, tagStr.get(CommonSpanTags.METHOD));
        //invoke type
        jsb.append(CommonSpanTags.INVOKE_TYPE, tagStr.get(CommonSpanTags.INVOKE_TYPE));
        //target ip
        jsb.append(CommonSpanTags.REMOTE_HOST, tagStr.get(CommonSpanTags.REMOTE_HOST));
        //target port
        jsb.append(CommonSpanTags.REMOTE_PORT, tagStr.get(CommonSpanTags.REMOTE_PORT));
        //local ip
        jsb.append(CommonSpanTags.LOCAL_HOST, tagStr.get(CommonSpanTags.LOCAL_HOST));
        //request serialize time
        jsb.append(CommonSpanTags.CLIENT_SERIALIZE_TIME,
            tagNum.get(AttachmentKeyConstants.CLIENT_SERIALIZE_TIME));
        //response deserialize time
        jsb.append(CommonSpanTags.CLIENT_DESERIALIZE_TIME,
            tagNum.get(AttachmentKeyConstants.CLIENT_DESERIALIZE_TIME));
        //Request Body bytes length
        Number reqSizeNum = tagNum.get(AttachmentKeyConstants.CLIENT_SERIALIZE_SIZE);
        jsb.append(CommonSpanTags.REQ_SIZE, reqSizeNum == null ? 0 : reqSizeNum.longValue());
        //Response Body bytes length
        Number respSizeNum = tagNum.get(AttachmentKeyConstants.CLIENT_DESERIALIZE_SIZE);
        jsb.append(CommonSpanTags.RESP_SIZE, respSizeNum == null ? 0 : respSizeNum.longValue());

        //error message
        if (StringUtils.isNotBlank(tagStr.get(Tags.ERROR.getKey()))) {
            jsb.append(Tags.ERROR.getKey(), tagStr.get(Tags.ERROR.getKey()));
        } else {
            jsb.append(Tags.ERROR.getKey(), StringUtils.EMPTY_STRING);
        }
    }
}
