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
package com.alipay.common.tracer.core.appender.manager;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.io.File;

/**
 *
 * @author liangen
 * @version $Id: ManagerTestUtil.java, v 0.1 2017年10月23日 下午8:34 liangen Exp $
 */
public class ManagerTestUtil {
    public static void deleteFile(String filePath) {

        File fileRoot = new File(filePath);

        if (fileRoot.isDirectory()) {
            String[] childrens = fileRoot.list();
            for (String children : childrens) {
                File file = new File(fileRoot, children);
                deleteFile(file.getPath());
            }

        }

        fileRoot.delete();
    }

    public static SofaTracerSpan createSofaTracerSpan(int sequence) {

        SofaTracerSpanContext spanContext = new SofaTracerSpanContext("traceID" + sequence,
            "spanId", "parentId", false);
        SofaTracerSpan span = new SofaTracerSpan(new SofaTracer.Builder("tracerType").build(),
            System.currentTimeMillis(), "callServiceName", spanContext, null);
        span.setLogType("logType" + sequence);

        return span;
    }
}