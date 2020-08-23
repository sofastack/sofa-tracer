package com.sofa.alipay.tracer.plugins.kafkamq.encoders;

import com.alipay.common.tracer.core.appender.builder.JsonStringBuilder;
import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.middleware.parent.AbstractDigestSpanEncoder;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

/**
 *  KafkaMQConsumeDigestJsonEncoder.
 *
 * @author chenchen6  2020/8/23 15:33
 * @since 3.1.0-SNAPSHOT
 */
public class KafkaMQConsumeDigestJsonEncoder extends AbstractDigestSpanEncoder {
    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SofaTracerSpan span) {

    }
}