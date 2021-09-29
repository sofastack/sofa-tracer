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
package com.alipay.sofa.tracer.plugins.rocketmq.interceptor;

import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.sofa.tracer.plugins.rocketmq.tracers.RocketMQSendTracer;
import io.opentracing.tag.Tags;
import org.apache.rocketmq.client.hook.SendMessageContext;
import org.apache.rocketmq.client.hook.SendMessageHook;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/12/12 8:22 PM
 * @since:
 **/
public class SofaTracerSendMessageHook implements SendMessageHook {

    private final String                                   appName;

    private static Map<SendMessageContext, SofaTracerSpan> TracerSpanMap = new ConcurrentHashMap<SendMessageContext, SofaTracerSpan>();

    public SofaTracerSendMessageHook(String appName) {
        this.appName = appName;
    }

    private static RocketMQSendTracer rocketMQSendTracer = RocketMQSendTracer
                                                             .getRocketMQSendTracerSingleton();

    @Override
    public String hookName() {
        return SofaTracerSendMessageHook.class.getCanonicalName();
    }

    @Override
    public void sendMessageBefore(SendMessageContext context) {
        SofaTracerSpan span = rocketMQSendTracer.clientSend("mq-message-send");
        String[] remote = context.getBrokerAddr().split(":");
        span.getSofaTracerSpanContext().setPeer(remote[0] + ":" + remote[1]);
        // put spanContext to message
        context.getMessage().putUserProperty("SOFA_TRACER_CONTEXT",
            span.getSofaTracerSpanContext().serializeSpanContext());
        SofaTracerSpan current = SofaTraceContextHolder.getSofaTraceContext().pop();
        // reset parent span
        if (current.getParentSofaTracerSpan() != null) {
            SofaTraceContextHolder.getSofaTraceContext().push(current.getParentSofaTracerSpan());
        }
        TracerSpanMap.put(context, current);
    }

    private void appendTags(SendMessageContext context, SofaTracerSpan span) {
        span.setTag(CommonSpanTags.LOCAL_APP, appName);
        MessageType msgType = context.getMsgType();
        Message message = context.getMessage();
        SendResult sendResult = context.getSendResult();
        String[] remote = context.getBrokerAddr().split(":");
        span.setTag("msgType", msgType.name());
        span.setTag("bornHost", context.getBornHost());
        span.setTag("brokerAddr", context.getBrokerAddr());
        span.setTag(CommonSpanTags.REMOTE_HOST, remote[0]);
        span.setTag(CommonSpanTags.REMOTE_PORT, remote[1]);
        span.setTag("producerGroup", context.getProducerGroup());
        span.setTag(CommonSpanTags.MSG_TOPIC, message.getTopic());
        span.setTag(CommonSpanTags.MSG_ID, sendResult.getMsgId());
        span.setTag("status", sendResult.getSendStatus().name());
        span.setTag("broker", context.getMq().getBrokerName());
        if (context.getException() != null) {
            span.setTag(Tags.ERROR.getKey(), context.getException().getMessage());
        }
    }

    @Override
    public void sendMessageAfter(SendMessageContext context) {
        SofaTracerSpan sofaTracerSpan = TracerSpanMap.remove(context);
        if (sofaTracerSpan == null) {
            return;
        }
        String resultCode = SofaTracerConstant.RESULT_CODE_SUCCESS;
        try {
            appendTags(context, sofaTracerSpan);
            if (context.getSendResult().getSendStatus() != SendStatus.SEND_OK) {
                resultCode = SofaTracerConstant.RESULT_CODE_ERROR;
            }
        } catch (Throwable t) {
            resultCode = SofaTracerConstant.RESULT_CODE_ERROR;
        }
        rocketMQSendTracer.clientReceiveTagFinish(sofaTracerSpan, resultCode);
    }
}
