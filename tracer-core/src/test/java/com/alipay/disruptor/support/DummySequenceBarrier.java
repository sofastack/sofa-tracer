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
package com.alipay.disruptor.support;

import com.alipay.disruptor.AlertException;
import com.alipay.disruptor.SequenceBarrier;

/**
 * @description: [support for test strategy]
 * @email: <a href="guolei.sgl@antfin.com"></a>
 * @author: guolei.sgl
 * @date: 18/8/1
 */
public class DummySequenceBarrier implements SequenceBarrier {
    @Override
    public long waitFor(long sequence) throws AlertException, InterruptedException {
        return 0;
    }

    @Override
    public long getCursor() {
        return 0;
    }

    @Override
    public boolean isAlerted() {
        return false;
    }

    @Override
    public void alert() {
    }

    @Override
    public void clearAlert() {
    }

    @Override
    public void checkAlert() throws AlertException {
    }
}
