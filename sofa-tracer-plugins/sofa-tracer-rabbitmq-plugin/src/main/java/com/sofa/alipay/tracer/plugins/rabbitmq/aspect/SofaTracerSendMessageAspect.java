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
package com.sofa.alipay.tracer.plugins.rabbitmq.aspect;

import com.sofa.alipay.tracer.plugins.rabbitmq.holder.RabbitMqSofaTracerHolder;
import com.sofa.alipay.tracer.plugins.rabbitmq.tracers.RabbitMQSendTracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;

/**
 *  SofaTracerSendMessageAspect.
 *
 * @author chenchen6  2020/8/22 21:46
 * @since 3.1.0
 */
@Aspect
public class SofaTracerSendMessageAspect {

    private final String           exchange;

    private final String           routingKey;

    private RabbitTemplate         rabbitTemplate;

    private final MessageConverter messageConverter;

    private RabbitMQSendTracer     rabbitMQSendTracer = RabbitMQSendTracer
                                                          .getRabbitMQSendTracerSingleton();

    public SofaTracerSendMessageAspect(String exchange, String routingKey,
                                       MessageConverter messageConverter,
                                       RabbitTemplate rabbitTemplate) {
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.rabbitTemplate = rabbitTemplate;
        this.messageConverter = messageConverter;
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#send(Message)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.send(..)) && args(message)",
            argNames = "pjp,message")
    public Object traceRabbitSend(ProceedingJoinPoint pjp, Object message) throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(this.exchange, this.routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 0));
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#send(String, Message)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.send(..)) && args(routingKey, message)",
            argNames = "pjp,routingKey,message")
    public Object traceRabbitSend(ProceedingJoinPoint pjp, String routingKey, Object message) throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(this.exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 1));
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#send(String, String, Message)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.send(..)) && args(exchange, " +
            "routingKey, message)", argNames = "pjp,exchange, routingKey, message")
    public Object traceRabbitSend(ProceedingJoinPoint pjp, String exchange, String routingKey, Object message)
            throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 2));
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#convertAndSend(Object)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.convertAndSend(..)) " +
            "&& args(message)", argNames = "pjp,message")
    public Object traceRabbitConvertAndSend(ProceedingJoinPoint pjp, Object message) throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(this.exchange, this.routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 0));
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#convertAndSend(String, Object)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.convertAndSend(..)) " +
            "&& args(routingKey, message)", argNames = "pjp,routingKey,message")
    public Object traceRabbitConvertAndSend(
            ProceedingJoinPoint pjp, String routingKey, Object message)
            throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(this.exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 1));
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#convertAndSend(String, String, Object)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.convertAndSend(..)) && args(exchange,"
            + "routingKey, message)", argNames = "pjp,exchange,routingKey,message")
    public Object traceRabbitConvertAndSend(
            ProceedingJoinPoint pjp, String exchange, String routingKey, Object message)
            throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 2));
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#convertAndSend(Object, MessagePostProcessor)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.convertAndSend(..))" +
            " && args(message, messagePostProcessor)", argNames = "pjp,message,messagePostProcessor")
    public Object traceRabbitConvertAndSend(ProceedingJoinPoint pjp, Object message,
                                            MessagePostProcessor messagePostProcessor) throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(this.exchange, this.routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 0));
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#convertAndSend(String, Object, MessagePostProcessor)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.convertAndSend(..))" +
            " && args(routingKey, message, messagePostProcessor)",
            argNames = "pjp,routingKey,message,messagePostProcessor")
    public Object traceRabbitConvertAndSend(ProceedingJoinPoint pjp, String routingKey, Object message,
                                            MessagePostProcessor messagePostProcessor) throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(this.exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 1));
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#convertAndSend(String, String, Object, MessagePostProcessor)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.convertAndSend(..))" +
            " && args(exchange, routingKey, message, messagePostProcessor)",
            argNames = "pjp,exchange,routingKey,message,messagePostProcessor")
    public Object traceRabbitConvertAndSend(ProceedingJoinPoint pjp,String exchange, String routingKey, Object message,
                                            MessagePostProcessor messagePostProcessor) throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 2));
    }

    /**
     * @see RabbitTemplate#convertAndSend(String, Object, MessagePostProcessor, CorrelationData)
     */
    @Around(value = "execution(* org.springframework.amqp.rabbit.core.RabbitTemplate.convertAndSend(..)) " +
            "&& args(routingKey, message, messagePostProcessor, correlationData)",
            argNames = "pjp,routingKey,message,messagePostProcessor,correlationData")
    public Object traceRabbitConvertAndSend(ProceedingJoinPoint pjp, String routingKey, Object message,
                                            MessagePostProcessor messagePostProcessor, CorrelationData correlationData)
            throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(this.exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 1));
    }

    /**
     * @see RabbitTemplate#convertAndSend(String, String, Object, CorrelationData)
     */
    @Around(value = "execution(* org.springframework.amqp.rabbit.core.RabbitTemplate.convertAndSend(..)) " +
            "&& args(exchange, routingKey, message, correlationData)",
            argNames = "pjp,exchange,routingKey,message,correlationData")
    public Object traceRabbitConvertAndSend(ProceedingJoinPoint pjp, String exchange, String routingKey, Object message,
                                            CorrelationData correlationData)
            throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 2));
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#sendAndReceive(Message)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.sendAndReceive(..))" +
            " && args(message)", argNames = "pjp,message")
    public Object traceRabbitSendAndReceive(ProceedingJoinPoint pjp, Object message) throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(this.exchange, this.routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 0));
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#sendAndReceive(String, Message)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.sendAndReceive(..))" +
            " && args(routingKey, message)", argNames = "pjp,routingKey,message")
    public Object traceRabbitSendAndReceive(ProceedingJoinPoint pjp, String routingKey, Object message) throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 1));
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#sendAndReceive(String, String, Message)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.sendAndReceive(..))" +
            " && args(exchange, routingKey, message)", argNames = "pjp,exchange,routingKey,message")
    public Object traceRabbitSendAndReceive(ProceedingJoinPoint pjp, String exchange, String routingKey, Object message)
            throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 2));
    }

    // Intercept public methods that eventually delegate to RabbitTemplate.doSendAndReceive
    // that can't be intercepted with AspectJ as it is protected.
    /**
     * @see RabbitTemplate#sendAndReceive(String, String, Message, CorrelationData)
     */
    @Around(value = "execution(* org.springframework.amqp.rabbit.core.RabbitTemplate.sendAndReceive(..)) && args(exchange,"
            + "routingKey, message, correlationData)", argNames = "pjp,exchange,routingKey,message,correlationData")
    public Object traceRabbitSendAndReceive(
            ProceedingJoinPoint pjp, String exchange, String routingKey, Message message, CorrelationData correlationData)
            throws Throwable {
        return createSofaTracerHolder()
                .nullResponseMeansTimeout((RabbitTemplate) pjp.getTarget())
                .doWithTracingHeadersMessage(exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 2));
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#convertSendAndReceive(Object)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.convertSendAndReceive(..))" +
            " && args(message)", argNames = "pjp,message")
    public Object traceRabbitConvertSendAndReceive(ProceedingJoinPoint pjp, Object message)
            throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 0));
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#convertSendAndReceive(String, Object)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.convertSendAndReceive(..))" +
            " && args(routingKey, message)", argNames = "pjp,routingKey,message")
    public Object traceRabbitConvertSendAndReceive(ProceedingJoinPoint pjp, String routingKey, Object message)
            throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 1));
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#convertSendAndReceive(String, String, Object)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.convertSendAndReceive(..))" +
            " && args(exchange, routingKey, message)", argNames = "pjp,exchange,routingKey,message")
    public Object traceRabbitConvertSendAndReceive(ProceedingJoinPoint pjp, String exchange, String routingKey,
                                                   Object message)
            throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 2));
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#convertSendAndReceive(Object, MessagePostProcessor)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.convertSendAndReceive(..))" +
            " && args(message, messagePostProcessor)", argNames = "pjp,message,messagePostProcessor")
    public Object traceRabbitConvertSendAndReceive(ProceedingJoinPoint pjp, Object message,
                                                   MessagePostProcessor messagePostProcessor)
            throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 0));
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#convertSendAndReceive(String, Object, MessagePostProcessor)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.convertSendAndReceive(..))" +
            " && args(routingKey, message, messagePostProcessor)",
            argNames = "pjp,routingKey,message,messagePostProcessor")
    public Object traceRabbitConvertSendAndReceive(ProceedingJoinPoint pjp, String routingKey, Object message,
                                                   MessagePostProcessor messagePostProcessor)
            throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 1));
    }

    /**
     * @see org.springframework.amqp.core.AmqpTemplate#convertSendAndReceive(String, String, Object, MessagePostProcessor)
     */
    @Around(value = "execution(* org.springframework.amqp.core.AmqpTemplate.convertSendAndReceive(..))" +
            " && args(exchange, routingKey, message, messagePostProcessor)",
            argNames = "pjp,exchange,routingKey,message,messagePostProcessor")
    public Object traceRabbitConvertSendAndReceive(ProceedingJoinPoint pjp, String exchange, String routingKey,
                                                   Object message, MessagePostProcessor messagePostProcessor)
            throws Throwable {
        return createSofaTracerHolder()
                .doWithTracingHeadersMessage(exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 2));
    }

    /**
     * @see RabbitTemplate#convertSendAndReceive(String, String, Object, MessagePostProcessor, CorrelationData)
     */
    @Around(value = "execution(* org.springframework.amqp.rabbit.core.RabbitTemplate.convertSendAndReceive(..)) " +
            "&& args(exchange, routingKey, message, messagePostProcessor, correlationData)",
            argNames = "pjp,exchange,routingKey,message,messagePostProcessor,correlationData")
    public Object traceRabbitConvertSendAndReceive(
            ProceedingJoinPoint pjp, String exchange, String routingKey, Object message,
            MessagePostProcessor messagePostProcessor, CorrelationData correlationData)
            throws Throwable {
        return createSofaTracerHolder()
                .nullResponseMeansTimeout((RabbitTemplate) pjp.getTarget())
                .doWithTracingHeadersMessage(exchange, routingKey, message, (convertedMessage) ->
                        proceedReplacingMessage(pjp, convertedMessage, 2));
    }

    private RabbitMqSofaTracerHolder createSofaTracerHolder() {
        return new RabbitMqSofaTracerHolder(rabbitMQSendTracer, messageConverter, rabbitTemplate);
    }

    private Object proceedReplacingMessage(ProceedingJoinPoint pjp, Message convertedMessage,
                                           int messageArgumentIndex) throws Throwable {
        final Object[] args = pjp.getArgs();
        args[messageArgumentIndex] = convertedMessage;
        return pjp.proceed(args);
    }
}
