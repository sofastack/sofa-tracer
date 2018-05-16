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
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.tag.Tags;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * SofaTracerSpan
 *
 * @author yangguanchao
 * @since 2017/06/17
 */
public class SofaTracerSpan implements Span {

    public static final char                                ARRAY_SEPARATOR      = '|';

    private final SofaTracer                                sofaTracer;

    /***
     * tags String,Integer,boolean
     */
    private final List<SofaTracerSpanReferenceRelationship> spanReferences;

    private final Map<String, String>                       tagsWithStr          = new LinkedHashMap<String, String>();

    private final Map<String, Boolean>                      tagsWithBool         = new LinkedHashMap<String, Boolean>();

    private final Map<String, Number>                       tagsWithNumber       = new LinkedHashMap<String, Number>();

    private final List<LogData>                             logs                 = new LinkedList<LogData>();

    private String                                          operationName        = StringUtils.EMPTY_STRING;

    private final SofaTracerSpanContext                     sofaTracerSpanContext;

    /***
     * 启动时间
     */
    private long                                            startTime;

    /***
     * span endTime
     */
    private long                                            endTime              = -1;

    /***
     *
     * report时才有意义:摘要日志类型,日志能够正确打印的关键信息:当前 span 的日志类型,如:客户端为 rpc-client-digest.log,服务端为 rpc-server-digest.log
     */
    private String                                          logType              = StringUtils.EMPTY_STRING;

    /***
     * 父亲 span,当作为客户端结束并弹出线程上下文时,需要将父亲 span 再放入
     */
    private SofaTracerSpan                                  parentSofaTracerSpan = null;

    public SofaTracerSpan cloneInstance() {
        SofaTracerSpanContext spanContext = this.sofaTracerSpanContext.cloneInstance();
        Map<String, Object> tags = new HashMap<String, Object>();
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

    /***
     * 作为服务端:还原回 {@link SofaTracerSpanContext} 之后,就可以直接构造 Server Span(traceId,spanId 不变)
     *
     * @param sofaTracer sofaTracer 当前具体中间件 tracer
     * @param startTime 开始时间
     * @param operationName 操作名称
     * @param sofaTracerSpanContext 当前上下文信息
     * @param tags 标签
     */
    public SofaTracerSpan(SofaTracer sofaTracer, long startTime, String operationName,
                          SofaTracerSpanContext sofaTracerSpanContext, Map<String, ?> tags) {
        //作为服务端还原回 sofaTracerSpanContext,traceId:spanId 不变,所以 spanReferences 为空
        this(sofaTracer, startTime, null, operationName,
            sofaTracerSpanContext != null ? sofaTracerSpanContext : SofaTracerSpanContext
                .rootStart(), tags);
    }

    /***
     * 注意:
     *
     * 1.作为服务端:还原回 {@link SofaTracerSpanContext} 之后,就可以直接构造 Server Span(traceId,spanId 不变)
     *
     *
     * 2.作为客户端:需要通过 {@link SofaTracer.SofaTracerSpanBuilder#start() 构建}
     *
     * @param sofaTracer 当前 tracer
     * @param startTime 开始时间
     * @param spanReferences 引用关系
     * @param operationName 操作名称
     * @param sofaTracerSpanContext 当前上下文
     * @param tags 标签
     */
    public SofaTracerSpan(SofaTracer sofaTracer, long startTime,
                          List<SofaTracerSpanReferenceRelationship> spanReferences,
                          String operationName, SofaTracerSpanContext sofaTracerSpanContext,
                          Map<String, ?> tags) {
        AssertUtils.notNull(sofaTracer);
        AssertUtils.notNull(sofaTracerSpanContext);
        this.sofaTracer = sofaTracer;
        this.startTime = startTime;
        this.spanReferences = spanReferences != null ? new ArrayList<SofaTracerSpanReferenceRelationship>(
            spanReferences) : null;
        this.operationName = operationName;
        this.sofaTracerSpanContext = sofaTracerSpanContext;
        //tags
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
        //关键记录:report span
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
        //注意:server 还是 client 在 OpenTracing 标准中是用 tags 标识的,所以在这里进行判断
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
        //使用默认的 event key,关键记录 span 事件:cs/cr/ss/sr
        return log(System.currentTimeMillis(), eventValue);
    }

    @Override
    public Span log(long currentTime, String eventValue) {
        //关键记录 span 事件
        AssertUtils.isTrue(currentTime >= startTime, "Current time must greater than start time");
        //记录下相关事件
        Map<String, String> fields = new HashMap<String, String>();
        fields.put(LogData.EVENT_TYPE_KEY, eventValue);
        //save
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
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put(eventName, payload);
        return this.log(currentTime, fields);
    }

    /***
     * 默认设置和读取的都是业务 baggage
     * @param key 关键字
     * @param value 值
     * @return 当前 span
     */
    @Override
    public Span setBaggageItem(String key, String value) {
        this.sofaTracerSpanContext.setBizBaggageItem(key, value);
        return this;
    }

    /***
     * 默认设置和读取的都是业务 baggage
     * @param key 关键字
     * @return 当前 span
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

    //=======================扩展的 API 接口开始

    /***
     *
     * @param errorType errorType error 描述:timeout_error/biz_error...
     * @param context context 记录的上下文信息
     * @param e e 异常信息
     * @param errorSourceApp errorSources 故障源 trade|rpc
     * @param errorSources errorSources 故障源数组
     */
    public void reportError(String errorType, Map<String, String> context, Throwable e,
                            String errorSourceApp, String... errorSources) {
        Tags.ERROR.set(this, true);
        //关键:用于记录所有的持久化数据
        Map<String, Object> tags = new HashMap<String, Object>();
        tags.putAll(this.getTagsWithStr());
        tags.putAll(this.getTagsWithBool());
        tags.putAll(this.getTagsWithNumber());
        tags.put(SpanTags.CURR_APP_TAG.getKey(), errorSourceApp);
        //构造新的
        CommonLogSpan commonLogSpan = new CommonLogSpan(this.sofaTracer,
            System.currentTimeMillis(), this.getOperationName(), this.getSofaTracerSpanContext(),
            tags);
        commonLogSpan.addSlot(Thread.currentThread().getName());
        commonLogSpan.addSlot(errorType);
        // 业务定制的输出中可能会有分隔符，现在将分割符替换成相应的转义字符
        commonLogSpan.addSlot(StringUtils.arrayToString(errorSources, ARRAY_SEPARATOR, "", ""));
        commonLogSpan.addSlot(StringUtils.mapToString(context));
        commonLogSpan.addSlot(this.getSofaTracerSpanContext() == null ? StringUtils.EMPTY_STRING
            : this.getSofaTracerSpanContext().getBizSerializedBaggage());

        if (e == null) {
            commonLogSpan.addSlot(StringUtils.EMPTY_STRING);
        } else {
            StringWriter sw = new StringWriter(256);
            e.printStackTrace(new PrintWriter(sw));
            // 同上
            String exception = sw.getBuffer().toString();
            commonLogSpan.addSlot(exception);
        }
        //report error 使用客户端服务端tags进行区分
        CommonTracerManager.reportError(commonLogSpan);
    }

    /**
     * 打印 Common Profile 日志
     *
     * @param profileApp     profile 应用
     * @param protocolType   协议类型
     * @param profileMessage 日志内容
     */
    public void profile(String profileApp, String protocolType, String profileMessage) {
        //关键:用于记录所有的持久化数据
        Map<String, Object> tags = new HashMap<String, Object>();
        tags.putAll(this.getTagsWithStr());
        tags.putAll(this.getTagsWithBool());
        tags.putAll(this.getTagsWithNumber());
        tags.put(SpanTags.CURR_APP_TAG.getKey(), profileApp);
        //构造新的,关键:用于记录所有的持久化数据
        CommonLogSpan commonLogSpan = new CommonLogSpan(this.sofaTracer,
            System.currentTimeMillis(), this.getOperationName(), this.getSofaTracerSpanContext(),
            tags);

        commonLogSpan.addSlot(protocolType);
        commonLogSpan.addSlot(profileMessage);

        CommonTracerManager.reportProfile(commonLogSpan);
    }

    /**
     * 返回自身作为下一个上下文的 parent
     * <p>
     * 采用 countMatches 来对 . 进行计数, 以获取更高的性能, 见com.alipay.common.tracer.benchmark.CountBenchmark 的性能测试数据
     * 防止 SofaTracerSpan 嵌套过深导致内存泄漏。这个时候重新创建一个上下文，使上面的上下文都能够释放。
     *
     * @return 重新构造的 span
     */
    public SofaTracerSpan getThisAsParentWhenExceedLayer() {
        final SofaTracerSpan parent;
        String rpcId = this.sofaTracerSpanContext.getSpanId();
        if (StringUtils.countMatches(rpcId, '.') + 1 > SofaTracerConstant.MAX_LAYER) {
            SofaTracerSpanContext parentSpanContext = SofaTracerSpanContext.rootStart();
            // 虽然重新创建一个 Span, 但是穿透数据没有必要丢掉;但是丢弃 tags
            Map<String, String> baggage = new HashMap<String, String>();
            baggage.putAll(this.sofaTracerSpanContext.getBizBaggage());
            parentSpanContext.addBizBaggage(baggage);
            //重新构造
            parent = new SofaTracerSpan(this.sofaTracer, System.currentTimeMillis(),
                this.operationName, parentSpanContext, null);
            // 在日志中进行记录, 防止发生了这个情况却无法快速知晓
            SelfLog.errorWithTraceId("OpenTracing Span layer exceed max layer limit "
                                     + SofaTracerConstant.MAX_LAYER,
                this.sofaTracerSpanContext.getTraceId());
        } else {
            parent = this;
        }

        return parent;
    }

    //=======================扩展的 API 接口结束

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

    public List<LogData> getLogs() {
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
                //初始化时候,tags也可以作为 client 和 server 的判断依据
                this.setTag(key, (String) value);
            } else if (value instanceof Boolean) {
                this.setTag(key, (Boolean) value);
            } else if (value instanceof Number) {
                this.setTag(key, (Number) value);
            } else {
                SelfLog.error("Span tags unsupported type [" + value.getClass() + "]");
            }
        }
    }

    @Override
    public String toString() {
        return "SofaTracerSpan{" + "operationName='" + operationName + '\''
               + ", sofaTracerSpanContext=" + sofaTracerSpanContext + '}';
    }

}
