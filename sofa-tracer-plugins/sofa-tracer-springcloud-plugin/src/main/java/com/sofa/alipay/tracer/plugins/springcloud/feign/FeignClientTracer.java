package com.sofa.alipay.tracer.plugins.springcloud.feign;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractClientTracer;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 10:52 AM
 * @since:
 **/
public class FeignClientTracer extends AbstractClientTracer {


    public FeignClientTracer(String tracerType) {
        super(tracerType);
    }

    @Override
    protected String getClientDigestReporterLogName() {
        return null;
    }

    @Override
    protected String getClientDigestReporterRollingKey() {
        return null;
    }

    @Override
    protected String getClientDigestReporterLogNameKey() {
        return null;
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getClientDigestEncoder() {
        return null;
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateClientStatReporter() {
        return null;
    }
}
