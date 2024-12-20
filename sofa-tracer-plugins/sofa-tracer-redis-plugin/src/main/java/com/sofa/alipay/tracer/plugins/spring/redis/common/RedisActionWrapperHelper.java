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
import com.alipay.common.tracer.core.constants.SofaTracerConstant;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.sofa.alipay.tracer.plugins.spring.redis.tracer.RedisSofaTracer;

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
        buildSpan(command, deserialize(key));
        return activateAndCloseSpan(supplier);
    }

    public <T> T doInScope(String command, Supplier<T> supplier) {
        redisSofaTracer.startTrace(command);
        return activateAndCloseSpan(supplier);
    }

    public void doInScope(String command, byte[] key, Runnable runnable) {
        buildSpan(command, deserialize(key));
        activateAndCloseSpan(runnable);
    }

    public void doInScope(String command, Runnable runnable) {
        redisSofaTracer.startTrace(command);
        activateAndCloseSpan(runnable);
    }

    public <T> T doInScope(String command, byte[][] keys, Supplier<T> supplier) {
        SofaTracerSpan span = redisSofaTracer.startTrace(command);
        span.setTag("keys", toStringWithDeserialization(limitKeys(keys)));
        return activateAndCloseSpan(supplier);
    }

    <T> T[] limitKeys(T[] keys) {
        if (keys != null && keys.length > 1024) {
            return Arrays.copyOfRange(keys, 0, 1024);
        }
        return keys;
    }

    private void handleTraceCompletion(Throwable candidateThrowable) {
        if (candidateThrowable != null) {
            redisSofaTracer.endTrace(SofaTracerConstant.RESULT_CODE_ERROR,
                candidateThrowable.getMessage());
        } else {
            redisSofaTracer.endTrace(SofaTracerConstant.RESULT_CODE_SUCCESS, null);
        }
    }

    public <T> T decorate(Supplier<T> supplier, String operateName) {
        Throwable candidateThrowable = null;
        try {
            redisSofaTracer.startTrace(operateName);
            return supplier.get();
        } catch (Throwable t) {
            candidateThrowable = t;
            throw t;
        } finally {
            handleTraceCompletion(candidateThrowable);
        }
    }

    public void decorate(Action action, String operateName) {
        Throwable candidateThrowable = null;
        try {
            redisSofaTracer.startTrace(operateName);
            action.execute();
        } catch (Throwable t) {
            candidateThrowable = t;
            throw t;
        } finally {
            handleTraceCompletion(candidateThrowable);
        }
    }

    public <T extends Exception> void decorateThrowing(ThrowingAction<T> action, String operateName)
                                                                                                    throws T {
        Throwable candidateThrowable = null;
        try {
            redisSofaTracer.startTrace(operateName);
            action.execute();
        } catch (Throwable t) {
            candidateThrowable = t;
            throw t;
        } finally {
            handleTraceCompletion(candidateThrowable);
        }
    }

    private <T> T activateAndCloseSpan(Supplier<T> supplier) {
        Throwable candidateThrowable = null;
        try {
            return supplier.get();
        } catch (Throwable t) {
            candidateThrowable = t;
            throw t;
        } finally {
            handleTraceCompletion(candidateThrowable);
        }
    }

    private void activateAndCloseSpan(Runnable runnable) {

        Throwable candidateThrowable = null;
        try {
            runnable.run();
        } catch (Throwable t) {
            candidateThrowable = t;
            throw t;
        } finally {
            handleTraceCompletion(candidateThrowable);
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

    public void buildSpan(String operationName, Object key) {
        SofaTracerSpan span = redisSofaTracer.startTrace(operationName);
        span.setTag("key", nullable(key));
    }

    public static String nullable(Object object) {
        if (object == null) {
            return "";
        }
        return object.toString();
    }
}
