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

import java.util.Map;

public final class LogData {

    /***
     * 事件类型关键字 @Nullable cs/cr/ss/sr ,去掉单独字段,使用统一 map 操作
     */
    public static final String   EVENT_TYPE_KEY          = "event";

    /**
     * <b>cr</b> - Client Receive. Signifies the end of the span. The client has
     * successfully received the response from the server side. If one subtracts the cs
     * timestamp from this timestamp one will receive the whole time needed by the client
     * to receive the response from the server.
     */
    public static final String   CLIENT_RECV_EVENT_VALUE = "cr";

    /**
     * <b>cs</b> - Client Sent. The client has made a request (a client can be e.g.
     * This annotation depicts the start of the span.
     */
    public static final String   CLIENT_SEND_EVENT_VALUE = "cs";

    /**
     * <b>sr</b> - Server Receive. The server side got the request and will start
     * processing it. If one subtracts the cs timestamp from this timestamp one will
     * receive the network latency.
     */
    public static final String   SERVER_RECV_EVENT_VALUE = "sr";

    /**
     * <b>ss</b> - Server Send. Annotated upon completion of request processing (when the
     * response got sent back to the client). If one subtracts the sr timestamp from this
     * timestamp one will receive the time needed by the server side to process the
     * request.
     */
    public static final String   SERVER_SEND_EVENT_VALUE = "ss";

    private final long           time;

    /* @Nullable eventName:value*/
    private final Map<String, ?> fields;

    public LogData(long time, Map<String, ?> fields) {
        this.time = time;
        this.fields = fields;
    }

    public long getTime() {
        return time;
    }

    public Map<String, ?> getFields() {
        return fields;
    }
}
