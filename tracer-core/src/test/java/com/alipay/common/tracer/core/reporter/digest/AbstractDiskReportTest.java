package com.alipay.common.tracer.core.reporter.digest;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.appender.file.TimedRollingFileAppender;
import com.alipay.common.tracer.core.base.AbstractTestBase;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.reporter.facade.Reporter;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.reporter.stat.model.StatKey;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracertest.encoder.ClientSpanEncoder;
import com.alipay.common.tracer.core.tracertest.encoder.ServerSpanEncoder;
import com.alipay.common.tracer.core.tracertest.type.TracerTestLogEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
/**
 * AbstractDiskReportTest Tester.
 *
 * @author <xiong.ch>
 * @version 1.0
 * @since <pre>四月 1, 2019</pre>
 */
public class AbstractDiskReportTest extends AbstractTestBase {
    private final String   tracerType    = "SofaTracerSpanTest";

    private final String   clientLogType = "client-log-test.log";

    private final String   serverLogType = "server-log-test.log";

    /***
     * 1s 钟统计一次
     */
    private final long CYCLE_IN_SECONDS = 1;

    private SofaTracer sofaTracer;

    private String            expectRollingPolicy       = SofaTracerConfiguration
                                                            .getRollingPolicy(TracerTestLogEnum.RPC_CLIENT
                                                                    .getRollingKey());

    private String            expectLogReserveConfig    = SofaTracerConfiguration
                                                            .getLogReserveConfig(TracerTestLogEnum.RPC_CLIENT
                                                                    .getLogReverseKey());
    private ClientSpanEncoder expectedClientSpanEncoder = new ClientSpanEncoder();

    private DiskReporterImpl clientAbstractDiskReporter;

    private AbstractSofaTracerStatisticReporter statReporter;

    @Before
    public void setUp() {
        Reporter clientReporter = new DiskReporterImpl(clientLogType, new ClientSpanEncoder());

        Reporter serverReporter = new DiskReporterImpl(serverLogType, new ServerSpanEncoder());

        sofaTracer = new SofaTracer.Builder(tracerType)
                .withTag("tracer", "AbstractDiskReportTest").withClientReporter(clientReporter)
                .withServerReporter(serverReporter).build();
        statReporter = new AbstractSofaTracerStatisticReporter("test", CYCLE_IN_SECONDS, AbstractSofaTracerStatisticReporter.DEFAULT_CYCLE,
                TimedRollingFileAppender.DAILY_ROLLING_PATTERN, "14"){

            @Override
            public void doReportStat(SofaTracerSpan sofaTracerSpan) {
                StatKey keys = new StatKey();
                long values[] = new long[0];
                this.addStat(keys, values);
            }
        };
        clientAbstractDiskReporter = new DiskReporterImpl(clientLogType, expectRollingPolicy,
                expectLogReserveConfig, expectedClientSpanEncoder, statReporter);
    }

    /**
     * Method : doReport()
     * for :Issues:#179
     */
    @Test
    public void testDoReport() {
        long startTime = 111;
        String traceId = "traceId";
        String spanId = "spanId";
        SofaTracerSpanContext sofaTracerSpanContext = new SofaTracerSpanContext(traceId, spanId,
                null);
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("key", "value");

        // other case:sofaTracerSpanContext.setSampled(Boolean.False)
        sofaTracerSpanContext.setSampled(Boolean.TRUE);
        SofaTracerSpan sofaTracerSpan = new SofaTracerSpan(this.sofaTracer, startTime,
                "testConstructSpan", sofaTracerSpanContext, tags);

        clientAbstractDiskReporter.doReport(sofaTracerSpan);
        if(!sofaTracerSpanContext.isSampled()){
            // 没有初始化
            assertEquals(false, this.clientAbstractDiskReporter.getIsDigestFileInited().get());
            Assert.assertEquals(0, statReporter.getStatData().size());
        }else {
            // 初始化
            assertEquals(true, this.clientAbstractDiskReporter.getIsDigestFileInited().get());
            Assert.assertNotEquals(0, statReporter.getStatData().size());
        }
    }
}
