/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.alipay.sofa.tracer.boot.zipkin;

import org.junit.Test;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;

/**
 * @author qilong.zql
 * @since 2.2.0
 */
public class HashAlgorithmTest {

    @Test
    public void testFNV64AndMurmurHash2() throws Exception {
        long fnvCost = testHashCode(new HashAlgorithm() {
            @Override
            public long hash(String data) {
                return ZipkinSofaTracerSpanRemoteReporter.FNV64HashCode(data);
            }
        });

        long murCost = testHashCode(new HashAlgorithm() {
            @Override
            public long hash(String data) {
                return ZipkinSofaTracerSpanRemoteReporter.MurmurHash64(data);
            }
        });
        Assert.isTrue(fnvCost > murCost );
    }

    /***
     * 测试 FNV64HashCode 算法性能,如果不满足性能要求,那么考虑更优秀算法
     * 目标: 200 万数据务必在 10s 内完成且没有碰撞发生
     * hash FNVHash64 : http://www.isthe.com/chongo/tech/comp/fnv/index.html#FNV-param
     * @throws Exception 异常
     */
    public long testHashCode(HashAlgorithm hashAlgorithm) throws Exception {
        //one million no redundant
        long startTime = System.currentTimeMillis();
        //100
        int iLong = 20;
        int jLong = 50;
        int kLong = 50;
        int lLong = 100;
        Map<Long, Long> mapHash = new HashMap<Long, Long>();
        for (int i = 0; i < iLong; i++) {
            String spanId1 = "" + i;
            //hash
            entranceHash(mapHash, spanId1, hashAlgorithm);
            //100000
            for (int j = 0; j < jLong; j++) {
                String spanId2 = i + "." + j;
                //hash
                entranceHash(mapHash, spanId2, hashAlgorithm);
                for (int k = 0; k < kLong; k++) {
                    String spanId3 = i + "." + j + "." + k;
                    //hash
                    entranceHash(mapHash, spanId3, hashAlgorithm);
                    for (int l = 0; l < lLong; l++) {
                        String spanId4 = i + "." + j + "." + k + "." + l;
                        //hash
                        entranceHash(mapHash, spanId4, hashAlgorithm);
                    }
                }
            }
        }
        long cost = System.currentTimeMillis() - startTime;
        long count = (iLong * jLong * kLong * lLong) + (iLong * jLong * kLong) + (iLong * jLong)
                + iLong;
        //目标: 200 万数据务必在 10s 内完成
        assertTrue("Count = " + count + ",FNV64HashCode Cost = " + cost + " ms", cost < 10 * 1000);
        //重复
        Map<Long, Long> redundantMap = getRedundant(mapHash);
        assertTrue("FNV64HashCode Redundant Size = " + redundantMap.size() + " ; Redundant = "
                + redundantMap, redundantMap.size() <= 0);
        return cost;
    }

    private void entranceHash(Map<Long, Long> map, String data, HashAlgorithm hashAlgorithm) {
        long hashCode = hashAlgorithm.hash(data);
        this.putMap(hashCode, map);
    }

    private void putMap(long hashCode, Map<Long, Long> map) {
        if (map.containsKey(hashCode)) {
            long count = map.get(hashCode);
            map.put(hashCode, ++count);
        } else {
            map.put(hashCode, 1L);
        }
    }

    private Map<Long, Long> getRedundant(Map<Long, Long> originMap) {
        Map<Long, Long> resultMap = new HashMap<Long, Long>();
        for (Map.Entry<Long, Long> entry : originMap.entrySet()) {
            Long value = entry.getValue();
            if (value > 1) {
                Long key = entry.getKey();
                resultMap.put(key, value);
            }
        }
        return resultMap;
    }

    private interface HashAlgorithm {
        long hash(String data);
    }
}