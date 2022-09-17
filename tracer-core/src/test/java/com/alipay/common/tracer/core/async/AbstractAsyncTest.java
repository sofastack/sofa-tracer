package com.alipay.common.tracer.core.async;

import com.alipay.common.tracer.core.SofaTracer;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import org.junit.Before;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
/**
 *
 * @author risk
 * @version $Id: AbstractAsyncTest.java, v 0.1 Nov. 22, 2022 $
 */

public abstract class AbstractAsyncTest {

    SofaTracerSpan span;
    SofaTracer tracer;

    protected CountDownLatch countDownLatch = new CountDownLatch(0);




    class TestRunnable implements Runnable {
        @Override
        public void run() {
            try {
                tracer.buildSpan("childRunnable").start().finish();
            } finally {
                countDownLatch.countDown();
            }
        }
    }

    class TestCallable implements Callable<Void> {
        @Override
        public Void call() throws Exception {

            try {
                tracer.buildSpan("childCallable").start().finish();
            } finally {
                countDownLatch.countDown();

            }
            return null;
        }
    }

    protected Thread createThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        return thread;
    }

    protected <V> Thread createThread(FutureTask<V> futureTask) {
        Thread thread = new Thread(futureTask);
        return thread;
    }


}
