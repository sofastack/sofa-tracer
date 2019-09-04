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
package com.alipay.sofa.tracer.plugin.flexible;

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import io.opentracing.tag.Tags;

import java.util.Map;
import java.util.Set;

/**
 * FlexibleDigestJsonEncoder for flexible biz tracer
 *
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/8/2 11:39 AM
 * @since:
 **/
public class FlexibleDigestJsonEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb, SofaTracerSpan span) {
        Map<String, String> strTags = span.getTagsWithStr();
        Map<String, Number> numTags = span.getTagsWithNumber();
        Map<String, Number> boolTags = span.getTagsWithNumber();
        //POST/GET
        jsb.append(CommonSpanTags.METHOD, strTags.get(CommonSpanTags.METHOD));

        Set<String> strKeys = strTags.keySet();
        strKeys.forEach(key->{
            if (!isFlexible(key)){
                jsb.append(key,strTags.get(key));
            }
        });
        Set<String> numKeys = numTags.keySet();
        numKeys.forEach(key->{
            if (!isFlexible(key)){
                jsb.append(key,numTags.get(key));
            }
        });
        Set<String> boolKeys = boolTags.keySet();
        boolKeys.forEach(key->{
            if (!isFlexible(key)){
                jsb.append(key,boolTags.get(key));
            }
        });
    }

    /**
     * common tag and component tag excluded
     * @param key
     * @return
     */
    protected boolean isFlexible(String key) {
        return CommonSpanTags.LOCAL_APP.equalsIgnoreCase(key)
               || CommonSpanTags.TRACE_ID.equalsIgnoreCase(key)
               || CommonSpanTags.SPAN_ID.equalsIgnoreCase(key)
               || CommonSpanTags.CURRENT_THREAD_NAME.equalsIgnoreCase(key)
               || CommonSpanTags.METHOD.equalsIgnoreCase(key)
               || CommonSpanTags.TIME.equalsIgnoreCase(key)
               || CommonSpanTags.TIME_COST_MILLISECONDS.equalsIgnoreCase(key)
               || Tags.SPAN_KIND.getKey().equalsIgnoreCase(key);
    }
}