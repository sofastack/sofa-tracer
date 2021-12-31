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
package com.sofa.alipay.tracer.plugins.spring.redis.common;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.configuration.SofaTracerConfiguration;
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.span.CommonSpanTags;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.sofa.alipay.tracer.plugins.spring.redis.tracer.RedisSofaTracer;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/11/18 9:15 PM
 * @since:
 **/
public class RedisActionWrapperHelper {
    public static final String    COMMAND        = "command";
    public static final String    COMPONENT_NAME = "java-redis";
    public static final String    DB_TYPE        = "redis";
    protected final SofaTracer    tracer;
    private final RedisSofaTracer redisSofaTracer;
    private String                appName;

    public RedisActionWrapperHelper() {
        redisSofaTracer = RedisSofaTracer.getRedisSofaTracerSingleton();
        this.tracer = redisSofaTracer.getSofaTracer();
    }

    private static String deserialize(byte[] bytes) {
        return (bytes == null ? "" : new String(bytes, StandardCharsets.UTF_8));
    }

    public <T> T doInScope(String command, byte[] key, Supplier<T> supplier) {
        Span span = buildSpan(command, deserialize(key));
        return activateAndCloseSpan(span, supplier);
    }

    public <T> T doInScope(String command, Supplier<T> supplier) {
        Span span = buildSpan(command);
        return activateAndCloseSpan(span, supplier);
    }

    public void doInScope(String command, byte[] key, Runnable runnable) {
        Span span = buildSpan(command, deserialize(key));
        activateAndCloseSpan(span, runnable);
    }

    public void doInScope(String command, Runnable runnable) {
        Span span = buildSpan(command);
        activateAndCloseSpan(span, runnable);
    }

    public <T> T doInScope(String command, byte[][] keys, Supplier<T> supplier) {
        Span span = buildSpan(command);
        span.setTag("keys", toStringWithDeserialization(limitKeys(keys)));
        return activateAndCloseSpan(span, supplier);
    }

    <T> T[] limitKeys(T[] keys) {
        if (keys != null && keys.length > 1024) {
            return Arrays.copyOfRange(keys, 0, 1024);
        }
        return keys;
    }

    public <T> T decorate(Supplier<T> supplier, String operateName) {
        Span span = buildSpan(operateName);
        Throwable candidateThrowable = null;
        try {
            return supplier.get();
        } catch (Throwable t) {
            candidateThrowable = t;
            throw t;
        } finally {
            if (candidateThrowable != null) {
                span.setTag(Tags.ERROR.getKey(),candidateThrowable.getMessage());
                redisSofaTracer.clientReceive(SofaTracerConstant.RESULT_CODE_ERROR);
            } else {
                redisSofaTracer.clientReceive(SofaTracerConstant.RESULT_CODE_SUCCESS);
            }
        }
    }

    public void decorate(Action action, String operateName) {
        Span span = buildSpan(operateName);
        Throwable candidateThrowable = null;
        try {
            action.execute();
        } catch (Throwable t) {
            candidateThrowable = t;
            throw t;
        } finally {
            if (candidateThrowable != null) {
                span.setTag(Tags.ERROR.getKey(),candidateThrowable.getMessage());
                redisSofaTracer.clientReceive(SofaTracerConstant.RESULT_CODE_ERROR);
            } else {
                redisSofaTracer.clientReceive(SofaTracerConstant.RESULT_CODE_SUCCESS);
            }
        }
    }

    public <T extends Exception> void decorateThrowing(ThrowingAction<T> action, String operateName)
                                                                                                    throws T {
        Span span = buildSpan(operateName);
        Throwable candidateThrowable = null;
        try {
            action.execute();
        } catch (Throwable t) {
            candidateThrowable = t;
            throw t;
        } finally {
            if (candidateThrowable != null) {
                span.setTag(Tags.ERROR.getKey(),candidateThrowable.getMessage());
                redisSofaTracer.clientReceive(SofaTracerConstant.RESULT_CODE_ERROR);
            } else {
                redisSofaTracer.clientReceive(SofaTracerConstant.RESULT_CODE_SUCCESS);
            }
        }
    }

    public <T extends Exception, V> V decorateThrowing(ThrowingSupplier<T, V> supplier,
                                                       String operateName) throws T {

        Span span = buildSpan(operateName);
        Throwable candidateThrowable = null;
        try {
            return supplier.get();
        } catch (Throwable t) {
            candidateThrowable = t;
            throw t;
        } finally {
            if (candidateThrowable != null) {
                span.setTag(Tags.ERROR.getKey(),candidateThrowable.getMessage());
                redisSofaTracer.clientReceive(SofaTracerConstant.RESULT_CODE_ERROR);
            } else {
                redisSofaTracer.clientReceive(SofaTracerConstant.RESULT_CODE_SUCCESS);
            }
        }
    }

    private <T> T activateAndCloseSpan(Span span, Supplier<T> supplier) {
        Throwable candidateThrowable = null;
        try {
            return supplier.get();
        } catch (Throwable t) {
            candidateThrowable = t;
            throw t;
        } finally {
            if (candidateThrowable != null) {
                span.setTag(Tags.ERROR.getKey(),candidateThrowable.getMessage());
                redisSofaTracer.clientReceive(SofaTracerConstant.RESULT_CODE_ERROR);
            } else {
                redisSofaTracer.clientReceive(SofaTracerConstant.RESULT_CODE_SUCCESS);
            }
        }
    }

    private void activateAndCloseSpan(Span span, Runnable runnable) {

        Throwable candidateThrowable = null;
        try {
            runnable.run();
        } catch (Throwable t) {
            candidateThrowable = t;
            throw t;
        } finally {
            if (candidateThrowable != null) {
                span.setTag(Tags.ERROR.getKey(),candidateThrowable.getMessage());
                redisSofaTracer.clientReceive(SofaTracerConstant.RESULT_CODE_ERROR);
            } else {
                redisSofaTracer.clientReceive(SofaTracerConstant.RESULT_CODE_SUCCESS);
            }
        }
    }

    private static String toStringWithDeserialization(byte[][] array) {
        if (array == null) {
            return "null";
        }

        List<String> list = new ArrayList<>();

        for (byte[] bytes : array) {
            list.add(deserialize(bytes));
        }

        return "[" + String.join(", ", list) + "]";
    }

    public Span buildSpan(String operationName) {
        return builder(operationName).start();
    }

    public Span buildSpan(String operationName, Object key) {
        return buildSpan(operationName).setTag("key", nullable(key));
    }

    public static String nullable(Object object) {
        if (object == null) {
            return "";
        }
        return object.toString();
    }

    private Tracer.SpanBuilder builder(String operationName) {
        SofaTracerSpan currentSpan = SofaTraceContextHolder.getSofaTraceContext().getCurrentSpan();
        if (this.appName == null) {
            this.appName = SofaTracerConfiguration
                .getProperty(SofaTracerConfiguration.TRACER_APPNAME_KEY);
        }
        Tracer.SpanBuilder sb = tracer.buildSpan(operationName).asChildOf(currentSpan)
            .withTag(CommonSpanTags.LOCAL_APP, appName).withTag(COMMAND, operationName)
            .withTag(Tags.COMPONENT.getKey(), COMPONENT_NAME)
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
            .withTag(Tags.DB_TYPE.getKey(), DB_TYPE);
        return sb;
    }
}
