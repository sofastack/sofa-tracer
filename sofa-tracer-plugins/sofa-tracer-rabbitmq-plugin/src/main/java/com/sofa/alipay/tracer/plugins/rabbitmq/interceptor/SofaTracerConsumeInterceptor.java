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
package com.sofa.alipay.tracer.plugins.rabbitmq.interceptor;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.registry.ExtendFormat;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.sofa.alipay.tracer.plugins.rabbitmq.carrier.RabbitMqExtractCarrier;
import com.sofa.alipay.tracer.plugins.rabbitmq.tracers.RabbitMQConsumeTracer;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.aop.AfterAdvice;
import org.springframework.aop.BeforeAdvice;

import java.util.Map;

/**
 *  SofaTracerConsumeInterceptor.
 *
 * @author chenchen6 2020/8/19 20:44
 * @since 3.1.0
 */
public class SofaTracerConsumeInterceptor implements MethodInterceptor, AfterAdvice, BeforeAdvice {

    private RabbitMQConsumeTracer rabbitMQConsumeTracer;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if (null == rabbitMQConsumeTracer) {
            rabbitMQConsumeTracer = RabbitMQConsumeTracer.getRabbitMQSendTracerSingleton();
        }
        Message message = (Message) methodInvocation.getArguments()[1];
        MessageProperties messageProperties = message.getMessageProperties();
        SofaTracerSpan rabbitMQSendSpan = null;
        String resultCode = SofaTracerConstant.RESULT_CODE_SUCCESS;
        SofaTracerSpanContext spanContext = getSpanContextFromHeaders(messageProperties
            .getHeaders());
        // sr
        rabbitMQSendSpan = rabbitMQConsumeTracer.serverReceive(spanContext);
        appendRequestSpanTags(rabbitMQSendSpan, messageProperties);
        try {
            return methodInvocation.proceed();
        } catch (Throwable e) {
            resultCode = SofaTracerConstant.RESULT_CODE_ERROR;
            throw new RuntimeException(e);
        } finally {
            rabbitMQConsumeTracer.serverSend(resultCode);
        }
    }

    private void appendRequestSpanTags(SofaTracerSpan tracerSpan,
                                       MessageProperties messageProperties) {
        // append some tags/
        tracerSpan.setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread().getName());
        tracerSpan.setTag(CommonSpanTags.LOCAL_APP,
            SofaTracerConfiguration.getProperty(SofaTracerConfiguration.TRACER_APPNAME_KEY));
        tracerSpan.setTag(CommonSpanTags.RABBIT_EXCHANGE, messageProperties.getReceivedExchange());
        tracerSpan.setTag(CommonSpanTags.RABBIT_ROUNTING_KEY,
            messageProperties.getReceivedRoutingKey());
        tracerSpan.setTag(CommonSpanTags.RABBIT_QUEUE_NAME, messageProperties.getConsumerQueue());
    }

    private SofaTracerSpanContext getSpanContextFromHeaders(Map<String, Object> headers) {

        SofaTracer sofaTracer = this.rabbitMQConsumeTracer.getSofaTracer();
        SofaTracerSpanContext spanContext = (SofaTracerSpanContext) sofaTracer.extract(
            ExtendFormat.Builtin.B3_TEXT_MAP, new RabbitMqExtractCarrier(headers));
        return spanContext;
    }
}
