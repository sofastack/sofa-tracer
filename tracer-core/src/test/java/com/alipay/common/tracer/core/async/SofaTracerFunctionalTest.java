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
package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.context.trace.SofaTraceContext;
import com.alipay.common.tracer.core.holder.SofaTraceContextHolder;
import com.alipay.common.tracer.core.reporter.digest.DiskReporterImpl;
import com.alipay.common.tracer.core.reporter.facade.Reporter;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.tracertest.encoder.ClientSpanEncoder;
import com.alipay.common.tracer.core.tracertest.encoder.ServerSpanEncoder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongSupplier;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.Predicate;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;

/**
 * @author khotyn,risk
 * @version v0.2
 */
public class SofaTracerFunctionalTest {
    private static SofaTracerSpan sofaTracerSpan;
    private static  SofaTracer sofaTracer;
    @BeforeClass
    public static void setup() {
        String clientLogType = "client-log-test.log";
        Reporter clientReporter = new DiskReporterImpl(clientLogType, new ClientSpanEncoder());
        String serverLogType = "server-log-test.log";
        Reporter serverReporter = new DiskReporterImpl(serverLogType, new ServerSpanEncoder());
        String tracerType = "SofaTracerSpanTest";
        sofaTracer = new SofaTracer.Builder(tracerType)
            .withTag("tracer", "SofaTraceContextHolderTest").withClientReporter(clientReporter)
            .withServerReporter(serverReporter).build();
        sofaTracerSpan = (SofaTracerSpan) sofaTracer.buildSpan("SofaTracerSpanTest").start();
    }

    @Before
    public void pushSpan() {
        sofaTracer.activateSpan(sofaTracerSpan);
    }

    @Test
    public void testWrappedBiConsumer() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        BiConsumer<String, String> biConsumer = new SofaTracerBiConsumer<>((s, s2) -> spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan(),sofaTracer);
        useThreadToRun(() -> biConsumer.accept("", ""));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawBiConsumer() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        BiConsumer<String, String> biConsumer = (s, s2) -> spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
        useThreadToRun(() -> biConsumer.accept("", ""));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedBiFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        BiFunction<String, String, String> biFunction = new SofaTracerBiFunction<>((s, s2) -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return "";
        }, sofaTracer);
        useThreadToRun(() -> biFunction.apply("", ""));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawBiFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        BiFunction<String, String, String> biFunction = (s, s2) -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return "";
        };
        useThreadToRun(() -> biFunction.apply("", ""));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedBiPredicate() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        BiPredicate<String, String> biPredicate = new SofaTracerBiPredicate<>((s, s2) -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return false;
        }, sofaTracer);
        useThreadToRun(() -> biPredicate.test("", ""));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawBiPredicate() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        BiPredicate<String, String> biPredicate = (s, s2) -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return false;
        };
        useThreadToRun(() -> biPredicate.test("", ""));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedBooleanSupplier() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        BooleanSupplier booleanSupplier = new SofaTracerBooleanSupplier(() -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return false;
        }, sofaTracer);
        useThreadToRun(booleanSupplier::getAsBoolean);
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawBooleanSupplier() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        BooleanSupplier booleanSupplier = () -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return false;
        };
        useThreadToRun(booleanSupplier::getAsBoolean);
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedConsumer() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        Consumer<String> consumer = new SofaTracerConsumer<>(s -> spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan()
        , sofaTracer);
        useThreadToRun(() -> consumer.accept(""));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawConsumer() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        Consumer<String> consumer = s -> spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
        useThreadToRun(() -> consumer.accept(""));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedDoubleBinaryOperator() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        DoubleBinaryOperator doubleBinaryOperator = new SofaTracerDoubleBinaryOperator((left, right) -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(() -> doubleBinaryOperator.applyAsDouble(0, 0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawDoubleBinaryOperator() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        DoubleBinaryOperator doubleBinaryOperator = (left, right) -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> doubleBinaryOperator.applyAsDouble(0, 0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedDoubleConsumer() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        DoubleConsumer doubleConsumer = new SofaTracerDoubleConsumer(value -> spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan()
        , sofaTracer);
        useThreadToRun(() -> doubleConsumer.accept(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawDoubleConsumer() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        DoubleConsumer doubleConsumer = value -> spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
        useThreadToRun(() -> doubleConsumer.accept(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedDoubleFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        DoubleFunction<String> doubleFunction = new SofaTracerDoubleFunction<>(value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return null;
        }, sofaTracer);
        useThreadToRun(() -> doubleFunction.apply(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRowDoubleFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        DoubleFunction<String> doubleFunction = value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return null;
        };
        useThreadToRun(() -> doubleFunction.apply(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedDoublePredicate() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        DoublePredicate doublePredicate = new SofaTracerDoublePredicate(value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return false;
        }, sofaTracer);

        useThreadToRun(() -> doublePredicate.test(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawDoublePredicate() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        DoublePredicate doublePredicate = value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return false;
        };
        useThreadToRun(() -> doublePredicate.test(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedDoubleSupplier() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        DoubleSupplier doubleSupplier = new SofaTracerDoubleSupplier(() -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(doubleSupplier::getAsDouble);
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawDoubleSupplier() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        DoubleSupplier doubleSupplier = () -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(doubleSupplier::getAsDouble);
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedDoubleToIntFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        DoubleToIntFunction doubleToIntFunction = new SofaTracerDoubleToIntFunction(value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(() -> doubleToIntFunction.applyAsInt(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawDoubleToIntFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        DoubleToIntFunction doubleToIntFunction = value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> doubleToIntFunction.applyAsInt(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedDoubleToLongFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        DoubleToLongFunction doubleToLongFunction = new SofaTracerDoubleToLongFunction(value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(() -> doubleToLongFunction.applyAsLong(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawDoubleToLongFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        DoubleToLongFunction doubleToLongFunction = value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> doubleToLongFunction.applyAsLong(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedDoubleUnaryOperator() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        DoubleUnaryOperator doubleUnaryOperator = new SofaTracerDoubleUnaryOperator(operand -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(() -> doubleUnaryOperator.applyAsDouble(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawDoubleUnaryOperator() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        DoubleUnaryOperator doubleUnaryOperator = operand -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> doubleUnaryOperator.applyAsDouble(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        Function<String, String> function = new SofaTracerFunction<>(s -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return "";
        }, sofaTracer);
        useThreadToRun(() -> function.apply(""));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        Function<String, String> function = s -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return "";
        };
        useThreadToRun(() -> function.apply(""));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedIntBinaryOperator() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        IntBinaryOperator intBinaryOperator = new SofaTracerIntBinaryOperator((left, right) -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(() -> intBinaryOperator.applyAsInt(0, 0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawIntBinaryOperator() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        IntBinaryOperator intBinaryOperator = (left, right) -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> intBinaryOperator.applyAsInt(0, 0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedIntConsumer() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        IntConsumer intConsumer = new SofaTracerIntConsumer(value -> spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan()
        , sofaTracer);
        useThreadToRun(() -> intConsumer.accept(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawIntConsumer() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        IntConsumer intConsumer = value -> spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
        useThreadToRun(() -> intConsumer.accept(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedIntFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        IntFunction<String> intFunction = new SofaTracerIntFunction<>(value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return null;
        }, sofaTracer);
        useThreadToRun(() -> intFunction.apply(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawIntFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        IntFunction<String> intFunction = value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return null;
        };
        useThreadToRun(() -> intFunction.apply(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedIntPredicate() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        IntPredicate intPredicate = new SofaTracerIntPredicate(value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return false;
        }, sofaTracer);
        useThreadToRun(() -> intPredicate.test(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawIntPredicate() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        IntPredicate intPredicate = value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return false;
        };
        useThreadToRun(() -> intPredicate.test(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedIntSupplier() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        IntSupplier intSupplier = new SofaTracerIntSupplier(() -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(intSupplier::getAsInt);
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawIntSupplier() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        IntSupplier intSupplier = () -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(intSupplier::getAsInt);
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedIntToDoubleFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        IntToDoubleFunction intToDoubleFunction = new SofaTracerIntToDoubleFunction(value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(() -> intToDoubleFunction.applyAsDouble(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawIntToDoubleFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        IntToDoubleFunction intToDoubleFunction = value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> intToDoubleFunction.applyAsDouble(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedIntToLongFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        IntToLongFunction intToLongFunction = new SofaTracerIntToLongFunction(value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(() -> intToLongFunction.applyAsLong(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawIntToLongFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        IntToLongFunction intToLongFunction = value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> intToLongFunction.applyAsLong(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedIntUnaryOperator() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        IntUnaryOperator intUnaryOperator = new SofaTracerIntUnaryOperator(operand -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(() -> intUnaryOperator.applyAsInt(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawIntUnaryOperator() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        IntUnaryOperator intUnaryOperator = operand -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> intUnaryOperator.applyAsInt(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedLongBinaryOperator() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        LongBinaryOperator longBinaryOperator = new SofaTracerLongBinaryOperator((left, right) -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(() -> longBinaryOperator.applyAsLong(0, 0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawLongBinaryOperator() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        LongBinaryOperator longBinaryOperator = (left, right) -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> longBinaryOperator.applyAsLong(0, 0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedLongConsumer() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        LongConsumer longConsumer = new SofaTracerLongConsumer(value -> spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan()
        , sofaTracer);
        useThreadToRun(() -> longConsumer.accept(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawLongConsumer() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        LongConsumer longConsumer = value -> spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
        useThreadToRun(() -> longConsumer.accept(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedLongFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        LongFunction<String> longFunction = new SofaTracerLongFunction<>(value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return null;
        }, sofaTracer);
        useThreadToRun(() -> longFunction.apply(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawLongFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        LongFunction<String> longFunction = value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return null;
        };
        useThreadToRun(() -> longFunction.apply(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedLongPredicate() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        LongPredicate longPredicate = new SofaTracerLongPredicate(value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return false;
        },sofaTracer);
        useThreadToRun(() -> longPredicate.test(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawLongPredicate() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        LongPredicate longPredicate = value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return false;
        };
        useThreadToRun(() -> longPredicate.test(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedLongSupplier() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        LongSupplier longSupplier = new SofaTracerLongSupplier(() -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(longSupplier::getAsLong);
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawLongSupplier() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        LongSupplier longSupplier = () -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(longSupplier::getAsLong);
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedLongToDoubleFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        LongToDoubleFunction longToDoubleFunction = new SofaTracerLongToDoubleFunction(value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(() -> longToDoubleFunction.applyAsDouble(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawLongToDoubleFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        LongToDoubleFunction longToDoubleFunction = value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> longToDoubleFunction.applyAsDouble(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedLongToIntFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        LongToIntFunction longToIntFunction = new SofaTracerLongToIntFunction(value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(() -> longToIntFunction.applyAsInt(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawLongToIntFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        LongToIntFunction longToIntFunction = value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> longToIntFunction.applyAsInt(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedLongUnaryOperator() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        LongUnaryOperator longUnaryOperator = new SofaTracerLongUnaryOperator(operand -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(() -> longUnaryOperator.applyAsLong(0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawLongUnaryOperator() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        LongUnaryOperator longUnaryOperator = operand -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> longUnaryOperator.applyAsLong(0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedObjDoubleConsumer() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ObjDoubleConsumer<String> objDoubleConsumer = new SofaTracerObjDoubleConsumer<>((s, value) -> spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan()
        , sofaTracer);
        useThreadToRun(() -> objDoubleConsumer.accept("", 0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawObjDoubleConsumer() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ObjDoubleConsumer<String> objDoubleConsumer = (s, value) -> spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
        useThreadToRun(() -> objDoubleConsumer.accept("", 0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedObjIntConsumer() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ObjIntConsumer<String> objIntConsumer = new SofaTracerObjIntConsumer<>((s, value) -> spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan()
        , sofaTracer);
        useThreadToRun(() -> objIntConsumer.accept("", 0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawObjIntConsumer() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ObjIntConsumer<String> objIntConsumer = (s, value) -> spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
        useThreadToRun(() -> objIntConsumer.accept("", 0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedObjLongConsumer() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ObjLongConsumer<String> objLongConsumer = new SofaTracerObjLongConsumer<>((s, value) -> spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan()
        , sofaTracer);
        useThreadToRun(() -> objLongConsumer.accept("", 0));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawObjLongConsumer() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ObjLongConsumer<String> objLongConsumer = (s, value) -> spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
        useThreadToRun(() -> objLongConsumer.accept("", 0));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedPredicate() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        Predicate<String> predicate = new SofaTracerPredicate<>(s -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return false;
        }, sofaTracer);
        useThreadToRun(() -> predicate.test(""));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawPredicate() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        Predicate<String> predicate = s -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return false;
        };
        useThreadToRun(() -> predicate.test(""));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedSupplier() throws ExecutionException, InterruptedException {
        CompletableFuture<SofaTracerSpan> future = CompletableFuture.supplyAsync(new SofaTracerSupplier<>(() -> (SofaTracerSpan) sofaTracer.activeSpan()
                ,sofaTracer )
        );
        Assert.assertEquals(sofaTracerSpan, future.get());
    }

    @Test
    public void testRawSupplier() throws ExecutionException, InterruptedException {
        CompletableFuture<SofaTracerSpan> future = CompletableFuture.supplyAsync(() -> (SofaTracerSpan) sofaTracer.activeSpan());
        Assert.assertNull(future.get());
    }

    @Test
    public void testWrappedToDoubleBiFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ToDoubleBiFunction<String, String> toDoubleBiFunction = new SofaTracerToDoubleBiFunction<>((s, s2) -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        },sofaTracer);
        useThreadToRun(() -> toDoubleBiFunction.applyAsDouble("", ""));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawToDoubleBiFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ToDoubleBiFunction<String, String> toDoubleBiFunction = (s, s2) -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> toDoubleBiFunction.applyAsDouble("", ""));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedToDoubleFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ToDoubleFunction<String> toDoubleFunction = new SofaTracerToDoubleFunction<>(value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(() -> toDoubleFunction.applyAsDouble(""));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawToDoubleFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ToDoubleFunction<String> toDoubleFunction = value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> toDoubleFunction.applyAsDouble(""));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedToIntBiFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ToIntBiFunction<String, String> toIntBiFunction = new SofaTracerToIntBiFunction<>((s, s2) -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(() -> toIntBiFunction.applyAsInt("", ""));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawToIntBiFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ToIntBiFunction<String, String> toIntBiFunction = (s, s2) -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> toIntBiFunction.applyAsInt("", ""));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedToIntFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ToIntFunction<String> toIntFunction = new SofaTracerToIntFunction<>(value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(() -> toIntFunction.applyAsInt(""));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawToIntFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ToIntFunction<String> toIntFunction = value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> toIntFunction.applyAsInt(""));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedToLongBiFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ToLongBiFunction<String, String> toLongBiFunction = new SofaTracerToLongBiFunction<>((s, s2) -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(() -> toLongBiFunction.applyAsLong("", ""));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawToLongBiFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ToLongBiFunction<String, String> toLongBiFunction = (s, s2) -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> toLongBiFunction.applyAsLong("", ""));
        Assert.assertNull(spanInFunction[0]);
    }

    @Test
    public void testWrappedToLongFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ToLongFunction<String> toLongFunction = new SofaTracerToLongFunction<>(value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        }, sofaTracer);
        useThreadToRun(() -> toLongFunction.applyAsLong(""));
        Assert.assertEquals(sofaTracerSpan, spanInFunction[0]);
    }

    @Test
    public void testRawToLongFunction() throws InterruptedException {
        final SofaTracerSpan[] spanInFunction = {null};
        ToLongFunction<String> toLongFunction = value -> {
            spanInFunction[0] = (SofaTracerSpan) sofaTracer.activeSpan();
            return 0;
        };
        useThreadToRun(() -> toLongFunction.applyAsLong(""));
        Assert.assertNull(spanInFunction[0]);
    }

    private void useThreadToRun(Runnable runnable) throws InterruptedException {
        Thread thread = new Thread(runnable);
        thread.start();
        thread.join();
    }
}