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
package com.alipay.common.tracer.core.reporter.composite;

import com.alipay.common.tracer.core.reporter.facade.AbstractReporter;
import com.alipay.common.tracer.core.reporter.facade.Reporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SofaTracerCompositeDigestReporterImpl
 *
 * @author yangguanchao
 * @since 2017/06/25
 */
public class SofaTracerCompositeDigestReporterImpl extends AbstractReporter {

    private Map<String, Reporter> compositedReporters = new ConcurrentHashMap<String, Reporter>();

    public synchronized boolean addReporter(Reporter reporter) {
        if (reporter == null) {
            return false;
        }
        String reporterType = reporter.getReporterType();
        if (compositedReporters.containsKey(reporterType)) {
            return false;
        }
        this.compositedReporters.put(reporterType, reporter);
        return true;
    }

    @Override
    public String getReporterType() {
        return COMPOSITE_REPORTER;
    }

    @Override
    public void doReport(SofaTracerSpan span) {
        for (Map.Entry<String, Reporter> entry : this.compositedReporters.entrySet()) {
            Reporter reporter = entry.getValue();
            reporter.report(span);
        }
    }
}
