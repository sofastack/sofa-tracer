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
package com.sofa.tracer.plugins.rabbitmq.base;

import com.alipay.common.tracer.core.SofaTracer;
import com.sofa.alipay.tracer.plugins.rabbitmq.aspect.SofaTracerSendMessageAspect;
import io.opentracing.Span;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * MockSofaTracerConsumeInterceptor.
 *
 * @author chenchen6  2020/8/12 02:22
 */
@Import(value = { MockSofaTracerConfiguration.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class MockSofaTracerSendMessageAspect {

    private static final String         ROUTING_KEY = "sofa.tracer.rounting.key";

    private SofaTracerSendMessageAspect aspect;

    @Autowired
    private SofaTracer                  mockSofaTracer;

    @Mock
    private ProceedingJoinPoint         proceedingJoinPoint;

    @Mock
    private MessageConverter            messageConverter;

    private final String                exchange    = "sofa-tracer.exchange";

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        aspect = new SofaTracerSendMessageAspect(exchange, ROUTING_KEY, messageConverter, null);
    }

    @After
    public void tearDown() {
        //ignore.
    }

    @Test
    public void testTraceRabbitSend_whenNoPropertiesHeaders() throws Throwable {
        Span span = mockSofaTracer.buildSpan("testSpan").start();
        Assert.assertNotNull(span);
        mockSofaTracer.buildSpan("testSpan");
        TestMySelfMessage<String> testMessage = new TestMySelfMessage<>("testMessage");

        Object[] args = new Object[] { exchange, ROUTING_KEY, testMessage };
        given(proceedingJoinPoint.getArgs()).willReturn(args);

        MessageProperties pro = new MessageProperties();
        pro.setReceivedExchange("testExchange");
        pro.setReceivedRoutingKey("testRoutingKey");
        pro.setMessageId("testMessageId");
        Message message = new Message("".getBytes(), pro);
        given(messageConverter.toMessage(any(Object.class), any(MessageProperties.class)))
            .willReturn(message);
        //com.sofa.alipay.tracer.plugins.rabbitmq.aspect.SofaTracerSendMessageAspect.traceRabbitSend(org.aspectj.lang.ProceedingJoinPoint, java.lang.String, java.lang.String, java.lang.Object)
        aspect.traceRabbitSend(proceedingJoinPoint, exchange, ROUTING_KEY, testMessage);
        verify(proceedingJoinPoint).getArgs();
        verify(messageConverter).toMessage(any(Object.class), any(MessageProperties.class));
        verify(proceedingJoinPoint).proceed(args);
    }

    @Test
    public void testTraceRabbitSend_whenNoConversionIsNeeded() throws Throwable {
        MessageProperties properties = new MessageProperties();
        Message message = new Message("".getBytes(), properties);
        Object[] args = new Object[] { exchange, ROUTING_KEY, message };
        given(proceedingJoinPoint.getArgs()).willReturn(args);

        given(messageConverter.toMessage(any(Object.class), any(MessageProperties.class)))
            .willReturn(message);
        // com.sofa.alipay.tracer.plugins.rabbitmq.aspect.SofaTracerSendMessageAspect.traceRabbitSend(org.aspectj.lang.ProceedingJoinPoint, java.lang.String, java.lang.String, java.lang.Object)
        aspect.traceRabbitSend(proceedingJoinPoint, exchange, ROUTING_KEY, message);
        verify(proceedingJoinPoint).getArgs();
        verify(proceedingJoinPoint).proceed(args);
    }

    @Test(expected = Throwable.class)
    public void testTraceRabbitSend_whenException() throws Throwable {
        MessageProperties properties = new MessageProperties();
        Message message = new Message("".getBytes(), properties);
        Object[] args = new Object[] { exchange, ROUTING_KEY, message };
        given(proceedingJoinPoint.getArgs()).willReturn(args);
        given(messageConverter.toMessage(any(Object.class), any(MessageProperties.class)))
            .willReturn(message);
        given(proceedingJoinPoint.proceed(args)).willThrow(new RuntimeException());
        try {
            aspect.traceRabbitSend(proceedingJoinPoint, exchange, ROUTING_KEY, message);
        } catch (Throwable t) {
            verify(proceedingJoinPoint).getArgs();
            verify(proceedingJoinPoint).proceed(args);
            throw t;
        }
    }

    class TestMySelfMessage<T> {

        private T body;

        TestMySelfMessage(T body) {
            this.body = body;
        }

    }
}
