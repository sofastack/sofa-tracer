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
package com.sofa.alipay.tracer.plugins.kafkamq.encoders;

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.util.Map;

/**
 *  KafkaMQConsumeDigestEncoder.
 *
 * @author chenchen6 2020/8/23 15:34
 * @since 3.1.0
 */
public class KafkaMQConsumeDigestEncoder extends AbstractDigestSpanEncoder {
    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SofaTracerSpan span) {
        Map<String, String> tagWithStr = span.getTagsWithStr();
        Map<String, Number> tagsWithNumber = span.getTagsWithNumber();
        xsb.append(tagWithStr.get(CommonSpanTags.KAFKA_TOPIC));
        xsb.append((Integer) tagsWithNumber.get(CommonSpanTags.KAFKA_PARTITION));
        xsb.append((Long) tagsWithNumber.get(CommonSpanTags.KAFKA_OFFSET));
    }
}
