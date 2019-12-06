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
package com.alipay.sofa.tracer.plugins.springcloud.instruments.stream;

import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.context.span.SofaTracerSpanContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.sofa.tracer.plugins.springcloud.tracers.StreamPubSofaTracer;
import com.alipay.sofa.tracer.plugins.springcloud.tracers.StreamSubSofaTracer;
import org.springframework.aop.support.AopUtils;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.ClassUtils;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/11/28 9:50 PM
 * @since:
 **/
@GlobalChannelInterceptor
public class SofaTracerChannelInterceptor extends ChannelInterceptorAdapter implements
                                                                           ExecutorChannelInterceptor {

    public static final String        STREAM_DIRECT_CHANNEL = "org.springframework."
                                                              + "cloud.stream.messaging.DirectWithAttributesChannel";

    private static final String       REMOTE_SERVICE_NAME   = "broker";

    private final StreamSubSofaTracer subSofaTracer;

    private final StreamPubSofaTracer pubSofaTracer;

    private final boolean             integrationObjectSupportPresent;

    private final boolean             hasDirectChannelClass;

    private final Class<?>            directWithAttributesChannelClass;

    SofaTracerChannelInterceptor() {
        this.subSofaTracer = StreamSubSofaTracer.getStreamSubSofaTracerSingleton();
        this.pubSofaTracer = StreamPubSofaTracer.getStreamPubSofaTracerSingleton();
        this.integrationObjectSupportPresent = ClassUtils.isPresent(
            "org.springframework.integration.context.IntegrationObjectSupport", null);
        this.hasDirectChannelClass = ClassUtils.isPresent(
            "org.springframework.integration.channel.DirectChannel", null);
        this.directWithAttributesChannelClass = ClassUtils.isPresent(STREAM_DIRECT_CHANNEL, null) ? ClassUtils
            .resolveClassName(STREAM_DIRECT_CHANNEL, null) : null;
    }

    public static SofaTracerChannelInterceptor create() {
        return new SofaTracerChannelInterceptor();
    }

    /**
     *
     * @param headers
     * @return
     */
    private SofaTracerSpan parseFromHeaders(MessageHeaderAccessor headers) {
        SofaTracerSpanContext spanContext = parseFromMessageHeaders(headers);
        headers.setImmutable();
        SofaTracerSpan sofaTracerSpan = subSofaTracer.serverReceive(spanContext);
        return sofaTracerSpan;
    }

    /**
     * 从 header 中解析出 spanContext
     * @param headers
     * @return
     */
    private SofaTracerSpanContext parseFromMessageHeaders(MessageHeaderAccessor headers) {
        SofaTracerSpanContext spanContext = null;
        Object sofa_tracer_span_context_key = headers.getHeader("SOFA_TRACER_SPAN_CONTEXT_KEY");
        if (sofa_tracer_span_context_key != null) {
            spanContext = SofaTracerSpanContext.deserializeFromString(String
                .valueOf(sofa_tracer_span_context_key));
        }
        return spanContext;
    }

    /**
     * 注入 spanContext
     * @param headers
     * @param spanContext
     */
    private void injectIntoMessageHeaders(MessageHeaderAccessor headers,
                                          SofaTracerSpanContext spanContext) {
        String spanContextStr = spanContext.serializeSpanContext();
        headers.setHeader("SOFA_TRACER_SPAN_CONTEXT_KEY", spanContextStr);
    }

    /**
     * 消息发送之前需要将 spanContext 塞到消息头中
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        if (emptyMessage(message)) {
            return message;
        }
        try {
            Message<?> retrievedMessage = getMessage(message);
            MessageHeaderAccessor headers = mutableHeaderAccessor(retrievedMessage);
            // 发送之前构建 tracer
            SofaTracerSpan sofaTracerSpan = pubSofaTracer.clientSend("send");
            addTags(message, sofaTracerSpan, channel);
            // spanContext 注入到 headers 中
            injectIntoMessageHeaders(headers, sofaTracerSpan.getSofaTracerSpanContext());
            Message<?> outputMessage = outputMessage(message, retrievedMessage, headers);
            if (isDirectChannel(channel)) {
                beforeHandle(outputMessage, channel, null);
            }
            return outputMessage;
        } catch (Throwable t) {
            SelfLog.error("Failed to build a message span.", t);
        }
        return message;
    }

    /**
     * 输出消息
     * @param originalMessage
     * @param retrievedMessage
     * @param additionalHeaders
     * @return
     */
    private Message<?> outputMessage(Message<?> originalMessage, Message<?> retrievedMessage,
                                     MessageHeaderAccessor additionalHeaders) {
        MessageHeaderAccessor headers = MessageHeaderAccessor.getMutableAccessor(originalMessage);
        if (originalMessage instanceof ErrorMessage) {
            ErrorMessage errorMessage = (ErrorMessage) originalMessage;
            headers.copyHeaders(null);
            return new ErrorMessage(errorMessage.getPayload(),
                isWebSockets(headers) ? headers.getMessageHeaders() : new MessageHeaders(
                    headers.getMessageHeaders()), errorMessage.getOriginalMessage());
        }
        headers.copyHeaders(additionalHeaders.getMessageHeaders());
        return new GenericMessage<>(retrievedMessage.getPayload(),
            isWebSockets(headers) ? headers.getMessageHeaders() : new MessageHeaders(
                headers.getMessageHeaders()));
    }

    private boolean isWebSockets(MessageHeaderAccessor headerAccessor) {
        return headerAccessor.getMessageHeaders().containsKey("stompCommand")
               || headerAccessor.getMessageHeaders().containsKey("simpMessageType");
    }

    private boolean isDirectChannel(MessageChannel channel) {
        Class<?> targetClass = AopUtils.getTargetClass(channel);
        boolean directChannel = this.hasDirectChannelClass
                                && DirectChannel.class.isAssignableFrom(targetClass);
        if (!directChannel) {
            return false;
        }
        if (this.directWithAttributesChannelClass == null) {
            return true;
        }
        return !isStreamSpecialDirectChannel(targetClass);
    }

    private boolean isStreamSpecialDirectChannel(Class<?> targetClass) {
        return this.directWithAttributesChannelClass.isAssignableFrom(targetClass);
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent,
                                    Exception ex) {
        if (emptyMessage(message)) {
            return;
        }
        if (isDirectChannel(channel)) {
            afterMessageHandled(message, channel, null, ex);
        }
        finishSendSpan(ex);
    }

    /**
     * This starts a consumer span as a child of the incoming message or the current trace
     * context, placing it in scope until the receive completes.
     *
     * 消费端接受 消息，解析出 spanContext
     */
    @Override
    public Message<?> postReceive(Message<?> message, MessageChannel channel) {
        if (emptyMessage(message)) {
            return message;
        }
        try {
            MessageHeaderAccessor headers = mutableHeaderAccessor(message);
            SofaTracerSpanContext spanContext = parseFromMessageHeaders(headers);
            SofaTracerSpan sofaTracerSpan = subSofaTracer.serverReceive(spanContext);
            sofaTracerSpan.setOperationName("receive");
            sofaTracerSpan.setTag("channel", messageChannelName(channel));
            headers.setImmutable();
            if (message instanceof ErrorMessage) {
                ErrorMessage errorMessage = (ErrorMessage) message;
                return new ErrorMessage(errorMessage.getPayload(), headers.getMessageHeaders(),
                    errorMessage.getOriginalMessage());
            }
            return new GenericMessage<>(message.getPayload(), headers.getMessageHeaders());
        } catch (Throwable t) {
            SelfLog.error("Failed to build span when receive msg.", t);
        }
        return message;
    }

    @Override
    public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
        if (emptyMessage(message)) {
            return;
        }
        finishReceiveSpan(ex);
    }

    private void finishReceiveSpan(Exception error) {
        SofaTracerSpan currentSpan = SofaTraceContextHolder.getSofaTraceContext().getCurrentSpan();
        if (currentSpan == null) {
            return;
        }
        String resultCode = SofaTracerConstant.RESULT_CODE_SUCCESS;
        // an error occurred, adding error to span
        if (error != null) {
            String message = error.getMessage();
            if (message == null) {
                message = error.getClass().getSimpleName();
            }
            currentSpan.setTag("error", message);
            resultCode = SofaTracerConstant.RESULT_CODE_ERROR;
        }
        subSofaTracer.serverSend(resultCode);
    }

    /**
     * This starts a consumer span as a child of the incoming message or the current trace
     * context. It then creates a span for the handler, placing it in scope.
     *
     * 处理之前
     */
    @Override
    public Message<?> beforeHandle(Message<?> message, MessageChannel channel,
                                   MessageHandler handler) {
        if (emptyMessage(message)) {
            return message;
        }
        System.out.println("----------------this is afterMessageHandled.");
        MessageHeaderAccessor headers = mutableHeaderAccessor(message);
        if (message instanceof ErrorMessage) {
            return new ErrorMessage((Throwable) message.getPayload(), headers.getMessageHeaders());
        }
        headers.setImmutable();
        return new GenericMessage<>(message.getPayload(), headers.getMessageHeaders());
        //        TraceContextOrSamplingFlags extracted = this.extractor.extract(headers);
        //        // Start and finish a consumer span as we will immediately process it.
        //        Span consumerSpan = this.tracer.nextSpan(extracted);
        //        if (!consumerSpan.isNoop()) {
        //            consumerSpan.kind(Span.Kind.CONSUMER).start();
        //            consumerSpan.remoteServiceName(REMOTE_SERVICE_NAME);
        //            addTags(message, consumerSpan, channel);
        //            consumerSpan.finish();
        //        }
        //        // create and scope a span for the message processor
        //        this.threadLocalSpan
        //                .next(TraceContextOrSamplingFlags.create(consumerSpan.context()))
        //                .name("handle").start();
        //        // remove any trace headers, but don't re-inject as we are synchronously
        //        // processing the
        //        // message and can rely on scoping to access this span later.
        //        MessageHeaderPropagation.removeAnyTraceHeaders(headers,
        //                this.tracing.propagation().keys());
        //        if (log.isDebugEnabled()) {
        //            log.debug("Created a new span in before handle" + consumerSpan);
        //        }
    }

    @Override
    public void afterMessageHandled(Message<?> message, MessageChannel channel,
                                    MessageHandler handler, Exception ex) {
        if (emptyMessage(message)) {
            return;
        }
        System.out.println("----------------this is afterMessageHandled.");
    }

    void addTags(Message<?> message, SofaTracerSpan span, MessageChannel channel) {
        if (channel != null) {
            span.setTag(CommonSpanTags.REMOTE_APP, REMOTE_SERVICE_NAME);
            span.setTag("channel", messageChannelName(channel));
        }
    }

    private String channelName(MessageChannel channel) {
        String name = null;
        if (this.integrationObjectSupportPresent) {
            if (channel instanceof IntegrationObjectSupport) {
                name = ((IntegrationObjectSupport) channel).getComponentName();
            }
            if (name == null && channel instanceof AbstractMessageChannel) {
                name = ((AbstractMessageChannel) channel).getFullChannelName();
            }
        }
        if (name == null) {
            name = channel.toString();
        }
        return name;
    }

    private String messageChannelName(MessageChannel channel) {
        return channelName(channel);
    }

    void finishSendSpan(Exception error) {
        SofaTracerSpan currentSpan = SofaTraceContextHolder.getSofaTraceContext().getCurrentSpan();
        if (currentSpan == null) {
            return;
        }
        String resultCode = SofaTracerConstant.RESULT_CODE_SUCCESS;
        // an error occurred, adding error to span
        if (error != null) {
            String message = error.getMessage();
            if (message == null) {
                message = error.getClass().getSimpleName();
            }
            currentSpan.setTag("error", message);
            resultCode = SofaTracerConstant.RESULT_CODE_ERROR;
        }
        pubSofaTracer.clientReceive(resultCode);
    }

    private MessageHeaderAccessor mutableHeaderAccessor(Message<?> message) {
        MessageHeaderAccessor headers = MessageHeaderAccessor.getMutableAccessor(message);
        headers.setLeaveMutable(true);
        return headers;
    }

    private Message<?> getMessage(Message<?> message) {
        Object payload = message.getPayload();
        if (payload instanceof MessagingException) {
            MessagingException e = (MessagingException) payload;
            Message<?> failedMessage = e.getFailedMessage();
            return failedMessage != null ? failedMessage : message;
        }
        return message;
    }

    private boolean emptyMessage(Message<?> message) {
        return message == null;
    }

}
