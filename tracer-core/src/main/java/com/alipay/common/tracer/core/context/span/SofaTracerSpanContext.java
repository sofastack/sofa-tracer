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
package com.alipay.common.tracer.core.context.span;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.generator.TraceIdGenerator;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.alipay.common.tracer.core.utils.TracerUtils;
import io.opentracing.SpanContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SofaTracerSpanContext
 *
 * @author yangguanchao
 * @since 2017/06/17
 */
public class SofaTracerSpanContext implements SpanContext {

    //spanId 分隔符
    public static final String        RPC_ID_SEPARATOR       = ".";

    //======================== 以下为序列化数据的 key ========================

    private static final String       TRACE_ID_KET           = "tcid";

    private static final String       SPAN_ID_KET            = "spid";

    private static final String       PARENT_SPAN_ID_KET     = "pspid";

    private static final String       SAMPLE_KET             = "sample";

    /***
     * 序列化系统透传属性 key 的关键字前缀(需要序列化添加,反序列化去除 key 的前缀)
     */
    private static final String       SYS_BAGGAGE_PREFIX_KEY = "_sys_";

    //span  关键属性参数
    private String                    traceId                = StringUtils.EMPTY_STRING;

    private String                    spanId                 = StringUtils.EMPTY_STRING;

    private String                    parentId               = StringUtils.EMPTY_STRING;

    /***
     * 默认不会采样
     */
    private boolean                   isSampled              = false;

    /***
     * 系统透传数据,主要是指系统维度的透传数据,注意业务透传不能使用此字段
     */
    private final Map<String, String> sysBaggage             = new ConcurrentHashMap<String, String>();

    /***
     * 透传数据,主要是指业务的透传数据
     */
    private final Map<String, String> bizBaggage             = new ConcurrentHashMap<String, String>();

    /**
     * 子上下文的计数器
     */
    private AtomicInteger             childContextIndex      = new AtomicInteger(0);

    /***
     * clone 一个当前模型
     * @return clone 对象
     */
    public SofaTracerSpanContext cloneInstance() {
        SofaTracerSpanContext spanContext = new SofaTracerSpanContext(this.traceId, this.spanId,
            this.parentId, this.isSampled);
        spanContext.addSysBaggage(this.sysBaggage);
        spanContext.addBizBaggage(this.bizBaggage);
        spanContext.childContextIndex = this.childContextIndex;
        return spanContext;
    }

    public SofaTracerSpanContext() {
        //默认不会采样
        this(StringUtils.EMPTY_STRING, StringUtils.EMPTY_STRING, null, false);
    }

    public SofaTracerSpanContext(String traceId, String spanId) {
        //默认不会采样
        this(traceId, spanId, null, false);
    }

    public SofaTracerSpanContext(String traceId, String spanId, String parentId) {
        //默认不会采样
        this(traceId, spanId, parentId, false);
    }

    public SofaTracerSpanContext(String traceId, String spanId, String parentId, boolean isSampled) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.parentId = StringUtils.isBlank(parentId) ? this.genParentSpanId(spanId) : parentId;
        this.isSampled = isSampled;
    }

    public SofaTracerSpanContext addBizBaggage(Map<String, String> bizBaggage) {
        if (bizBaggage != null && bizBaggage.size() > 0) {
            this.bizBaggage.putAll(bizBaggage);
        }
        return this;
    }

    public SofaTracerSpanContext addSysBaggage(Map<String, String> sysBaggage) {
        if (sysBaggage != null && sysBaggage.size() > 0) {
            this.sysBaggage.putAll(sysBaggage);
        }
        return this;
    }

    /***
     * 系统和业务 baggage 均返回
     * @return 迭代器
     */
    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        Map<String, String> allBaggage = new HashMap<String, String>();
        if (this.bizBaggage != null && this.bizBaggage.size() > 0) {
            allBaggage.putAll(this.bizBaggage);
        }
        if (this.sysBaggage != null && this.sysBaggage.size() > 0) {
            allBaggage.putAll(this.sysBaggage);
        }
        return allBaggage.entrySet();
    }

    /****
     * 关键信息统一生成字符串
     * @return
     */
    private String contextAsString() {// NOPMD
        return String.format("%s:%s:%s:%s", traceId, spanId, parentId, isSampled);
    }

    /**
     * 将 Tracer 中的穿透属性序列化成一个 String
     * <p>
     * <strong>此方法一般是 Tracer 内部或者直接集成 Tracer 的中间件使用</strong>
     * </p>
     *
     * @return 序列化以后的穿透属性
     */
    public String getBizSerializedBaggage() {
        return StringUtils.mapToString(bizBaggage);
    }

    public String getSysSerializedBaggage() {
        return StringUtils.mapToString(this.sysBaggage);
    }

    /**
     * 将一个穿透属性序列化后的 String，反序列化回来成为一个 Map 接口
     * <p>
     * <strong>此方法一般是 Tracer 内部或者直接集成 Tracer 的中间件使用</strong>
     * </p>
     *
     * @param bizBaggageAttrs 序列化后的穿透属性 String
     */
    public void deserializeBizBaggage(String bizBaggageAttrs) {
        StringUtils.stringToMap(bizBaggageAttrs, this.bizBaggage);

        if (StringUtils.isNotBlank(bizBaggageAttrs)) {
            if (bizBaggageAttrs.length() > TracerUtils.getBaggageMaxLength() / 2) {
                SelfLog.infoWithTraceId("Get biz baggage from upstream system, and the length is "
                                        + bizBaggageAttrs.length());
            }
        }
    }

    public void deserializeSysBaggage(String sysBaggageAttrs) {
        StringUtils.stringToMap(sysBaggageAttrs, this.sysBaggage);

        if (StringUtils.isNotBlank(sysBaggageAttrs)) {
            if (sysBaggageAttrs.length() > TracerUtils.getSysBaggageMaxLength() / 2) {
                SelfLog
                    .infoWithTraceId("Get system baggage from upstream system, and the length is "
                                     + sysBaggageAttrs.length());
            }
        }
    }

    /***
     * 将 SpanContext 序列化为一个字符串,与 {@link SofaTracerSpanContext#deserializeFromString} 互逆
     *
     * @return 序列化后的字符串, 格式:tcid:0,spid:1
     */
    public String serializeSpanContext() {
        StringBuilder serializedValue = new StringBuilder();
        serializedValue.append(TRACE_ID_KET).append(StringUtils.EQUAL).append(traceId)
            .append(StringUtils.AND);
        serializedValue.append(SPAN_ID_KET).append(StringUtils.EQUAL).append(spanId)
            .append(StringUtils.AND);
        serializedValue.append(PARENT_SPAN_ID_KET).append(StringUtils.EQUAL).append(parentId)
            .append(StringUtils.AND);
        serializedValue.append(SAMPLE_KET).append(StringUtils.EQUAL).append(isSampled)
            .append(StringUtils.AND);
        //system bizBaggage
        if (this.sysBaggage.size() > 0) {
            serializedValue.append(StringUtils.mapToStringWithPrefix(this.sysBaggage,
                SYS_BAGGAGE_PREFIX_KEY));
        }
        //bizBaggage
        if (this.bizBaggage.size() > 0) {
            serializedValue.append(StringUtils.mapToString(bizBaggage));
        }
        return serializedValue.toString();
    }

    /***
     * 反序列化并还原一个 SofaTracerSpanContext,与 {@link SofaTracerSpanContext#serializeSpanContext()} 互逆
     * @param deserializedValue 反序列化的字符串,格式:tcid:0,spid:1
     * @return SofaTracerSpanContext
     */
    public static SofaTracerSpanContext deserializeFromString(String deserializedValue) {
        if (StringUtils.isBlank(deserializedValue)) {
            return SofaTracerSpanContext.rootStart();
        }
        //默认值
        String traceId = TraceIdGenerator.generate();
        String spanId = SofaTracer.ROOT_SPAN_ID;
        String parentId = StringUtils.EMPTY_STRING;
        //默认false
        boolean sampled = false;
        //sys bizBaggage
        Map<String, String> sysBaggage = new HashMap<String, String>();
        //bizBaggage
        Map<String, String> baggage = new HashMap<String, String>();

        Map<String, String> spanContext = new HashMap<String, String>();
        StringUtils.stringToMap(deserializedValue, spanContext);

        for (Map.Entry<String, String> entry : spanContext.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
                continue;
            }
            if (TRACE_ID_KET.equals(key)) {
                traceId = value;
                continue;
            }
            if (SPAN_ID_KET.equals(key)) {
                spanId = value;
                continue;
            }
            if (PARENT_SPAN_ID_KET.equals(key)) {
                parentId = value;
                continue;
            }
            if (SAMPLE_KET.equals(key)) {
                sampled = Boolean.parseBoolean(value);
                continue;
            }
            int sysIndex = key.indexOf(SYS_BAGGAGE_PREFIX_KEY);
            if (sysIndex == 0) {
                //必须前缀作为开始
                String sysKey = key.substring(SYS_BAGGAGE_PREFIX_KEY.length());
                sysBaggage.put(sysKey, value);
            } else {
                //剩余全部为业务 bizBaggage
                baggage.put(key, value);
            }
        }
        SofaTracerSpanContext sofaTracerSpanContext = new SofaTracerSpanContext(traceId, spanId,
            parentId, sampled);
        if (sysBaggage.size() > 0) {
            sofaTracerSpanContext.addSysBaggage(sysBaggage);
        }
        if (baggage.size() > 0) {
            sofaTracerSpanContext.addBizBaggage(baggage);
        }
        return sofaTracerSpanContext;
    }

    /****
     * 作为根节点而开始
     *
     * 注意:1.留下这个接口,不对接具体的 tracer 实现,主要为了在序列化或者反序列化发生异常时,做补救
     *     2.不可以随意调用此方法,正确的入口应该是 {@link SofaTracer.SofaTracerSpanBuilder#createRootSpanContext()}
     * @return 根节点
     */
    public static SofaTracerSpanContext rootStart() {
        return rootStart(false);
    }

    public static SofaTracerSpanContext rootStart(boolean isSampled) {
        //生成 traceId
        String traceId = TraceIdGenerator.generate();
        //默认不采样
        return new SofaTracerSpanContext(traceId, SofaTracer.ROOT_SPAN_ID,
            StringUtils.EMPTY_STRING, isSampled);
    }

    private String genParentSpanId(String spanId) {
        return (StringUtils.isBlank(spanId) || spanId.lastIndexOf(RPC_ID_SEPARATOR) < 0) ? StringUtils.EMPTY_STRING
            : spanId.substring(0, spanId.lastIndexOf(RPC_ID_SEPARATOR));
    }

    /***
     * 允许设置 tracdId
     * @param traceId traceId
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /****
     * 允许设置 spanId
     * @param spanId spanId
     */
    public void setSpanId(String spanId) {
        this.spanId = spanId;
        this.parentId = this.genParentSpanId(spanId);
    }

    public SofaTracerSpanContext setBizBaggageItem(String key, String value) {
        if (StringUtils.isBlank(key)) {
            return this;
        }
        this.bizBaggage.put(key, value);
        return this;
    }

    public String getBizBaggageItem(String key) {
        return this.bizBaggage.get(key);
    }

    public SofaTracerSpanContext setSysBaggageItem(String key, String value) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            return this;
        }
        this.sysBaggage.put(key, value);
        return this;
    }

    public String getSysBaggageItem(String key) {
        return this.sysBaggage.get(key);
    }

    public String getTraceId() {
        return StringUtils.isBlank(this.traceId) ? StringUtils.EMPTY_STRING : traceId;
    }

    public String getSpanId() {
        return StringUtils.isBlank(spanId) ? StringUtils.EMPTY_STRING : spanId;
    }

    public String getParentId() {
        return StringUtils.isBlank(parentId) ? StringUtils.EMPTY_STRING : parentId;
    }

    public Map<String, String> getBizBaggage() {
        return bizBaggage;
    }

    public Map<String, String> getSysBaggage() {
        return sysBaggage;
    }

    public boolean isSampled() {
        return isSampled;
    }

    public void setSampled(boolean sampled) {
        isSampled = sampled;
    }

    public AtomicInteger getChildContextIndex() {
        return childContextIndex;
    }

    /**
     * 获取下一个子上下文的 ID
     *
     * @return 下一个 spanId
     */
    public String nextChildContextId() {
        return this.spanId + RPC_ID_SEPARATOR + childContextIndex.incrementAndGet();
    }

    /**
     * 获取上一个子上下文的 rpcId
     *
     * @return 上一个子上下文的 rpcId
     */
    public String lastChildContextId() {
        return this.spanId + RPC_ID_SEPARATOR + childContextIndex.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SofaTracerSpanContext)) {
            return false;
        }

        SofaTracerSpanContext that = (SofaTracerSpanContext) o;

        if (!traceId.equals(that.traceId)) {
            return false;
        }
        if (!spanId.equals(that.spanId)) {
            return false;
        }
        if (StringUtils.isBlank(parentId)) {
            //一个为空
            //另一个也为空
            //另一个不为空
            return StringUtils.isBlank(that.parentId);
        }
        return parentId.equals(that.parentId);
    }

    @Override
    public int hashCode() {
        int result = traceId.hashCode();
        result = 31 * result + spanId.hashCode();
        result = 31 * result + parentId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SofaTracerSpanContext{" + "traceId='" + traceId + '\'' + ", spanId='" + spanId
               + '\'' + ", parentId='" + parentId + '\'' + ", isSampled=" + isSampled
               + ", bizBaggage=" + bizBaggage + ", sysBaggage=" + sysBaggage
               + ", childContextIndex=" + childContextIndex + '}';
    }

}
