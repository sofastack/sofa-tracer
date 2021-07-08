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

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * Segment
 * @author zhaochen
 */
@Getter
@Setter
public class Segment {
    private String     traceId;
    private String     traceSegmentId;
    private List<Span> spans = new LinkedList<>();
    // each service name is displayed as a node in the topology view.
    // Indicators obtained from the SPAN under the same service name are aggregated as indicators of the service
    private String     service;
    private String     serviceInstance;
    // Whether the segment includes all tracked spans.
    // In the production environment tracked, some tasks could include too many spans for one request context, such as a batch update for a cache, or an async job.
    // The agent/SDK could optimize or ignore some tracked spans for better performance.
    // In this case, the value should be flagged as TRUE.
    private boolean    isSizeLimited;

    public void addSpan(Span span) {
        spans.add(span);
    }

}
