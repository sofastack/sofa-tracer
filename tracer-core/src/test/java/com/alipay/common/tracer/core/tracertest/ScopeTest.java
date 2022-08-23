package com.alipay.common.tracer.core.tracertest;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.reporter.digest.DiskReporterImpl;
import com.alipay.common.tracer.core.reporter.facade.Reporter;
import com.alipay.common.tracer.core.samplers.Sampler;
import com.alipay.common.tracer.core.samplers.SofaTracerPercentageBasedSampler;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.LogData;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracertest.encoder.ClientSpanEncoder;
import com.alipay.common.tracer.core.tracertest.encoder.ServerSpanEncoder;
import com.alipay.common.tracer.core.tracertest.type.TracerTestLogEnum;
import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.tag.Tags;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ScopeTest {
    private final String tracerType           = "TracerTestService";
    private final String tracerGlobalTagKey   = "tracerkey";
    private final String tracerGlobalTagValue = "tracervalue";
    private SofaTracer   sofaTracer;

    @Before
    public void beforeInstance(){
        SofaTracerConfiguration.setProperty(SofaTracerConfiguration.SAMPLER_STRATEGY_NAME_KEY,
                SofaTracerPercentageBasedSampler.TYPE);
        SofaTracerConfiguration.setProperty(
                SofaTracerConfiguration.SAMPLER_STRATEGY_PERCENTAGE_KEY, "100");

        //client
        DiskReporterImpl clientReporter = new DiskReporterImpl(
                TracerTestLogEnum.RPC_CLIENT.getDefaultLogName(), new ClientSpanEncoder());

        //server
        DiskReporterImpl serverReporter = new DiskReporterImpl(
                TracerTestLogEnum.RPC_SERVER.getDefaultLogName(), new ServerSpanEncoder());
        this.sofaTracer = new SofaTracer.Builder(tracerType)
                .withClientReporter(clientReporter)
                .withServerReporter(serverReporter)
                .build();
    }

    @Test
    public void testActiveSpan(){
        SofaTracerSpan sofaTracerSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("testActiveSpan").start();
        try(Scope scope = this.sofaTracer.scopeManager().activate(sofaTracerSpan)){
            assertEquals(sofaTracerSpan, this.sofaTracer.activeSpan());
        }finally {
            sofaTracerSpan.finish();
        }
    }

    @Test
    public void testActivateSpan(){
        SofaTracerSpan sofaTracerSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("testActivateSpan").start();
        this.sofaTracer.activateSpan(sofaTracerSpan);
        assertEquals(sofaTracerSpan, this.sofaTracer.activeSpan());
    }

    @Test
    public void testActiveSpanPropagation(){
        SofaTracerSpan sofaTracerSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("parent").start();
        try(Scope scope = this.sofaTracer.activateSpan(sofaTracerSpan)){
            assertEquals(sofaTracerSpan, this.sofaTracer.scopeManager().activeSpan());
        }finally {
            sofaTracerSpan.finish();
        }
    }


    @Test
    public void testActiveSpanAutoReference() {
        SofaTracerSpan parent = (SofaTracerSpan) this.sofaTracer.buildSpan("parent").start();
        try (Scope scope = this.sofaTracer.activateSpan(parent)) {
            SofaTracerSpan child = (SofaTracerSpan) this.sofaTracer.buildSpan("child").start();
            child.finish();
            assertEquals(References.CHILD_OF, child.getSpanReferences().get(0).getReferenceType());

        } finally {
            parent.finish();
        }

    }

    @Test
    public void testIgnoreActiveSpan(){
        SofaTracerSpan parentSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("parent")
                .start();
        try(Scope scope = this.sofaTracer.activateSpan(parentSpan)){
            assertEquals(this.sofaTracer.activeSpan(), parentSpan);
            SofaTracerSpan sonSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("child").
                    ignoreActiveSpan().start();
            sonSpan.finish();
            assertEquals(sonSpan.getSpanReferences().size(),0);
            SofaTracerSpan daughterSpan = (SofaTracerSpan) this.sofaTracer.buildSpan("child").start();
            daughterSpan.finish();
            assertEquals(daughterSpan.getSpanReferences().size(),1);
        }finally {
            parentSpan.finish();
        }
    }




    @Test
    public void testContextScope(){
        SofaTracer tracer = new SofaTracer.Builder(tracerType)
                .build();
        SofaTracerSpan sofaTracerSpan = (SofaTracerSpan) tracer.buildSpan("parent").start();
        try(Scope scope = tracer.activateSpan(sofaTracerSpan)){
            assertEquals(sofaTracerSpan, tracer.scopeManager().activeSpan());
        }finally {
            sofaTracerSpan.finish();
        }
    }








}
