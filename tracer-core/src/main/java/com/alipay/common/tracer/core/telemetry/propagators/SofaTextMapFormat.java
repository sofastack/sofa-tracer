package com.alipay.common.tracer.core.telemetry.propagators;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.telemetry.SofaTelemetrySpan;
import com.alipay.common.tracer.core.telemetry.SofaTelemetrySpanContext;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import javax.annotation.Nullable;
import java.util.Collection;

public class SofaTextMapFormat implements TextMapPropagator {
    private SofaTracer sofaTracer;
    public SofaTextMapFormat(SofaTracer sofaTracer){
        this.sofaTracer = sofaTracer;
    }
    @Override
    public Collection<String> fields() {
        return null;
    }

    @Override
    public <C> void inject(Context context, @Nullable C c, TextMapSetter<C> textMapSetter) {
        //将context转换为spancontext
        //C就是carrier，也就是sofa的第三个参数
        //

        SofaTelemetrySpan span = (SofaTelemetrySpan)Span.fromContext(context);
        SofaTracerSpanContext spanContext = span.getSpan().getSofaTracerSpanContext();

        sofaTracer.inject(spanContext,
                Format.Builtin.TEXT_MAP,
                (TextMap) c);

    }

    @Override
    public <C> Context extract(Context context, @Nullable C c, TextMapGetter<C> textMapGetter) {

        SofaTracerSpanContext sofaTracerSpanContext = (SofaTracerSpanContext) sofaTracer.extract(
                                                        Format.Builtin.TEXT_MAP,
                                                        (TextMap)c);
        SofaTelemetrySpanContext sofaTelemetrySpanContext = new SofaTelemetrySpanContext(sofaTracerSpanContext);

        Context content = Context.root().with(Span.wrap(sofaTelemetrySpanContext));
        return content;
    }
}
