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

import com.alipay.sofa.tracer.plugins.skywalking.model.Segment;

import java.util.ArrayList;

/**
 * Message
 * @author zhaochen
 */
public class Message {
    final int                maxBytes;
    // max time to wait for sending next message
    final long               timeoutNanos;
    final ArrayList<Segment> message              = new ArrayList<>();
    boolean                  full                 = false;
    // whether there is more than one segment in the message
    boolean                  hasAtLeastOneSegment = false;
    int                      messageSizeInBytes;
    long                     deadlineNanoTime;
    // how many segments contains in the message
    int                      count                = 0;

    public Message(int maxBytes, Long timeoutNanos) {
        this.maxBytes = maxBytes;
        this.timeoutNanos = timeoutNanos;
        // contains []
        this.messageSizeInBytes = 2;
    }

    /**
     * add segment to the message
     * @param next segment to add
     * @param nextSizeInBytes the size of segment to be add
     * @return whether add successfully
     */
    public boolean offer(Segment next, int nextSizeInBytes) {
        int x = countMessageSizeInBytes(nextSizeInBytes);
        int y = maxBytes;
        int includingNextVsMaxBytes = (x < y) ? -1 : ((x == y) ? 0 : 1);
        if (includingNextVsMaxBytes > 0) {
            full = true;
            return false; // can't fit the next message into this buffer
        }
        addSegmentToMessage(next);
        messageSizeInBytes = x;
        count++;
        if (includingNextVsMaxBytes == 0)
            full = true;
        return true;
    }

    private int countMessageSizeInBytes(int nextSizeInBytes) {
        // if there is more than one segment in the message need to add one comma
        return messageSizeInBytes + nextSizeInBytes + (hasAtLeastOneSegment ? 1 : 0);
    }

    public void reset() {
        messageSizeInBytes = 2;
        hasAtLeastOneSegment = false;
        deadlineNanoTime = 0;
        full = false;
        count = 0;
        message.clear();
    }

    public ArrayList<Segment> getMessage() {
        return this.message;
    }

    private void addSegmentToMessage(Segment next) {
        this.message.add(next);
        if (!hasAtLeastOneSegment) {
            hasAtLeastOneSegment = true;
        }
    }

    /**
     * the time remaining to send the message
     * @return remaining time to send message
     */
    long remainingNanos() {
        if (message.isEmpty()) {
            deadlineNanoTime = System.nanoTime() + timeoutNanos;
        }
        return Math.max(deadlineNanoTime - System.nanoTime(), 0);
    }

    /**
     * if buffer is full or overtime send the message
     * @return whether ready to send message
     */
    boolean isReady() {
        return full || remainingNanos() <= 0;
    }

    /**
     * get how many segment contains in the message
     * @return
     */
    int getCount() {
        return this.count;
    }

}
