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
package com.alipay.sofa.tracer.plugins.mongodb.encodes;

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import io.opentracing.tag.Tags;

import java.util.Map;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/9/1 3:52 PM
 * @since:
 **/
public class MongoDigestEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SofaTracerSpan span) {
        Map<String, String> strTags = span.getTagsWithStr();
        Map<String, Number> numTags = span.getTagsWithNumber();
        xsb.append(strTags.get(Tags.DB_STATEMENT.getKey()));
        xsb.append(strTags.get(Tags.DB_INSTANCE.getKey()));
        xsb.append(strTags.get(Tags.PEER_HOSTNAME.getKey()));
        if (strTags.containsKey(CommonSpanTags.PEER_HOST)) {
            xsb.append(strTags.get(CommonSpanTags.PEER_HOST));
        }
        if (strTags.containsKey(Tags.ERROR.getKey())) {
            xsb.append(strTags.get(Tags.ERROR.getKey()));
        }
        xsb.append(String.valueOf(numTags.get(Tags.PEER_PORT.getKey())));
        xsb.append(strTags.get(Tags.DB_TYPE.getKey()));
    }
}
