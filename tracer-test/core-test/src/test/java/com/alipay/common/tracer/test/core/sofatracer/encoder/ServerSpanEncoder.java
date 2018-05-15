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
package com.alipay.common.tracer.test.core.sofatracer.encoder;

import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

/**
 * ClientSpanEncoder
 *
 * @author yangguanchao
 * @since 2017/07/01
 */
public class ServerSpanEncoder extends ClientSpanEncoder implements SpanEncoder<SofaTracerSpan> {

    XStringBuilder xsb = new XStringBuilder();

    //测试用 客户端 和 服务端 一起测试

    //    @Override
    //    public void encode(SofaTracerSpan span, TraceAppender appender) throws IOException {
    //        SofaTracerSpanContext spanContext = span.getSofaTracerSpanContext();
    //        xsb.reset();
    //
    //        xsb.append(Timestamp.format(span.getStartTime())).append(span.getTagsWithStr())
    //            .append(span.getTagsWithBool().toString()).append(span.getTagsWithNumber().toString())
    //            .appendEnd(spanContext.getBizBaggage());
    //
    //        if (appender instanceof LoadTestAwareAppender) {
    //            ((LoadTestAwareAppender) appender).append(xsb.toString(), TracerUtils.isLoadTest(span));
    //        } else {
    //            appender.append(xsb.toString());
    //        }
    //    }
}
