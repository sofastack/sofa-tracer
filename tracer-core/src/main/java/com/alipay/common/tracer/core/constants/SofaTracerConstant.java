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
package com.alipay.common.tracer.core.constants;

import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;

import java.nio.charset.Charset;

/**
 * SofaTracerConstant
 *
 * @author yangguanchao
 * @since 2017/06/19
 */
public class SofaTracerConstant {

    /**
     * Span tag key to describe the type of sampler used on the root span.
     */
    public static final String  SAMPLER_TYPE_TAG_KEY      = "sampler.type";

    /**
     * Span tag key to describe the parameter of the sampler used on the root span.
     */
    public static final String  SAMPLER_PARAM_TAG_KEY     = "sampler.param";

    public static final String  DEFAULT_UTF8_ENCODING     = "UTF-8";

    public static final Charset DEFAULT_UTF8_CHARSET      = Charset.forName(DEFAULT_UTF8_ENCODING);

    public static final String  RPC_2_JVM_DIGEST_LOG_NAME = "rpc-2-jvm-digest.log";

    //******************* span encoder constant start *****************

    /**
     * 耗时单位
     */
    public static final String  MS                        = "ms";

    /***
     * 字节单位
     */
    public static final String  BYTE                      = "B";

    /**
     * Tracer 上下文嵌套的最大深度
     */
    public static final int     MAX_LAYER                 = 100;

    //******************* span encoder constant end *****************

    //******************* exception constant start *****************

    /***
     * 业务异常
     */
    public static final String  BIZ_ERROR                 = "biz_error";

    //******************* exception constant end *****************

    /***
     * ============= baggage key start ==============
     */
    /**
     * 必须保持一致,baggage key 压测标识
     */
    public static final String  LOAD_TEST_TAG             = "mark";

    /***
     * 压测标识必须为 T 即 baggage 中 mark=T 才可以打印到 shdow 文件
     */
    public static final String  LOAD_TEST_VALUE           = "T";

    /***
     * 非压测情况下的返回值 {@link AbstractSofaTracerStatisticReporter}
     */
    public static final String  NON_LOAD_TEST_VALUE       = "F";
}
