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
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.registry.AbstractTextB3Formatter;
import com.alipay.common.tracer.core.registry.ExtendFormat;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.sofa.alipay.tracer.plugins.rabbitmq.carrier.RabbitMqExtractCarrier;
import com.sofa.alipay.tracer.plugins.rabbitmq.interceptor.SofaTracerConsumeInterceptor;
import com.sofa.alipay.tracer.plugins.rabbitmq.tracers.RabbitMQConsumeTracer;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MockSofaTracerConsumeInterceptor.
 *
 * @author chenchen6  2020/8/12 03:22
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { MockSofaTracerConfiguration.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class MockSofaTracerConsumeInterceptor {

    private String     sendTraceId    = "sendTraceId0000001";

    private String     sendSpanId     = "0.1";

    private String     consumeTraceId = sendTraceId;

    private String     consumeSpanId  = "0.1.1";

    @Autowired
    private SofaTracer mockSofaTracer;

    /**
     * test sofa rabbit send and consume.
     * @throws Throwable
     */
    @Test
    public void testSofaRabbitSendAndConsume() throws Throwable {
        //get interceptor.
        SofaTracerConsumeInterceptor interceptor = new SofaTracerConsumeInterceptor();
        //construct method invocation.
        MethodInvocation4CurrentTest invocation = new MethodInvocation4CurrentTest();
        //get message.
        Message message = (Message) invocation.getArguments()[1];
        //extract
        SofaTracerSpanContext context = getSpanContextFromHeaders(message.getMessageProperties()
            .getHeaders(), mockSofaTracer);
        //invoke current invocation.
        interceptor.invoke(invocation);
        //assert trace id and span id.
        assertSofaTracerIdAndSpanId(consumeTraceId, consumeSpanId, context);
    }

    /**
     *
     *  assert sofa trace id and span id.
     *
     * @param traceId
     * @param spanId
     * @param context
     */
    private void assertSofaTracerIdAndSpanId(String traceId, String spanId,
                                             SofaTracerSpanContext context) {
        RabbitMQConsumeTracer consumeTracer = RabbitMQConsumeTracer
            .getRabbitMQSendTracerSingleton();
        //sr.
        SofaTracerSpan serverSpan = consumeTracer.serverReceive(context);
        SofaTracerSpanContext serverContext = (SofaTracerSpanContext) serverSpan.context();

        //assert
        assertThat(serverContext.getTraceId()).isEqualTo(traceId);
        assertThat(serverContext.getSpanId()).isEqualTo(spanId);
    }

    /**
     * construct message and add some amqp headers by sofa trace id and spanId.
     * @return
     */
    private Message getConstructMessageWithContext() {
        Message message = getMessage();
        MessageProperties messageProperties = message.getMessageProperties();
        messageProperties.setHeader(AbstractTextB3Formatter.TRACE_ID_KEY_HEAD, sendTraceId);
        messageProperties.setHeader(AbstractTextB3Formatter.SPAN_ID_KEY_HEAD, sendSpanId);
        return new Message("".getBytes(Charset.defaultCharset()), messageProperties);
    }

    /**
     * construct easy message.
     * @return
     */
    private Message getMessage() {
        final MessageProperties properties = new MessageProperties();
        properties.setReceivedExchange("sofaTracerTestExchange");
        properties.setReceivedRoutingKey("sofaTracerTestRoutingKey");
        return new Message("".getBytes(Charset.defaultCharset()), properties);
    }

    private SofaTracerSpanContext getSpanContextFromHeaders(Map<String, Object> headers,
                                                            SofaTracer tracer) {

        SofaTracerSpanContext spanContext = (SofaTracerSpanContext) tracer.extract(
            ExtendFormat.Builtin.B3_TEXT_MAP, new RabbitMqExtractCarrier(headers));
        return spanContext;
    }

    private class MethodInvocation4CurrentTest implements MethodInvocation {

        @Override
        public Method getMethod() {
            //ignore.
            return null;
        }

        @Override
        public Object[] getArguments() {
            // create message for current second arguments.
            Object[] objs = { null, getConstructMessageWithContext() };
            return objs;
        }

        @Override
        public Object proceed() throws Throwable {
            //ignore.
            return null;
        }

        @Override
        public Object getThis() {
            //ignore.
            return null;
        }

        @Override
        public AccessibleObject getStaticPart() {
            //ignore.
            return null;
        }
    }
}
