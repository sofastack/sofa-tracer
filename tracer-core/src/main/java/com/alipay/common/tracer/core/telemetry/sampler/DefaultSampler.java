package com.alipay.common.tracer.core.telemetry.sampler;

import com.alipay.common.tracer.core.samplers.Sampler;
import com.alipay.common.tracer.core.samplers.SamplingStatus;
import com.alipay.common.tracer.core.span.SofaTracerSpan;

public class DefaultSampler implements Sampler {
    @Override
    public SamplingStatus sample(SofaTracerSpan sofaTracerSpan) {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public void close() {

    }
}
