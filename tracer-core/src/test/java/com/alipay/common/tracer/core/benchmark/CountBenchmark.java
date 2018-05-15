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
package com.alipay.common.tracer.core.benchmark;

import com.alipay.common.tracer.core.utils.StringUtils;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

/**
 * 对比 JDK6 下 {@link StringTokenizer#countTokens()} 和 {@link String#split(String)} 的性能.
 * 
 * JDK 6, Mac OS 10.11.1, 性能数据如下:
 *
 * <pre>
 * Benchmark                                Mode    Cnt     Score       Error   Units
 * CountBenchmark.countUseStringSplit       avgt    20      15.044  ±   0.735   us/op
 * CountBenchmark.countUseStringTokenizer   avgt    20      7.446   ±   0.443   us/op
 * CountBenchmark.countUseCountMatches      avgt    20      0.759   ±   0.069   us/op
 * </pre>
 * 
 * @author khotyn 12/29/15 3:44 PM
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class CountBenchmark {
    private static final String RPC_ID = "0.1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20.21.22.23.24.25.26.27.28."
                                         + "29.30.31.32.33.34.35.36.37.38.39.40.41.42.43.44.45.46.47.48.49.50.51.52.53.54.55.56.57.58.59.60.61.62.63"
                                         + ".64.65.66.67.68.69.70.71.72.73.74.75.76.77.78.79.80.81.82.83.84.85.86.87.88.89.90.91.92.93.94.95.96.97.98"
                                         + ".99.100.101.102.103.104.105.106.107.108.109.110.111.112.113.114.115.116.117.118.119.120.121.122.123.124."
                                         + "125.126.127.128.129.130.131.132.133.134.135.136.137.138.139.140.141.142.143.144.145.146.147.148.149.150."
                                         + "151.152.153.154.155.156.157.158.159.160.161.162.163.164.165.166.167.168.169.170.171.172.173.174.175.176"
                                         + ".177.178.179.180.181.182.183.184.185.186.187.188.189.190.191.192.193.194.195.196.197.198.199.200";

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void countUseStringTokenizer(Blackhole blackhole) {
        blackhole.consume(new StringTokenizer(RPC_ID, ".").countTokens());
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void countUseStringSplit(Blackhole blackhole) {
        blackhole.consume(RPC_ID.split("\\.").length);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void countUseCountMatches(Blackhole blackhole) {
        blackhole.consume(StringUtils.countMatches(RPC_ID, '.'));
    }

    @Test
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder().include(CountBenchmark.class.getSimpleName()).forks(1)
            .build();
        new Runner(opt).run();
    }
}
