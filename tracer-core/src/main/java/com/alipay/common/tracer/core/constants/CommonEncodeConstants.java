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

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/4/3 8:33 PM
 * @since:
 **/
public class CommonEncodeConstants {
    /**
     * SPAN_ID records the current span's id
     */
    public static final String SPAN_ID                 = "spanId";

    /**
     * TRACE_ID records the current span's traceId
     */
    public static final String TRACE_ID                = "traceId";

    /**
     * BAGGAGE records the span's baggage
     */
    public static final String BAGGAGE                 = "baggage";

    /**
     * TIME records the current span's begin time
     */
    public static final String TIME                    = "time";

    /**
     * TIME_COST_MILLISECONDS records the current span's cost time
     */
    public static final String TIME_COST_MILLISECONDS  = "time.cost.milliseconds";

    /**
     * TOTAL_COST_MILLISECONDS records the span's cost time period
     */
    public static final String TOTAL_COST_MILLISECONDS = "total.cost.milliseconds";

    /**
     * STAT_KEY is the span's stat log key for stat.key
     */
    public static final String STAT_KEY                = "stat.key";

    /**
     * COUNT is the span's stat log key for count
     */
    public static final String COUNT                   = "count";

    /**
     * SUCCESS is the span's stat log key for success
     */
    public static final String SUCCESS                 = "success";

    /**
     * LOAD_TEST is the span's stat log key for load.test
     */
    public static final String LOAD_TEST               = "load.test";
}
