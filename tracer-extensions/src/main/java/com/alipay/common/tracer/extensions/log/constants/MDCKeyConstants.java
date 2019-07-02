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
package com.alipay.common.tracer.extensions.log.constants;

/**
 * 负责管理MDC中需要存入的key值常量
 *
 * @author luoguimu123
 * @version $Id: MDCKeyConstants.java, v 0.1 2017年08月07日 上午8:44 luoguimu123 Exp $
 */
public class MDCKeyConstants {

    public static final String MDC_TRACEID      = "SOFA-TraceId";

    public static final String MDC_SPANID       = "SOFA-SpanId";

    public static final String MDC_PARENTSPANID = "SOFA-ParentSpanId";

}