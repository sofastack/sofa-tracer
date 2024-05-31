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
package com.alipay.common.tracer.core.span;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.extensions.SpanExtensionFactory;
import com.alipay.common.tracer.core.reporter.common.CommonTracerManager;
import com.alipay.common.tracer.core.reporter.facade.Reporter;
import com.alipay.common.tracer.core.tags.SpanTags;
import com.alipay.common.tracer.core.utils.AssertUtils;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.sofa.common.code.LogCode2Description;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.tag.Tags;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * SofaTracerSpan
 *
 * @author yangguanchao
 * @since 2017/06/17
 */
public class SofaTracerSpan implements Span {

    public static final char                                ARRAY_SEPARATOR      = '|';

    private final SofaTracer                                sofaTracer;

    private final List<SofaTracerSpanReferenceRelationship> spanReferences;
    /** tags for String  */
    private final Map<String, String>                       tagsWithStr          = new ConcurrentHashMap<>();
    /** tags for Boolean */
    private final Map<String, Boolean>                      tagsWithBool         = new ConcurrentHashMap<>();
    /** tags for Number  */
    private final Map<String, Number>                       tagsWithNumber       = new ConcurrentHashMap<>();

    private final ConcurrentLinkedQueue<LogData>            logs                 = new ConcurrentLinkedQueue<>();

    private String                                          operationName        = StringUtils.EMPTY_STRING;

    private final SofaTracerSpanContext                     sofaTracerSpanContext;

    private long                                            startTime;
    private long                                            endTime              = -1;

    /**
     * Only meaningful when reporting
     * Digest log type,The logs correctly printed key information.
     * For example, the client is rpc-client-digest.log and the server is rpc-server-digest.log
     */
    private String                                          logType              = StringUtils.EMPTY_STRING;

    /**
     * parent span. Describe the child-of relationship
     */
    private SofaTracerSpan                                  parentSofaTracerSpan = null;

    public SofaTracerSpan cloneInstance() {
        SofaTracerSpanContext spanContext = this.sofaTracerSpanContext.cloneInstance();
        Map<String, Object> tags = new HashMap<>();
        tags.putAll(this.tagsWithBool);
        tags.putAll(this.tagsWithStr);
        tags.putAll(this.tagsWithNumber);
        SofaTracerSpan cloneSpan = new SofaTracerSpan(this.sofaTracer, this.startTime,
            this.spanReferences, this.operationName, spanContext, tags);
        if (this.logs != null && this.logs.size() > 0) {
            for (LogData logData : this.logs) {
                cloneSpan.log(logData);
            }
        }
        cloneSpan.setEndTime(this.endTime);
        cloneSpan.setLogType(this.logType);
        cloneSpan.setParentSofaTracerSpan(this.parentSofaTracerSpan);

        return cloneSpan;
    }

    /**
     * As a server-Side(Kind.type is server): After reverting back to {@link SofaTracerSpanContext},
     * you can directly construct Server Span (traceId, spanId unchanged)
     *
     * @param sofaTracer            sofaTracer
     * @param startTime             startTime
     * @param operationName         operationName
     * @param sofaTracerSpanContext sofaTracerSpanContext
     * @param tags                  tags
     */
    public SofaTracerSpan(SofaTracer sofaTracer, long startTime, String operationName,
                          SofaTracerSpanContext sofaTracerSpanContext, Map<String, ?> tags) {
        this(sofaTracer, startTime, null, operationName,
            sofaTracerSpanContext != null ? sofaTracerSpanContext : SofaTracerSpanContext
                .rootStart(), tags);
    }

    /**
     * Note:
     *
     * 1.As a server-side: After reverting back to {@link SofaTracerSpanContext}, you can directly construct Server Span (traceId, spanId unchanged)
     *
     * 2.As a client: need to be built by {@link SofaTracer.SofaTracerSpanBuilder#start()}
     *
     * @param sofaTracer            sofaTracer
     * @param startTime             startTime
     * @param spanReferences        spanReferences
     * @param operationName         operationName
     * @param sofaTracerSpanContext sofaTracerSpanContext
     * @param tags                  tags
     */
    public SofaTracerSpan(SofaTracer sofaTracer, long startTime,
                          List<SofaTracerSpanReferenceRelationship> spanReferences,
                          String operationName, SofaTracerSpanContext sofaTracerSpanContext,
                          Map<String, ?> tags) {
        AssertUtils.notNull(sofaTracer);
        AssertUtils.notNull(sofaTracerSpanContext);
        this.sofaTracer = sofaTracer;
        this.startTime = startTime;
        this.spanReferences = spanReferences != null ? new ArrayList<>(spanReferences) : null;
        this.operationName = operationName;
        this.sofaTracerSpanContext = sofaTracerSpanContext;
        this.setTags(tags);

        SpanExtensionFactory.logStartedSpan(this);
    }

    @Override
    public SpanContext context() {
        return this.sofaTracerSpanContext;
    }

    @Override
    public void finish() {
        this.finish(System.currentTimeMillis());
    }

    @Override
    public void finish(long endTime) {
        this.setEndTime(endTime);
        //Key record:report span
        this.sofaTracer.reportSpan(this);
        SpanExtensionFactory.logStoppedSpan(this);
    }

    @Override
    public void close() {
        this.finish();
    }

    @Override
    public Span setTag(String key, String value) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            return this;
        }
        this.tagsWithStr.put(key, value);
        //to set log type by span kind type
        if (isServer()) {
            Reporter serverReporter = this.sofaTracer.getServerReporter();
            if (serverReporter != null) {
                this.setLogType(serverReporter.getReporterType());
            }
        } else if (isClient()) {
            Reporter clientReporter = this.sofaTracer.getClientReporter();
            if (clientReporter != null) {
                this.setLogType(clientReporter.getReporterType());
            }
        }
        return this;
    }

    @Override
    public Span setTag(String key, boolean value) {
        this.tagsWithBool.put(key, value);
        return this;
    }

    @Override
    public Span setTag(String key, Number number) {
        if (number == null) {
            return this;
        }
        this.tagsWithNumber.put(key, number);
        return this;
    }

    @Override
    public Span log(String eventValue) {
        //log with current time
        return log(System.currentTimeMillis(), eventValue);
    }

    @Override
    public Span log(long currentTime, String eventValue) {
        AssertUtils.isTrue(currentTime >= startTime, "Current time must greater than start time");
        Map<String, String> fields = new HashMap<>();
        fields.put(LogData.EVENT_TYPE_KEY, eventValue);
        return this.log(currentTime, fields);
    }

    public Span log(LogData logData) {
        if (logData == null) {
            return this;
        }
        this.logs.add(logData);
        return this;
    }

    @Override
    public Span log(long currentTime, Map<String, ?> map) {
        AssertUtils.isTrue(currentTime >= startTime, "current time must greater than start time");
        this.logs.add(new LogData(currentTime, map));
        return this;
    }

    @Override
    public Span log(Map<String, ?> map) {
        return this.log(System.currentTimeMillis(), map);
    }

    @Override
    public Span log(String eventName, /* @Nullable */Object payload) {
        //key:value
        return this.log(System.currentTimeMillis(), eventName, payload);
    }

    @Override
    public Span log(long currentTime, String eventName, /* @Nullable */Object payload) {
        //key:value
        AssertUtils.isTrue(currentTime >= startTime, "current time must greater than start time");
        Map<String, Object> fields = new HashMap<>();
        fields.put(eventName, payload);
        return this.log(currentTime, fields);
    }

    /**
     *
     * The default settings are business baggage
     * @param key
     * @param value
     * @return
     */
    @Override
    public Span setBaggageItem(String key, String value) {
        this.sofaTracerSpanContext.setBizBaggageItem(key, value);
        return this;
    }

    /**
     * The default read are business baggage
     * @param key
     * @return
     */
    @Override
    public String getBaggageItem(String key) {
        return this.sofaTracerSpanContext.getBizBaggageItem(key);
    }

    @Override
    public Span setOperationName(String operationName) {
        this.operationName = operationName;
        return this;
    }

    //======================= Extended API interface starts

    /**
     *
     * @param errorType         errorType error description:timeout_error/biz_error...
     * @param context           context
     * @param e                 e
     * @param errorSourceApp    errorSourceApp trade|rpc
     * @param errorSources      errorSources
     */
    public void reportError(String errorType, Map<String, String> context, Throwable e,
                            String errorSourceApp, String... errorSources) {
        Tags.ERROR.set(this, true);
        //all tags set
        Map<String, Object> tags = new HashMap<>();
        tags.putAll(this.getTagsWithStr());
        tags.putAll(this.getTagsWithBool());
        tags.putAll(this.getTagsWithNumber());
        tags.put(SpanTags.CURR_APP_TAG.getKey(), errorSourceApp);
        //Construct new CommonLogSpan
        CommonLogSpan commonLogSpan = new CommonLogSpan(this.sofaTracer,
            System.currentTimeMillis(), this.getOperationName(), this.getSofaTracerSpanContext(),
            tags);
        commonLogSpan.addSlot(Thread.currentThread().getName());
        commonLogSpan.addSlot(errorType);
        // There may be a separator in the output of the business customization, now replace the separator with the corresponding escape character
        commonLogSpan.addSlot(StringUtils.arrayToString(errorSources, ARRAY_SEPARATOR, "", ""));
        commonLogSpan.addSlot(StringUtils.mapToString(context));
        commonLogSpan.addSlot(this.getSofaTracerSpanContext() == null ? StringUtils.EMPTY_STRING
            : this.getSofaTracerSpanContext().getBizSerializedBaggage());

        if (e == null) {
            commonLogSpan.addSlot(StringUtils.EMPTY_STRING);
        } else {
            StringWriter sw = new StringWriter(256);
            e.printStackTrace(new PrintWriter(sw));
            String exception = sw.getBuffer().toString();
            commonLogSpan.addSlot(exception);
        }
        CommonTracerManager.reportError(commonLogSpan);
    }

    /**
     * Print Common Profile Log
     *
     * @param profileApp     profileApp
     * @param protocolType   protocolType
     * @param profileMessage profileMessage
     */
    public void profile(String profileApp, String protocolType, String profileMessage) {
        Map<String, Object> tags = new HashMap<>();
        tags.putAll(this.getTagsWithStr());
        tags.putAll(this.getTagsWithBool());
        tags.putAll(this.getTagsWithNumber());
        tags.put(SpanTags.CURR_APP_TAG.getKey(), profileApp);
        CommonLogSpan commonLogSpan = new CommonLogSpan(this.sofaTracer,
            System.currentTimeMillis(), this.getOperationName(), this.getSofaTracerSpanContext(),
            tags);

        commonLogSpan.addSlot(protocolType);
        commonLogSpan.addSlot(profileMessage);

        CommonTracerManager.reportProfile(commonLogSpan);
    }

    /**
     * Return itself as the parent of the next context
     * <p>
     * Use countMatches to count . for higher performance, see performance test data for com.alipay.common.tracer.benchmark.CountBenchmark

     * Preventing SofaTracerSpan from nesting too deeply causes a memory leak.
     * This time recreates a context so that the above context can be released.
     * </p>
     * @return
     */
    public SofaTracerSpan getThisAsParentWhenExceedLayer() {
        final SofaTracerSpan parent;
        String rpcId = this.sofaTracerSpanContext.getSpanId();
        if (StringUtils.countMatches(rpcId, '.') + 1 > SofaTracerConstant.MAX_LAYER) {
            SofaTracerSpanContext parentSpanContext = SofaTracerSpanContext.rootStart();
            // discard tags
            Map<String, String> baggage = new HashMap<>();
            baggage.putAll(this.sofaTracerSpanContext.getBizBaggage());
            parentSpanContext.addBizBaggage(baggage);
            parent = new SofaTracerSpan(this.sofaTracer, System.currentTimeMillis(),
                this.operationName, parentSpanContext, null);
            // Record in the log to prevent this from happening but not to know quickly
            SelfLog.errorWithTraceId("OpenTracing Span layer exceed max layer limit "
                                     + SofaTracerConstant.MAX_LAYER,
                this.sofaTracerSpanContext.getTraceId());
        } else {
            parent = this;
        }

        return parent;
    }

    //======================= Extended API interface end

    public List<SofaTracerSpanReferenceRelationship> getSpanReferences() {
        if (spanReferences == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(spanReferences);
    }

    public SofaTracer getSofaTracer() {
        return sofaTracer;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getDurationMicroseconds() {
        return this.endTime - this.startTime;
    }

    public Map<String, String> getTagsWithStr() {
        return tagsWithStr;
    }

    public Map<String, Boolean> getTagsWithBool() {
        return tagsWithBool;
    }

    public Map<String, Number> getTagsWithNumber() {
        return tagsWithNumber;
    }

    public String getOperationName() {
        return operationName;
    }

    public SofaTracerSpanContext getSofaTracerSpanContext() {
        return sofaTracerSpanContext;
    }

    public ConcurrentLinkedQueue<LogData> getLogs() {
        return logs;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public SofaTracerSpan getParentSofaTracerSpan() {
        if (this.parentSofaTracerSpan != null) {
            return this.parentSofaTracerSpan.getThisAsParentWhenExceedLayer();
        }
        return null;
    }

    public void setParentSofaTracerSpan(SofaTracerSpan parentSofaTracerSpan) {
        this.parentSofaTracerSpan = parentSofaTracerSpan;
    }

    public boolean isServer() {
        return Tags.SPAN_KIND_SERVER.equals(tagsWithStr.get(Tags.SPAN_KIND.getKey()));
    }

    public boolean isClient() {
        return Tags.SPAN_KIND_CLIENT.equals(tagsWithStr.get(Tags.SPAN_KIND.getKey()));
    }

    private void setTags(Map<String, ?> tags) {
        if (tags == null || tags.size() <= 0) {
            return;
        }
        for (Map.Entry<String, ?> entry : tags.entrySet()) {
            String key = entry.getKey();
            if (StringUtils.isBlank(key)) {
                continue;
            }
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (value instanceof String) {
                this.setTag(key, (String) value);
            } else if (value instanceof Boolean) {
                this.setTag(key, (Boolean) value);
            } else if (value instanceof Number) {
                this.setTag(key, (Number) value);
            } else {
                SelfLog.error(String.format(
                    LogCode2Description.convert(SofaTracerConstant.SPACE_ID, "01-00012"),
                    value.getClass()));
            }
        }
    }

    @Override
    public String toString() {
        return "SofaTracerSpan{" + "operationName='" + operationName + '\''
               + ", sofaTracerSpanContext=" + sofaTracerSpanContext + '}';
    }

}
