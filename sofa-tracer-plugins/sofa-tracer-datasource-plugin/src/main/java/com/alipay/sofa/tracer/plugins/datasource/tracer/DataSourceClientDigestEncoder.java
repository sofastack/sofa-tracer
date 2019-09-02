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
package com.alipay.sofa.tracer.plugins.datasource.tracer;

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.util.Map;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/8/31 4:07 PM
 * @since:
 **/
public class DataSourceClientDigestEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SofaTracerSpan span) {
        Map<String, String> tagWithStr = span.getTagsWithStr();
        Map<String, Number> tagWithNum = span.getTagsWithNumber();
        //dataBase Name
        xsb.append(tagWithStr.get(DataSourceTracerKeys.DATABASE_NAME));
        //SQL
        xsb.appendEscape(tagWithStr.get(DataSourceTracerKeys.SQL));
        //db connection established cost time
        xsb.append(tagWithNum.get(DataSourceTracerKeys.CONNECTION_ESTABLISH_COST)
                   + SofaTracerConstant.MS);
        //db cost time
        xsb.append(tagWithNum.get(DataSourceTracerKeys.DB_EXECUTE_COST) + SofaTracerConstant.MS);
        // db type
        xsb.append(tagWithStr.get(DataSourceTracerKeys.DATABASE_TYPE));
        // connect url
        xsb.append(tagWithStr.get(DataSourceTracerKeys.DATABASE_ENDPOINT));
    }
}
