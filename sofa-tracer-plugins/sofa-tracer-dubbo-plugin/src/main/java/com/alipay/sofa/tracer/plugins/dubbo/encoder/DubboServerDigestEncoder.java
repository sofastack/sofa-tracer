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
import com.alipay.common.tracer.core.appender.self.Timestamp;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.tracer.plugins.dubbo.constants.AttachmentKeyConstants;
import io.opentracing.tag.Tags;

import java.io.IOException;
import java.util.Map;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/9/1 2:46 PM
 * @since:
 **/
public class DubboServerDigestEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SofaTracerSpan span) {
        Map<String, String> tagWithStr = span.getTagsWithStr();
        Map<String, Number> tagWithNum = span.getTagsWithNumber();

        //protocol
        xsb.append(tagWithStr.get(CommonSpanTags.PROTOCOL));
        // service
        xsb.append(tagWithStr.get(CommonSpanTags.SERVICE));
        // method
        xsb.append(tagWithStr.get(CommonSpanTags.METHOD));
        // invoke type
        xsb.append(tagWithStr.get(CommonSpanTags.INVOKE_TYPE));
        xsb.append(tagWithStr.get(CommonSpanTags.REMOTE_HOST));
        xsb.append(tagWithStr.get(CommonSpanTags.REMOTE_PORT));
        xsb.append(tagWithStr.get(CommonSpanTags.LOCAL_HOST));
        xsb.append(tagWithNum.get(AttachmentKeyConstants.CLIENT_SERIALIZE_TIME) + "");
        xsb.append(tagWithNum.get(AttachmentKeyConstants.CLIENT_DESERIALIZE_TIME) + "");
        //Request Body bytes length
        long serializeTime = getTime(tagWithNum.get(AttachmentKeyConstants.SERVER_SERIALIZE_TIME));
        long deserializeTime = getTime(tagWithNum
            .get(AttachmentKeyConstants.SERVER_DESERIALIZE_TIME));
        xsb.append(String.valueOf(serializeTime));
        xsb.append(String.valueOf(deserializeTime));
        Number reqSizeNum = tagWithNum.get(AttachmentKeyConstants.SERVER_DESERIALIZE_SIZE);
        xsb.append(reqSizeNum == null ? 0 : reqSizeNum.longValue());
        Number respSizeNum = tagWithNum.get(AttachmentKeyConstants.SERVER_SERIALIZE_SIZE);
        xsb.append(respSizeNum == null ? 0 : respSizeNum.longValue());

        xsb.append(StringUtils.isBlank(tagWithStr.get(Tags.ERROR.getKey())) ? "" : tagWithStr
            .get(Tags.ERROR.getKey()));
    }

    private long getTime(Number number) {
        if (number != null) {
            return number.longValue();
        }
        return 0;
    }

}
