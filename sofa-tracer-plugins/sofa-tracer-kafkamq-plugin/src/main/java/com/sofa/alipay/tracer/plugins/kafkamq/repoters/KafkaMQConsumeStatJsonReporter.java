package com.sofa.alipay.tracer.plugins.kafkamq.repoters;

import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

/**
 * KafkaMQConsumeStatJsonReporter.
 *
 * @author chenchen6   2020/8/23 15:44
 * @since 3.1.0-SNAPSHOT
 */
public class KafkaMQConsumeStatJsonReporter extends AbstractSofaTracerStatisticReporter {
    public KafkaMQConsumeStatJsonReporter(String statTracerName, String rollingPolicy, String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    @Override
    public void doReportStat(SofaTracerSpan sofaTracerSpan) {

    }
}
