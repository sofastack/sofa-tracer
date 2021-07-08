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
package com.alipay.sofa.tracer.plugins.skywalking.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * Span
 * @author zhaochen
 */
@Getter
@Setter
public class Span {
    //so the detail of span :
    //https://github.com/apache/skywalking-data-collect-protocol/blob/e626ee04850703c220f64b642d2893fa65572943/language-agent/Tracing.proto
    private int                      spanId;
    private int                      parentSpanId;
    private Long                     startTime;
    private Long                     endTime;
    private List<SegmentReference>   refs = new LinkedList<>();
    private String                   operationName;
    private String                   peer;
    private SpanType                 spanType;
    // Span layer represent the component tech stack, related to the network tech.
    private SpanLayer                spanLayer;
    private int                      componentId;
    @JSONField(name = "isError")
    private boolean                  isError;
    private List<KeyStringValuePair> tags = new LinkedList<>();
    private List<Log>                logs = new LinkedList<>();
    private boolean                  skipAnalysis;

    public void addSegmentReference(SegmentReference segmentReference) {
        refs.add(segmentReference);
    }

    public void addTag(String key, String value) {
        tags.add(new KeyStringValuePair(key, value));
    }

    public void addLog(Log log) {
        logs.add(log);
    }

}
