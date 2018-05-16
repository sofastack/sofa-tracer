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
package com.alipay.common.tracer.core.reporter.stat.model;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 不断被更新的统计数据
 * <p>
 * 注意这里用的是CAS机制
 * 该机制用失败重试的方式代替了锁
 * 它允许多线程同时对一个原子进行修改，但是当写入新值却发现新值已被修改时，需要重新执行赋值过程，直至成功
 * 需要注意的是赋值过程是会可能被不断重复执行的，那赋值算法中唯一允许变化的就是该原子内部的属性值，其他的因数不能发生变化
 * 这样能在大部分没有并发冲突的情况下提高性能
 * 所有对AtomicReference中的值的更改，必需是写入一个全新的数组，不能直接更改原来已有数组中的值（这样才能保证是对原子进行修改）
 * @author zhanghan
 */
public class StatValues {
    /**
     *  真实的value
     */
    private final AtomicReference<long[]> values = new AtomicReference<long[]>();

    public StatValues(long[] values) {
        this.values.set(values);
    }

    /**
     * 写入新值
     * (所有操作需满足CAS机制)
     * @param update 要更新的新值
     */
    public void update(long[] update) {
        long[] current;
        long[] tmp = new long[update.length];
        do {
            current = values.get();
            for (int k = 0; k < update.length && k < current.length; k++) {
                tmp[k] = current[k] + update[k];
            }
        } while (!values.compareAndSet(current, tmp));

    }

    /**
     * 打印完毕后对槽清空
     * 由于在打印期间值可能已经被更新，所以传入需要被clear的数值数组，减去已被打印的值
     * (所有操作需满足CAS机制)
     *  @param toBeClear long 数组
     */
    public void clear(long[] toBeClear) {
        long[] current;
        long[] tmp = new long[toBeClear.length];
        do {
            current = values.get();
            for (int k = 0; k < current.length && k < toBeClear.length; k++) {
                tmp[k] = current[k] - toBeClear[k];
            }
        } while (!values.compareAndSet(current, tmp));
    }

    /**
     * 返回当前值
     * 这里可以直接get并返回，返回的值肯定不会改变
     * 因为任何对values这个原子的改变都是直接覆盖数组的引用地址
     * 而不会更新数组中的值
     * @return long 数组
     */
    public long[] getCurrentValue() {
        return values.get();
    }
}
