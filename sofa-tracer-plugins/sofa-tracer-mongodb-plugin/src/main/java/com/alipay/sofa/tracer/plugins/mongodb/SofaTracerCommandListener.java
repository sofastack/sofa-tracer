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
package com.alipay.sofa.tracer.plugins.mongodb;

import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.NetUtils;
import com.alipay.sofa.tracer.plugins.mongodb.tracers.MongoClientTracer;
import com.mongodb.ServerAddress;
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;
import io.opentracing.tag.Tags;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.alipay.common.tracer.core.constants.SofaTracerConstant.RESULT_CODE_ERROR;
import static com.alipay.common.tracer.core.constants.SofaTracerConstant.RESULT_CODE_SUCCESS;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/12/6 11:16 AM
 * @since:
 **/
public class SofaTracerCommandListener implements CommandListener {

    public static final String                 COMPONENT_NAME = "mongodb";

    private final MongoClientTracer            mongoClientTracer;

    private final String                       applicationName;
    /**
     * Cache for (request id, span) pairs
     */
    private final Map<Integer, SofaTracerSpan> cache          = new ConcurrentHashMap<>();

    public SofaTracerCommandListener(String applicationName) {
        this.mongoClientTracer = MongoClientTracer.getMongoClientTracerSingleton();
        this.applicationName = applicationName;
    }

    @Override
    public void commandStarted(CommandStartedEvent event) {
        buildSpan(event);
        SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
        SofaTracerSpan sofaTracerSpan = sofaTraceContext.pop();
        if (sofaTracerSpan == null) {
            return;
        }
        if (sofaTracerSpan.getParentSofaTracerSpan() != null) {
            sofaTraceContext.push(sofaTracerSpan.getParentSofaTracerSpan());
        }
        if (sofaTracerSpan != null) {
            cache.put(event.getRequestId(), sofaTracerSpan);
        }
    }

    @Override
    public void commandSucceeded(CommandSucceededEvent event) {
        SofaTracerSpan span = cache.remove(event.getRequestId());
        if (span != null) {
            finishSpan(span, null);
        }
    }

    @Override
    public void commandFailed(CommandFailedEvent event) {
        SofaTracerSpan span = cache.remove(event.getRequestId());
        if (span != null) {
            finishSpan(span, event.getThrowable());
        }
    }

    private SofaTracerSpan buildSpan(CommandStartedEvent event) {
        SofaTracerSpan sofaTracerSpan = mongoClientTracer.clientSend(event.getCommandName());
        decorate(sofaTracerSpan, event);
        return sofaTracerSpan;
    }

    private void finishSpan(SofaTracerSpan sofaTracerSpan, Throwable throwable) {
        if (sofaTracerSpan == null) {
            return;
        }
        String resultCode = RESULT_CODE_SUCCESS;
        if (throwable != null) {
            resultCode = RESULT_CODE_ERROR;
            String message = throwable.getMessage();
            if (message == null) {
                message = throwable.getClass().getName();
            }
            sofaTracerSpan.setTag(Tags.ERROR.getKey(), message);
            sofaTracerSpan.log(errorLogs(throwable));
        }
        mongoClientTracer.clientReceiveTagFinish(sofaTracerSpan, resultCode);
    }

    private void decorate(SofaTracerSpan span, CommandStartedEvent event) {
        String command = event.getCommandName();
        String host = event.getConnectionDescription().getServerAddress().getHost();
        InetAddress hostAddress = NetUtils.getIpAddress(host);
        span.setTag(Tags.COMPONENT.getKey(), COMPONENT_NAME);
        span.setTag(Tags.DB_STATEMENT.getKey(), event.getCommand().toString());
        span.setTag(Tags.DB_INSTANCE.getKey(), event.getDatabaseName());
        span.setTag(Tags.PEER_HOSTNAME.getKey(),
            hostAddress == null ? host : hostAddress.getHostAddress());
        InetSocketAddress address = event.getConnectionDescription().getServerAddress()
            .getSocketAddress();
        if (address != null) {
            span.setTag("peer.host", address.toString());
        }
        span.setTag(Tags.PEER_PORT.getKey(), event.getConnectionDescription().getServerAddress()
            .getPort());
        ServerAddress serverAddress = event.getConnectionDescription().getServerAddress();
        span.getSofaTracerSpanContext().setPeer(
            serverAddress.getHost() + ":" + serverAddress.getPort());
        span.setTag(Tags.DB_TYPE.getKey(), "mongodb");
        span.setTag(CommonSpanTags.METHOD, command);
        span.setTag(CommonSpanTags.LOCAL_APP, applicationName);
    }

    private Map<String, Object> errorLogs(Throwable throwable) {
        Map<String, Object> errorLogs = new HashMap<>(4);
        errorLogs.put("event", Tags.ERROR.getKey());
        errorLogs.put("error.kind", throwable.getClass().getName());
        errorLogs.put("error.object", throwable);
        errorLogs.put("message", throwable.getMessage());
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        errorLogs.put("stack", sw.toString());
        return errorLogs;
    }
}
