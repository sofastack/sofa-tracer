package com.alipay.common.tracer.core.telemetry.baggage;

import com.alipay.common.tracer.core.span.SofaTracerSpan;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.baggage.BaggageEntry;
import io.opentelemetry.api.baggage.BaggageEntryMetadata;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.BiConsumer;

public class SofaTelemetryBaggage implements Baggage {
    private SofaTracerSpan sofaTracerSpan;

    SofaTelemetryBaggage(SofaTracerSpan sofaTracerSpan){
        this.sofaTracerSpan = sofaTracerSpan;
    }

    public Map<String ,String> getBizBaggage(){
        return this.sofaTracerSpan.getSofaTracerSpanContext().getBizBaggage();
    }

    @Override
    public int size() {
        int size =  this.getBizBaggage().size();
        return size;
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super BaggageEntry> biConsumer) {

    }

    @Override
    public Map<String, BaggageEntry> asMap() {
        return null;
    }

    @Nullable
    @Override
    public String getEntryValue(String s) {
        return sofaTracerSpan.getBaggageItem(s);
    }

    @Override
    public BaggageBuilder toBuilder() {
        return new SofaTelemetryBaggageBuilder(sofaTracerSpan);
    }
}
