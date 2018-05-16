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
package com.alipay.common.tracer.core.samplers;

import com.alipay.common.tracer.core.constants.SofaTracerConstant;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SofaTracerPercentageBasedSampler
 *
 * @author yangguanchao
 * @since  2017/06/19
 */
public class SofaTracerPercentageBasedSampler implements Sampler {

    static final String             TYPE    = "PercentageBasedSampler";

    private final AtomicInteger     counter = new AtomicInteger(0);
    private final BitSet            sampleDecisions;
    private final SamplerProperties configuration;

    public SofaTracerPercentageBasedSampler(SamplerProperties configuration) {
        int outOf100 = (int) (configuration.getPercentage() * 100.0f);
        this.sampleDecisions = randomBitSet(100, outOf100, new Random());
        this.configuration = configuration;
    }

    @Override
    public SamplingStatus sample(String operation, String traceId) {
        SamplingStatus samplingStatus = new SamplingStatus();
        Map<String, Object> tags = new HashMap<String, Object>();
        tags.put(SofaTracerConstant.SAMPLER_TYPE_TAG_KEY, TYPE);
        tags.put(SofaTracerConstant.SAMPLER_PARAM_TAG_KEY, configuration.getPercentage());
        tags = Collections.unmodifiableMap(tags);
        samplingStatus.setTags(tags);

        if (this.configuration.getPercentage() == 0) {
            samplingStatus.setSampled(false);
            return samplingStatus;
        } else if (this.configuration.getPercentage() == 100) {
            samplingStatus.setSampled(true);
            return samplingStatus;
        }
        synchronized (this) {
            final int i = this.counter.getAndIncrement();
            boolean result = this.sampleDecisions.get(i);
            if (i == 99) {
                this.counter.set(0);
            }
            samplingStatus.setSampled(result);
            return samplingStatus;
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void close() {
        //do nothing
    }

    /**
     * Reservoir sampling algorithm borrowed from Stack Overflow.
     * <p>
     * http://stackoverflow.com/questions/12817946/generate-a-random-bitset-with-n-1s
     * @param size 大小
     * @param cardinality 基数
     * @param rnd 随机种子
     * @return BitSet
     */
    public static BitSet randomBitSet(int size, int cardinality, Random rnd) {
        BitSet result = new BitSet(size);
        int[] chosen = new int[cardinality];
        int i;
        for (i = 0; i < cardinality; ++i) {
            chosen[i] = i;
            result.set(i);
        }
        for (; i < size; ++i) {
            int j = rnd.nextInt(i + 1);
            if (j < cardinality) {
                result.clear(chosen[j]);
                result.set(i);
                chosen[j] = i;
            }
        }
        return result;
    }
}
