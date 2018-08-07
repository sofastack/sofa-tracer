package com.alipay.sofa.tracer.plugins.httpclient;

import com.alipay.common.tracer.core.appender.encoder.SpanEncoder;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracer.AbstractClientTracer;

/**
 * HttpClientTracer
 *
 * @author yangguanchao
 * @since 2018/08/07
 */
public class HttpClientTracer extends AbstractClientTracer {

    public static final String HTTP_CLIENT_JSON_FORMAT_OUTPUT = "http_client_json_format_output";

    private volatile static HttpClientTracer httpClientTracer = null;

    /***
     * Http Client Tracer Singleton
     * @return singleton
     */
    public static HttpClientTracer getHttpClientTracerSingleton() {
        if (httpClientTracer == null) {
            synchronized (HttpClientTracer.class) {
                if (httpClientTracer == null) {
                    httpClientTracer = new HttpClientTracer();
                }
            }
        }
        return httpClientTracer;
    }

    private HttpClientTracer() {
        super("httpclient");
    }

    @Override
    protected String getClientDigestReporterLogName() {
        return HttpClientLogEnum.HTTP_CLIENT_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getClientDigestReporterRollingKey() {
        return HttpClientLogEnum.HTTP_CLIENT_DIGEST.getRollingKey();
    }

    @Override
    protected String getClientDigestReporterLogNameKey() {
        return HttpClientLogEnum.HTTP_CLIENT_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SofaTracerSpan> getClientDigestEncoder() {
        String isJsonOutput = SofaTracerConfiguration.getProperty(HTTP_CLIENT_JSON_FORMAT_OUTPUT);
        // default json output
        if (Boolean.FALSE.toString().equalsIgnoreCase(isJsonOutput)) {
            return new HttpClientDigestEncoder();
        } else {
            //blank or true is json output
            return new HttpClientDigestJsonEncoder();
        }
    }

    @Override
    protected AbstractSofaTracerStatisticReporter generateClientStatReporter() {
        HttpClientLogEnum httpClientLogEnum = HttpClientLogEnum.HTTP_CLIENT_STAT;
        String statLog = httpClientLogEnum.getDefaultLogName();
        String statRollingPolicy = SofaTracerConfiguration.getRollingPolicy(httpClientLogEnum.getRollingKey());
        String statLogReserveConfig = SofaTracerConfiguration.getLogReserveConfig(httpClientLogEnum.getLogNameKey());
        // default json output
        String isJsonOutput = SofaTracerConfiguration.getProperty(HTTP_CLIENT_JSON_FORMAT_OUTPUT);
        if (Boolean.FALSE.toString().equalsIgnoreCase(isJsonOutput)) {
            return new HttpClientStatReporter(statLog, statRollingPolicy, statLogReserveConfig);
        }else {
            //blank or true is json output
            return new HttpClientStatJsonReporter(statLog, statRollingPolicy, statLogReserveConfig);
        }
    }
}
