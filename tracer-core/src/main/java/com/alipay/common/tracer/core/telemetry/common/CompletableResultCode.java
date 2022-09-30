package com.alipay.common.tracer.core.telemetry.common;


import io.opentelemetry.sdk.internal.ComponentRegistry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CompletableResultCode {
    private static final CompletableResultCode SUCCESS = (new CompletableResultCode()).succeed();
    private static final CompletableResultCode FAILURE = (new CompletableResultCode()).fail();
    @Nullable
    private Boolean succeeded = null;
    private final List<Runnable> completionActions = new ArrayList();
    private final Object lock = new Object();

    public static CompletableResultCode ofSuccess() {
        return SUCCESS;
    }

    public static CompletableResultCode ofFailure() {
        return FAILURE;
    }

    public static CompletableResultCode ofAll(Collection<CompletableResultCode> codes) {
        if (codes.isEmpty()) {
            return ofSuccess();
        } else {
            CompletableResultCode result = new CompletableResultCode();
            AtomicInteger pending = new AtomicInteger(codes.size());
            AtomicBoolean failed = new AtomicBoolean();
            Iterator var4 = codes.iterator();

            while(var4.hasNext()) {
                CompletableResultCode code = (CompletableResultCode)var4.next();
                code.whenComplete(() -> {
                    if (!code.isSuccess()) {
                        failed.set(true);
                    }

                    if (pending.decrementAndGet() == 0) {
                        if (failed.get()) {
                            result.fail();
                        } else {
                            result.succeed();
                        }
                    }

                });
            }

            return result;
        }
    }

    public CompletableResultCode() {
    }

    public CompletableResultCode succeed() {
        synchronized(this.lock) {
            if (this.succeeded == null) {
                this.succeeded = true;
                Iterator var2 = this.completionActions.iterator();

                while(var2.hasNext()) {
                    Runnable action = (Runnable)var2.next();
                    action.run();
                }
            }

            return this;
        }
    }

    public CompletableResultCode fail() {
        synchronized(this.lock) {
            if (this.succeeded == null) {
                this.succeeded = false;
                Iterator var2 = this.completionActions.iterator();

                while(var2.hasNext()) {
                    Runnable action = (Runnable)var2.next();
                    action.run();
                }
            }

            return this;
        }
    }

    public boolean isSuccess() {
        synchronized(this.lock) {
            return this.succeeded != null && this.succeeded;
        }
    }

    public CompletableResultCode whenComplete(Runnable action) {
        boolean runNow = false;
        synchronized(this.lock) {
            if (this.succeeded != null) {
                runNow = true;
            } else {
                this.completionActions.add(action);
            }
        }

        if (runNow) {
            action.run();
        }

        return this;
    }

    public boolean isDone() {
        synchronized(this.lock) {
            return this.succeeded != null;
        }
    }

    public CompletableResultCode join(long timeout, TimeUnit unit) {
        if (this.isDone()) {
            return this;
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            Objects.requireNonNull(latch);
            this.whenComplete(latch::countDown);

            try {
                if (!latch.await(timeout, unit)) {
                    return this;
                }
            } catch (InterruptedException var6) {
                Thread.currentThread().interrupt();
            }

            return this;
        }
    }
}
