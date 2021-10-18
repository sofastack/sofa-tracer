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

import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.sofa.tracer.plugins.rocketmq.tracers.RocketMQConsumeTracer;
import org.apache.rocketmq.client.hook.ConsumeMessageContext;
import org.apache.rocketmq.client.hook.ConsumeMessageHook;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/12/12 8:22 PM
 * @since:
 **/
public class SofaTracerConsumeMessageHook implements ConsumeMessageHook {

    private final String appName;

    public SofaTracerConsumeMessageHook(String appName) {
        this.appName = appName;
    }

    private static RocketMQConsumeTracer rocketMQConsumeTracer = RocketMQConsumeTracer
                                                                   .getRocketMQConsumeTracerSingleton();

    @Override
    public String hookName() {
        return SofaTracerConsumeMessageHook.class.getCanonicalName();
    }

    @Override
    public void consumeMessageBefore(ConsumeMessageContext context) {
        // ignore
    }

    @Override
    public void consumeMessageAfter(ConsumeMessageContext context) {
        for (MessageExt msg : context.getMsgList()) {
            String sContext = msg.getUserProperty("SOFA_TRACER_CONTEXT");
            if (sContext != null) {
                try {
                    SofaTracerSpanContext spanContext = SofaTracerSpanContext
                        .deserializeFromString(sContext);
                    SofaTracerSpan span = rocketMQConsumeTracer.serverReceive(spanContext);
                    span.setOperationName("mq-message-receive");
                    appendTags(context, msg, span);

                    String resultCode = SofaTracerConstant.RESULT_CODE_SUCCESS;
                    if (!context.isSuccess()) {
                        resultCode = SofaTracerConstant.RESULT_CODE_ERROR;
                    }
                    rocketMQConsumeTracer.serverSend(resultCode);
                } catch (Throwable t) {
                    SelfLog.error("Error to log consume side.", t);
                }
            }
        }
    }

    private void appendTags(ConsumeMessageContext context, MessageExt msg, SofaTracerSpan span) {
        span.setTag(CommonSpanTags.LOCAL_APP, appName);
        String consumerGroup = context.getConsumerGroup();
        MessageQueue mq = context.getMq();
        String topic = mq.getTopic();
        String brokerName = mq.getBrokerName();
        span.setTag("consumerGroup", consumerGroup);
        span.setTag(CommonSpanTags.MSG_TOPIC, topic);
        span.setTag("broker", brokerName);
        span.setTag(CommonSpanTags.MSG_ID, msg.getMsgId());
        span.setTag("status", context.getStatus());
    }
}
