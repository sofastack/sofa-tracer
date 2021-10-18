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
package com.sofa.alipay.tracer.plugins.rabbitmq.holder;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.registry.ExtendFormat;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.StringUtils;
import com.sofa.alipay.tracer.plugins.rabbitmq.carrier.RabbitMqInjectCarrier;
import com.sofa.alipay.tracer.plugins.rabbitmq.tracers.RabbitMQSendTracer;
import io.opentracing.tag.Tags;
import org.springframework.amqp.core.Address;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 *  RabbitMqSofaTracerHolder.
 * @author chenchen6 2020/8/19 20:44
 * @since 3.1.0
 */
public class RabbitMqSofaTracerHolder {
    private final String           rabbitSendPostFix   = "-rabbit-send";

    private static final Field     FIELD_REPLY_TIMEOUT = ReflectionUtils.findField(
                                                           RabbitTemplate.class, "replyTimeout");

    static {
        FIELD_REPLY_TIMEOUT.setAccessible(true);
    }

    private RabbitMQSendTracer     rabbitMQSendTracer;
    private final MessageConverter messageConverter;
    private boolean                nullResponseMeansError;
    private RabbitTemplate         rabbitTemplate;

    public RabbitMqSofaTracerHolder nullResponseMeansTimeout(RabbitTemplate rabbitTemplate) {
        this.nullResponseMeansError = true;
        this.rabbitTemplate = rabbitTemplate;
        return this;
    }

    public RabbitMqSofaTracerHolder(RabbitMQSendTracer rabbitMQSendTracer,
                                    MessageConverter messageConverter, RabbitTemplate rabbitTemplate) {
        this.rabbitMQSendTracer = rabbitMQSendTracer;
        this.messageConverter = messageConverter;
        this.rabbitTemplate = rabbitTemplate;
    }

    public <T> T doWithTracingHeadersMessage(String exchange, String routingKey, Object message,
                                             ProceedFunction<T> proceedCallback) throws Throwable {
        T resp = null;
        String operationName = Thread.currentThread().getStackTrace()[1].getMethodName();
        operationName = operationName + rabbitSendPostFix;
        String resultCode = StringUtils.EMPTY_STRING;
        Message convertedMessage = convertMessageIfNecessary(message);
        SofaTracerSpan tracerSpan = null;
        Throwable t = null;
        long replyTimeout = 0;
        try {
            tracerSpan = rabbitMQSendTracer.clientSend(operationName);
            appendRequestSpanTagsAndInject(tracerSpan, exchange, routingKey, convertedMessage);
            resp = proceedCallback.apply(convertedMessage);
            // amq.rabbitmq.reply-to
            if (routingKey != null && routingKey.startsWith(Address.AMQ_RABBITMQ_REPLY_TO + ".")) {
                resultCode = SofaTracerConstant.RESULT_SUCCESS;
                return resp;
            }
            // null is error.
            if (nullResponseMeansError && null == resp) {
                resultCode = SofaTracerConstant.RESULT_CODE_ERROR;
                replyTimeout = (long) ReflectionUtils.getField(FIELD_REPLY_TIMEOUT, rabbitTemplate);
            }
            // no return value. null == resp or others.
            resultCode = SofaTracerConstant.RESULT_CODE_SUCCESS;
        } catch (Throwable e) {
            t = e;
            resultCode = SofaTracerConstant.RESULT_CODE_ERROR;
            throw new RuntimeException(e);
        } finally {
            SofaTraceContext sofaTraceContext = SofaTraceContextHolder.getSofaTraceContext();
            SofaTracerSpan currentSpan = sofaTraceContext.getCurrentSpan();
            // is get error
            if (t != null) {
                currentSpan.setTag(Tags.ERROR.getKey(), t.getMessage());
            }
            if (0 != replyTimeout) {
                currentSpan.setTag(CommonSpanTags.RABBIT_REPLY_TIME_OUT, replyTimeout);
            }
            rabbitMQSendTracer.clientReceive(resultCode);
        }
        return resp;
    }

    private Message convertMessageIfNecessary(final Object object) {
        if (object instanceof Message) {
            return (Message) object;
        }
        return messageConverter.toMessage(object, new MessageProperties());
    }

    private void appendRequestSpanTagsAndInject(SofaTracerSpan tracerSpan, String exchange,
                                                String routingKey, Message convertedMessage) {
        MessageProperties properties = convertedMessage.getMessageProperties();
        // append tags
        tracerSpan.setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread().getName());
        tracerSpan.setTag(CommonSpanTags.LOCAL_APP,
            SofaTracerConfiguration.getProperty(SofaTracerConfiguration.TRACER_APPNAME_KEY));
        tracerSpan.setTag(CommonSpanTags.RABBIT_EXCHANGE, exchange);
        tracerSpan.setTag(CommonSpanTags.RABBIT_ROUNTING_KEY, routingKey);
        // inject
        injectCarrier(tracerSpan, properties);
    }

    //inject headers.
    private void injectCarrier(SofaTracerSpan tracerSpan, MessageProperties properties) {
        SofaTracer sofaTracer = this.rabbitMQSendTracer.getSofaTracer();
        sofaTracer.inject(tracerSpan.getSofaTracerSpanContext(), ExtendFormat.Builtin.B3_TEXT_MAP,
            new RabbitMqInjectCarrier(properties));
    }

    public interface ProceedFunction<T> {
        T apply(Message t) throws Throwable;
    }
}
