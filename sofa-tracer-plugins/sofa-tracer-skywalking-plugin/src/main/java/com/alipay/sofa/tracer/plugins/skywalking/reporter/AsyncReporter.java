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
package com.alipay.sofa.tracer.plugins.skywalking.reporter;

import com.alipay.common.tracer.core.appender.self.SelfLog;
import com.alipay.sofa.tracer.plugins.skywalking.sender.SkywalkingRestTemplateSender;
import com.alipay.sofa.tracer.plugins.skywalking.model.Segment;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AsyncReporter
 * @author zhaochen
 */
public class AsyncReporter implements Closeable, Flushable {
    final Segment[]              segments;
    final int[]                  sizesInBytes;
    final int                    maxBufferSize;                                    // the max num of segment
    SkywalkingRestTemplateSender sender;
    int                          count               = 0;                          //how many segment in the array
    int                          writePos            = 0;
    int                          readPos             = 0;
    int                          messageMaxBytes     = 2 * 1024 * 1024;            // max packetSize is 2M
    long                         messageTimeoutNanos = TimeUnit.SECONDS.toNanos(1);
    long                         closeTimeoutNanos   = TimeUnit.SECONDS.toNanos(1);
    final AtomicBoolean          closed              = new AtomicBoolean(false);
    final ReentrantLock          lock                = new ReentrantLock(false);
    final Condition              available           = lock.newCondition();
    final CountDownLatch         close               = new CountDownLatch(1);

    /**
     * when create a new reporter will start a new thread to flush segment into one message,
     * when the message size bigger than threshold or remaining time no more than 0, it's time to
     * send the message
     * @param maxBufferSize the size of the segment buffer
     * @param sender sender to send the message
     */
    public AsyncReporter(int maxBufferSize, SkywalkingRestTemplateSender sender) {
        this.maxBufferSize = maxBufferSize;
        this.segments = new Segment[maxBufferSize];
        this.sizesInBytes = new int[maxBufferSize];
        this.sender = sender;
        // many segments are bundled into one message
        final Message message = new Message(messageMaxBytes, messageTimeoutNanos);
        final Thread flushThread = new Thread("AsyncReporter") {
            @Override
            public void run() {
                try {
                    while (!closed.get()) {
                        flush(message);
                    }
                } catch (RuntimeException | Error e) {
                    SelfLog.warn("Unexpected error flushing spans", e);
                } finally {
                    if (count > 0) {
                        SelfLog.warn("Dropped " + count + " spans due to AsyncReporter.close()");
                    }
                    close.countDown();
                }
            }
        };
        flushThread.setDaemon(true);
        flushThread.start();
    }

    public void report(Segment segment, int segmentByteSize) {
        if (segment == null)
            throw new NullPointerException("segment == null");
        //if already closed or the segment Array is full just dropping the segment
        if (closed.get() || !addSegment(segment, segmentByteSize)) {
            SelfLog.warn("Dropped one span because reporter is close or buffer queue is full ");
        }
    }

    /**
     * a new thread will call this function in a synchronous loop until the reporter is closed
     * @param message
     */

    public void flush(Message message) {
        if (closed.get())
            throw new IllegalStateException("closed");
        // drain segment in the segment array as many as possible
        drainTo(message, message.remainingNanos());
        if (!message.isReady() && !closed.get())
            return;
        // send the message
        try {
            if (!sender.post(message.getMessage())) {
                int count = message.getCount();
                SelfLog.warn("Dropped " + count + " spans, result code is not 2XX");
            }
        } catch (RuntimeException | Error t) {
            // In failure case, we increment messages and spans dropped.
            int count = message.getCount();
            SelfLog.warn("Dropped " + count + " spans", t);
            // Raise in case the sender was closed out-of-band.
            if (t instanceof IllegalStateException)
                throw (IllegalStateException) t;
        } finally {
            message.reset();
        }
    }

    /**
     * add segment to the segment array
     * @param segment segment to add
     * @param segmentByteSize the size of segment to add
     * @return whether the operation is successful
     */
    private boolean addSegment(Segment segment, int segmentByteSize) {
        lock.lock();
        try {
            if (count == maxBufferSize)
                return false;
            segments[writePos] = segment;
            sizesInBytes[writePos] = segmentByteSize;
            writePos++;
            //back to the head
            if (writePos == maxBufferSize)
                writePos = 0;
            count++;
            // add successfully, alert any drainers
            available.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * clear the data buffered
     * @return how many segments are removed
     */
    private int clear() {
        lock.lock();
        try {
            int result = count;
            count = readPos = writePos = 0;
            Arrays.fill(segments, null);
            Arrays.fill(sizesInBytes, 0);
            return result;
        } finally {
            lock.unlock();
        }
    }

    /**
     *  Blocks for up to nanosTimeout for spans to appear. Then, consume as many as possible.
     * @param message message to receive the segment
     * @param nanosTimeout  remaining time to send the message
     * @return how many segments are add to message
     */
    int drainTo(Message message, long nanosTimeout) {
        try {
            lock.lockInterruptibly();
            try {
                long nanosLeft = nanosTimeout;
                while (count == 0) {
                    if (nanosLeft <= 0)
                        return 0;
                    nanosLeft = available.awaitNanos(nanosLeft);
                }
                return doDrain(message);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            return 0;
        }
    }

    /**
     * add segments to the message as many as possible
     * @param message
     * @return the count of segment added to message
     */
    int doDrain(Message message) {
        int drainedCount = 0;
        while (drainedCount < count) {
            Segment next = segments[readPos];
            int nextSizeInBytes = sizesInBytes[readPos];
            if (next == null)
                break;
            if (message.offer(next, nextSizeInBytes)) {
                drainedCount++;
                segments[readPos] = null;
                sizesInBytes[readPos] = 0;
                if (++readPos == segments.length)
                    readPos = 0; // circle back to the front of the array
            } else {
                break;
            }
        }
        count -= drainedCount;
        return drainedCount;
    }

    @Override
    public void close() throws IOException {
        if (!closed.compareAndSet(false, true))
            return; // already closed
        try {
            // wait for in-flight spans to send
            if (!close.await(closeTimeoutNanos, TimeUnit.NANOSECONDS)) {
                SelfLog.warn("Timed out waiting for in-flight spans to send");
            }
        } catch (InterruptedException e) {
            SelfLog.warn("Interrupted waiting for in-flight spans to send");
            Thread.currentThread().interrupt();
        }
        int count = clear();
        if (count > 0) {
            SelfLog.warn("Dropped " + count + " spans due to AsyncReporter.close()");
        }
    }

    @Override
    public void flush() throws IOException {
        // timeoutNanos equals 0, so will send immediately
        flush(new Message(messageMaxBytes, 0L));
    }
}
