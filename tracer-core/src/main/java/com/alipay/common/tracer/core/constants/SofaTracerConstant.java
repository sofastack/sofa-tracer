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

    /**
     * Time-consuming unit
     */
    public static final String  MS                        = "ms";

    /**
     * Byte unit
     */
    public static final String  BYTE                      = "B";

    /**
     * Maximum depth of the Tracer context nesting
     */
    public static final int     MAX_LAYER                 = 100;

    //******************* span encoder constant end *****************

    //******************* exception constant start *****************
    /**
     * Business exception
     */
    public static final String  BIZ_ERROR                 = "biz_error";

    //******************* exception constant end *****************

    //******************* baggage key start *****************
    /**
     * Must be consistent, baggage key for pressure measurement mark
     */
    public static final String  LOAD_TEST_TAG             = "mark";
    /**
     * The pressure measurement mark must be T, ie mark=T in baggage, to print to the shadow file.
     */
    public static final String  LOAD_TEST_VALUE           = "T";

    /**
     * Return value in case of non-pressure measurement {@link AbstractSofaTracerStatisticReporter}
     */
    public static final String  NON_LOAD_TEST_VALUE       = "F";

    /**
     * Result code for time out
     */
    public static final String  RESULT_CODE_TIME_OUT      = "03";
    /**
     * Result code for success
     */
    public static final String  RESULT_CODE_SUCCESS       = "00";
    /**
     * Result code for failure
     */
    public static final String  RESULT_CODE_ERROR         = "99";
    /**
     * Result state for success
     */
    public static final String  RESULT_SUCCESS            = "success";
    /**
     * Result state for failed
     */
    public static final String  RESULT_FAILED             = "failed";
    /**
     * digest result state for success
     */
    public static final String  DIGEST_FLAG_SUCCESS       = "Y";
    /**
     * digest result state for failure
     */
    public static final String  DIGEST_FLAG_FAILS         = "N";
    /**
     * stat result state for success
     */
    public static final String  STAT_FLAG_SUCCESS         = DIGEST_FLAG_SUCCESS;
    /**
     * stat result state for failure
     */
    public static final String  STAT_FLAG_FAILS           = DIGEST_FLAG_FAILS;

    public static final String  SPACE_ID                  = "sofa-tracer";
}
