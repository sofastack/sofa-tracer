/*
 * Copyright 2011 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.disruptor;

/**
 * Experimental poll-based interface for the Disruptor.
 */
public class EventPoller<T> {
    private final DataProvider<T> dataProvider;
    private final Sequencer       sequencer;
    private final Sequence        sequence;
    private final Sequence        gatingSequence;

    public interface Handler<T> {
        boolean onEvent(T event, long sequence, boolean endOfBatch) throws Exception;
    }

    public enum PollState {
        PROCESSING, GATING, IDLE
    }

    public EventPoller(final DataProvider<T> dataProvider, final Sequencer sequencer,
                       final Sequence sequence, final Sequence gatingSequence) {
        this.dataProvider = dataProvider;
        this.sequencer = sequencer;
        this.sequence = sequence;
        this.gatingSequence = gatingSequence;
    }

    public PollState poll(final Handler<T> eventHandler) throws Exception {
        final long currentSequence = sequence.get();
        long nextSequence = currentSequence + 1;
        final long availableSequence = sequencer.getHighestPublishedSequence(nextSequence,
            gatingSequence.get());

        if (nextSequence <= availableSequence) {
            boolean processNextEvent;
            long processedSequence = currentSequence;

            try {
                do {
                    final T event = dataProvider.get(nextSequence);
                    processNextEvent = eventHandler.onEvent(event, nextSequence,
                        nextSequence == availableSequence);
                    processedSequence = nextSequence;
                    nextSequence++;

                } while (nextSequence <= availableSequence & processNextEvent);
            } finally {
                sequence.set(processedSequence);
            }

            return PollState.PROCESSING;
        } else if (sequencer.getCursor() >= nextSequence) {
            return PollState.GATING;
        } else {
            return PollState.IDLE;
        }
    }

    public static <T> EventPoller<T> newInstance(final DataProvider<T> dataProvider,
                                                 final Sequencer sequencer,
                                                 final Sequence sequence,
                                                 final Sequence cursorSequence,
                                                 final Sequence... gatingSequences) {
        Sequence gatingSequence;
        if (gatingSequences.length == 0) {
            gatingSequence = cursorSequence;
        } else if (gatingSequences.length == 1) {
            gatingSequence = gatingSequences[0];
        } else {
            gatingSequence = new FixedSequenceGroup(gatingSequences);
        }

        return new EventPoller<T>(dataProvider, sequencer, sequence, gatingSequence);
    }

    public Sequence getSequence() {
        return sequence;
    }
}
