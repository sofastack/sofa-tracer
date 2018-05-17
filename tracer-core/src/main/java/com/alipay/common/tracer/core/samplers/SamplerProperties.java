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

/**
 * SamplerProperties
 *
 * @author yangguanchao
 * @since  2017/06/20
 */
public class SamplerProperties {
    /**
     * Percentage of requests that should be sampled. E.g. 1.0 - 100% requests should be
     * sampled. The precision is whole-numbers only (i.e. there's no support for 0.1% of
     * the traces).
     */
    private float percentage = 0.1f;

    public float getPercentage() {
        return this.percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }
}
